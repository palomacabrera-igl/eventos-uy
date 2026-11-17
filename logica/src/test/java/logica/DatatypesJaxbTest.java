package logica;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba que todos los DT (las clases DT...) pueden viajar por un servicio web.
 *
 * Por la red viaja XML, y JAXB es la biblioteca que convierte un objeto en XML y
 * lo vuelve a armar del otro lado. Para reconstruir un objeto, JAXB lo crea
 * vacio y despues completa cada campo con su setter. Por eso cada DT tiene que
 * cumplir tres reglas:
 *
 *   1. Un constructor publico SIN parametros.
 *   2. Un getter y un setter publicos por cada campo.
 *   3. Nada de Set ni de fechas de java.time: se usa List y DTFecha.
 *   4. @XmlType(name = "NombreDeLaClase"). Sin esto JAXB nombra el tipo con
 *      minuscula inicial (dtEvento) y el sitio, al generar su cliente, tendria
 *      que usar clases llamadas DtEvento en lugar de DTEvento.
 *
 * Si agregas un campo o un DT nuevo y esta prueba falla, falta una de esas tres
 * cosas. El mensaje dice en que clase y en que campo.
 *
 * Las pruebas de ida y vuelta arman cada DT con TODOS sus campos llenos, lo
 * convierten a XML, lo reconstruyen y lo convierten otra vez. Controlan dos cosas:
 *   - que cada campo aparezca en el XML: si le falta el setter, JAXB ignora el
 *     campo en silencio y nunca viaja (por eso tambien se revisa la regla 2 aparte);
 *   - que la segunda copia coincida con la primera: detecta tipos que JAXB no sabe
 *     reconstruir, colecciones mal armadas o subclases (DTAsistente) que se pierden.
 * Cuando agregues un DT nuevo, agrega tambien su ejemplo en ejemplos().
 */
class DatatypesJaxbTest {

    // ===== Reglas de forma =====

    @Test
    @DisplayName("cada DT tiene un constructor publico sin parametros")
    void constructorVacio() {
        for (Class<?> dt : datatypes()) {
            try {
                assertTrue(Modifier.isPublic(dt.getConstructor().getModifiers()),
                        dt.getSimpleName() + ": el constructor sin parametros tiene que ser publico");
            } catch (NoSuchMethodException e) {
                throw new AssertionError(dt.getSimpleName()
                        + " no tiene constructor publico sin parametros (lo exige JAXB)");
            }
        }
    }

    @Test
    @DisplayName("cada campo de un DT tiene su getter y su setter publicos")
    void getterYSetterPorCampo() {
        for (Class<?> dt : datatypes()) {
            for (var campo : dt.getDeclaredFields()) {
                if (Modifier.isStatic(campo.getModifiers())) {
                    continue;
                }
                String nombre = Character.toUpperCase(campo.getName().charAt(0)) + campo.getName().substring(1);
                assertTrue(tieneMetodo(dt, "get" + nombre, 0) || tieneMetodo(dt, "is" + nombre, 0),
                        dt.getSimpleName() + "." + campo.getName() + " no tiene getter publico (get" + nombre + ")");
                assertTrue(tieneMetodo(dt, "set" + nombre, 1),
                        dt.getSimpleName() + "." + campo.getName() + " no tiene setter publico (set" + nombre + ")");
            }
        }
    }

    @Test
    @DisplayName("ningun DT usa Set ni fechas de java.time")
    void sinSetNiLocalDate() {
        for (Class<?> dt : datatypes()) {
            for (Method m : dt.getMethods()) {
                // Solo los getters: lo que viaja es lo que el getter devuelve. Metodos como
                // DTFecha.aLocalDate() son ayudas locales y no viajan.
                boolean getter = m.getParameterCount() == 0 && m.getDeclaringClass() != Object.class
                        && (m.getName().startsWith("get") || m.getName().startsWith("is"));
                if (!getter) {
                    continue;
                }
                Class<?> tipo = m.getReturnType();
                assertTrue(!Set.class.isAssignableFrom(tipo),
                        dt.getSimpleName() + "." + m.getName() + " devuelve Set: usa List");
                assertTrue(!tipo.getName().startsWith("java.time."),
                        dt.getSimpleName() + "." + m.getName() + " devuelve " + tipo.getSimpleName()
                                + ": usa DTFecha");
            }
        }
    }

    @Test
    @DisplayName("cada DT declara @XmlType con su propio nombre")
    void nombreDeTipoIgualAlDeLaClase() {
        for (Class<?> dt : datatypes()) {
            XmlType anotacion = dt.getAnnotation(XmlType.class);
            assertNotNull(anotacion, dt.getSimpleName() + " no tiene @XmlType(name = \""
                    + dt.getSimpleName() + "\"): el sitio generaria una clase con otro nombre");
            assertEquals(dt.getSimpleName(), anotacion.name(),
                    dt.getSimpleName() + ": el name de @XmlType tiene que ser el nombre de la clase");
        }
    }

    @Test
    @DisplayName("cada DT que existe tiene su ejemplo en esta prueba")
    void hayUnEjemploPorDatatype() {
        Set<String> existentes = datatypes().stream().map(Class::getSimpleName)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> conEjemplo = ejemplos().keySet().stream().map(Class::getSimpleName)
                .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(existentes, conEjemplo,
                "Los DT y los ejemplos de DatatypesJaxbTest.ejemplos() tienen que ser los mismos");
    }

    // ===== Ida y vuelta por JAXB =====

    @TestFactory
    @DisplayName("cada DT sobrevive a la ida y vuelta por XML sin perder ningun campo")
    Stream<DynamicTest> idaYVuelta() throws JAXBException {
        JAXBContext contexto = JAXBContext.newInstance(datatypes().toArray(new Class<?>[0]));
        return ejemplos().entrySet().stream().map(e -> DynamicTest.dynamicTest(
                e.getKey().getSimpleName(), () -> idaYVuelta(contexto, e.getKey(), e.getValue())));
    }

    @Test
    @DisplayName("un asistente y un organizador viajan como DTUsuario sin perder quien es cada uno")
    void subtiposComoDTUsuario() throws JAXBException {
        JAXBContext contexto = JAXBContext.newInstance(datatypes().toArray(new Class<?>[0]));

        DTAsistente asistente = (DTAsistente) ejemplos().get(DTAsistente.class);
        DTUsuario llegoAsistente = desdeXml(contexto, DTUsuario.class,
                aXml(contexto, DTUsuario.class, asistente));
        assertInstanceOf(DTAsistente.class, llegoAsistente);
        assertTrue(llegoAsistente.esAsistente());
        assertEquals("Perez", ((DTAsistente) llegoAsistente).getApellido());

        DTOrganizador organizador = (DTOrganizador) ejemplos().get(DTOrganizador.class);
        DTUsuario llegoOrganizador = desdeXml(contexto, DTUsuario.class,
                aXml(contexto, DTUsuario.class, organizador));
        assertInstanceOf(DTOrganizador.class, llegoOrganizador);
        assertTrue(llegoOrganizador.esOrganizador());
    }

    @Test
    @DisplayName("los bytes de una imagen llegan iguales")
    void imagenConservaSusBytes() throws JAXBException {
        JAXBContext contexto = JAXBContext.newInstance(DTImagen.class);
        DTImagen original = new DTImagen(new byte[]{0, 1, 2, (byte) 0xFF, (byte) 0x80}, "image/png");

        DTImagen llego = desdeXml(contexto, DTImagen.class, aXml(contexto, DTImagen.class, original));

        assertEquals(original.getTipo(), llego.getTipo());
        org.junit.jupiter.api.Assertions.assertArrayEquals(original.getContenido(), llego.getContenido());
    }

    private static <T> void idaYVuelta(JAXBContext contexto, Class<T> clase, Object ejemplo)
            throws JAXBException {
        assertTodosLosCamposLlenos(ejemplo);

        T original = clase.cast(ejemplo);
        String primero = aXml(contexto, clase, original);

        // Cada campo con getter tiene que aparecer como elemento del XML.
        for (Method m : ejemplo.getClass().getMethods()) {
            String campo = nombreDeCampo(m);
            if (campo != null) {
                assertTrue(primero.contains("<" + campo + ">") || primero.contains("<" + campo + " "),
                        clase.getSimpleName() + "." + campo + " no aparece en el XML: JAXB lo ignora "
                                + "(casi seguro le falta el setter)");
            }
        }

        T reconstruido = desdeXml(contexto, clase, primero);
        assertNotNull(reconstruido);
        String segundo = aXml(contexto, clase, reconstruido);

        assertEquals(primero, segundo,
                clase.getSimpleName() + ": al reconstruirlo desde el XML se perdio algun dato");
    }

    /** "getTieneImagen" -> "tieneImagen". Devuelve null si el metodo no es un getter. */
    private static String nombreDeCampo(Method m) {
        if (m.getParameterCount() != 0 || m.getDeclaringClass() == Object.class
                || m.getReturnType() == void.class) {
            return null;
        }
        String nombre = m.getName();
        String resto;
        if (nombre.startsWith("get") && nombre.length() > 3) {
            resto = nombre.substring(3);
        } else if (nombre.startsWith("is") && nombre.length() > 2) {
            resto = nombre.substring(2);
        } else {
            return null;
        }
        return Character.toLowerCase(resto.charAt(0)) + resto.substring(1);
    }

    /**
     * Si el ejemplo dejara un campo vacio, la prueba de ida y vuelta no podria
     * notar que ese campo se pierde: vacio antes y vacio despues.
     */
    private static void assertTodosLosCamposLlenos(Object ejemplo) {
        for (Method m : ejemplo.getClass().getMethods()) {
            boolean getter = m.getParameterCount() == 0 && m.getDeclaringClass() != Object.class
                    && (m.getName().startsWith("get") || m.getName().startsWith("is"))
                    && m.getReturnType() != void.class;
            if (!getter) {
                continue;
            }
            Object valor;
            try {
                valor = m.invoke(ejemplo);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            String donde = ejemplo.getClass().getSimpleName() + "." + m.getName() + " en el ejemplo";
            assertNotNull(valor, donde + " esta en null: llenalo");
            if (valor instanceof Collection<?> c) {
                assertTrue(!c.isEmpty(), donde + " esta vacio: agrega un elemento");
            } else if (valor instanceof Boolean b) {
                assertTrue(b, donde + " esta en false: ponelo en true");
            } else if (valor instanceof Number n) {
                assertTrue(n.doubleValue() != 0, donde + " esta en 0: poner otro valor");
            }
        }
    }

    // ===== Utilidades =====

    private static <T> String aXml(JAXBContext contexto, Class<T> clase, T objeto) throws JAXBException {
        Marshaller marshaller = contexto.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter salida = new StringWriter();
        marshaller.marshal(new JAXBElement<>(new QName("dato"), clase, objeto), salida);
        return salida.toString();
    }

    private static <T> T desdeXml(JAXBContext contexto, Class<T> clase, String xml) throws JAXBException {
        Unmarshaller unmarshaller = contexto.createUnmarshaller();
        return unmarshaller.unmarshal(new StreamSource(new StringReader(xml)), clase).getValue();
    }

    private static boolean tieneMetodo(Class<?> clase, String nombre, int parametros) {
        for (Method m : clase.getMethods()) {
            if (m.getName().equals(nombre) && m.getParameterCount() == parametros) {
                return true;
            }
        }
        return false;
    }

    /** Todas las clases DT... compiladas en el paquete logica. Un DT nuevo entra solo. */
    private static List<Class<?>> datatypes() {
        try {
            Path carpeta = Path.of(DTFecha.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .resolve("logica");
            List<Class<?>> clases = new ArrayList<>();
            try (Stream<Path> archivos = Files.list(carpeta)) {
                for (Path archivo : archivos.toList()) {
                    String nombre = archivo.getFileName().toString();
                    if (nombre.matches("DT[A-Za-z]+\\.class")) {
                        clases.add(Class.forName("logica." + nombre.replace(".class", "")));
                    }
                }
            }
            clases.sort((a, b) -> a.getSimpleName().compareTo(b.getSimpleName()));
            return clases;
        } catch (URISyntaxException | java.io.IOException | ClassNotFoundException e) {
            throw new AssertionError("No se pudieron buscar los DT compilados", e);
        }
    }

    // ===== Un ejemplo de cada DT, con todos los campos llenos =====

    private static Map<Class<?>, Object> ejemplos() {
        DTFecha fecha = new DTFecha(28, 10, 2026);

        DTTipoRegistro tipo = new DTTipoRegistro("General", "Entrada general", 500.0, 40);
        DTRegistro registro = new DTRegistro("ana", "JIAP 2027", "General", 500.0,
                new DTFecha(1, 11, 2026), true);
        DTPatrocinio patrocinio = new DTPatrocinio("ANTEL-27", new DTFecha(1, 3, 2027),
                100000.0, "ORO", 5, "ANTEL", "General");

        DTAsistente asistente = new DTAsistente("ana", "Ana", "ana@correo.uy", "Perez",
                new DTFecha(5, 3, 1998), "ANCAP", true);
        asistente.setFechaAlta(fecha);
        DTOrganizador organizador = new DTOrganizador("imm", "IMM", "imm@correo.uy",
                "Intendencia de Montevideo", "https://montevideo.gub.uy", true);
        organizador.setFechaAlta(fecha);
        DTUsuario usuario = new DTUsuario("pepe", "Pepe", "pepe@correo.uy");
        usuario.setTipo(TipoUsuario.ASISTENTE);
        usuario.setTieneImagen(true);
        usuario.setFechaAlta(fecha);

        Map<Class<?>, Object> ejemplos = new LinkedHashMap<>();
        ejemplos.put(DTFecha.class, fecha);
        ejemplos.put(DTTipoRegistro.class, tipo);
        ejemplos.put(DTRegistro.class, registro);
        ejemplos.put(DTPatrocinio.class, patrocinio);
        ejemplos.put(DTUsuario.class, usuario);
        ejemplos.put(DTAsistente.class, asistente);
        ejemplos.put(DTOrganizador.class, organizador);
        ejemplos.put(DTCategoria.class,
                new DTCategoria("Tecnologia", new ArrayList<>(List.of(new DTCategoria("IA")))));
        ejemplos.put(DTDatosRegistro.class,
                new DTDatosRegistro(new ArrayList<>(List.of(tipo)), new ArrayList<>(List.of(asistente))));
        ejemplos.put(DTEdicionCompleto.class, new DTEdicionCompleto("JIAP 2027", "JIAP27", fecha,
                new DTFecha(2, 10, 2027), new DTFecha(4, 10, 2027), "Uruguay", "Montevideo",
                organizador, new ArrayList<>(List.of(tipo)), new ArrayList<>(List.of(registro)),
                new ArrayList<>(List.of(patrocinio)), EstadoEdicion.ACEPTADA, true, "JIAP"));
        ejemplos.put(DTEdicionEvento.class, new DTEdicionEvento("JIAP 2027", "JIAP27", "Montevideo",
                "Uruguay", new DTFecha(2, 10, 2027), new DTFecha(4, 10, 2027), fecha,
                EstadoEdicion.INGRESADA, true, "JIAP"));
        ejemplos.put(DTEvento.class, new DTEvento("JIAP", "JIAP", "Jornadas de informatica", fecha,
                new ArrayList<>(List.of("Tecnologia", "Educacion")), true));
        ejemplos.put(DTImagen.class, new DTImagen(new byte[]{1, 2, 3}, "image/png"));
        ejemplos.put(DTInstitucion.class,
                new DTInstitucion("ANCAP", "Combustibles", "https://ancap.com.uy", true));
        return ejemplos;
    }
}
