package servicios;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.xml.ws.Endpoint;
import logica.DTAsistente;
import logica.DTEdicionCompleto;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTImagen;
import logica.DTOrganizador;
import logica.DTPatrocinio;
import logica.DTRegistro;
import logica.DTTipoRegistro;
import logica.EstadoEdicion;
import logica.IControladorSistema;
import logica.ReglaNegocioException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la fachada de servicios web (Publicador).
 *
 * No necesitan base de datos: la fachada recibe un controlador de mentira que
 * contesta datos fijos y anota con que parametros lo llamaron. Lo que se
 * prueba es lo que la fachada agrega de verdad: el contrato que publica y la
 * forma en que viajan los datos por la red.
 *
 * El servicio se publica en un puerto libre y se le mandan sobres SOAP escritos
 * a mano con el cliente HTTP del JDK. Se hace asi, y no con un cliente
 * generado, para ver el XML real que viaja: es lo que recibe el sitio.
 */
class PublicadorTest {

    private static final String NS = "http://servicios/";
    private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";

    /** Cada llamada que llego al controlador de mentira: {nombre, argumentos}. */
    private static final List<Object[]> LLAMADAS = new CopyOnWriteArrayList<>();

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static Endpoint endpoint;
    private static String url;

    @BeforeAll
    static void publicar() throws IOException {
        int puerto;
        try (ServerSocket libre = new ServerSocket(0)) {
            puerto = libre.getLocalPort();
        }
        url = "http://localhost:" + puerto + "/eventosuy";
        endpoint = Endpoint.publish(url, new Publicador(controladorDeMentira()));
    }

    @AfterAll
    static void detener() {
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    @BeforeEach
    void limpiarLlamadas() {
        LLAMADAS.clear();
    }

    // =====================================================================
    //  El contrato (WSDL)
    // =====================================================================

    @Test
    @DisplayName("el WSDL publica exactamente las operaciones de la fachada")
    void elWsdlTieneTodasLasOperaciones() throws Exception {
        Set<String> enElWsdl = operacionesDelWsdl();
        Set<String> enLaClase = new TreeSet<>();
        for (Method m : metodosPublicados()) {
            enLaClase.add(m.getName());
        }

        assertEquals(enLaClase, enElWsdl,
                "Las operaciones del WSDL y los metodos publicos de Publicador tienen que ser las mismas");
        assertTrue(enElWsdl.size() >= 30, "Se esperan al menos las 30 operaciones que usa el sitio hoy");
    }

    @Test
    @DisplayName("los parametros tienen nombre propio y no arg0, arg1...")
    void sinArgCero() throws Exception {
        assertFalse(contratoCompleto().contains("arg0"),
                "Falta algun @WebParam(name = ...): en el contrato aparece un parametro arg0");
    }

    @Test
    @DisplayName("el contrato incluye las subclases de DTUsuario")
    void elContratoConoceLasSubclases() throws Exception {
        String contrato = contratoCompleto();
        assertTrue(contrato.contains("name=\"DTAsistente\""), "Falta DTAsistente (@XmlSeeAlso)");
        assertTrue(contrato.contains("name=\"DTOrganizador\""), "Falta DTOrganizador (@XmlSeeAlso)");
        assertTrue(contrato.contains("extension base="), "Las subclases deberian extender a DTUsuario");
    }

    @Test
    @DisplayName("los tipos del contrato se llaman igual que los DT (DTEvento y no dtEvento)")
    void nombresDeTiposIgualesALosDt() throws Exception {
        String contrato = contratoCompleto();
        for (String dt : List.of("DTEvento", "DTUsuario", "DTEdicionCompleto", "DTFecha", "DTImagen")) {
            assertTrue(contrato.contains("name=\"" + dt + "\""),
                    "El contrato no tiene el tipo " + dt + ": el sitio generaria otro nombre de clase");
        }
        assertFalse(contrato.contains("name=\"dtEvento\""), "Quedo un tipo con minuscula inicial");
    }

    @Test
    @DisplayName("las operaciones que pueden fallar declaran el fault de ReglaNegocioException")
    void declaranElFault() throws Exception {
        String contrato = contratoCompleto();
        assertTrue(contrato.contains("<fault") || contrato.contains(":fault"),
                "El contrato no declara ningun fault");
        assertTrue(contrato.contains("ReglaNegocioException"),
                "El fault de ReglaNegocioException no aparece en el contrato");
    }

    // =====================================================================
    //  Reglas de forma de la clase (para quien agregue operaciones)
    // =====================================================================

    @Test
    @DisplayName("cada operacion tiene @WebMethod, nombre unico y todos sus parametros con @WebParam")
    void reglasDeForma() {
        Set<String> nombres = new TreeSet<>();
        for (Method m : metodosPublicados()) {
            assertNotNull(m.getAnnotation(WebMethod.class), m.getName() + " no tiene @WebMethod");
            assertTrue(nombres.add(m.getName()),
                    m.getName() + " esta repetida: SOAP no admite dos operaciones con el mismo nombre");
            for (Parameter p : m.getParameters()) {
                WebParam wp = p.getAnnotation(WebParam.class);
                assertNotNull(wp, m.getName() + ": un parametro no tiene @WebParam");
                assertFalse(wp.name().isBlank(), m.getName() + ": @WebParam sin name");
            }
        }
    }

    @Test
    @DisplayName("no se publica ninguna operacion con estado del Laboratorio 1")
    void sinOperacionesConEstado() {
        for (Method m : metodosPublicados()) {
            assertFalse(m.getName().startsWith("seleccionar") || m.getName().startsWith("ingresar"),
                    m.getName() + " es una operacion con estado: con varios clientes se pisarian");
        }
    }

    @Test
    @DisplayName("ninguna firma usa Set ni fechas de java.time, y cada operacion existe en el controlador")
    void firmasQueViajan() throws Exception {
        for (Method m : metodosPublicados()) {
            assertViaja(m.getName(), m.getReturnType());
            for (Class<?> parametro : m.getParameterTypes()) {
                assertViaja(m.getName(), parametro);
            }
            // La fachada solo delega: tiene que existir la misma operacion en el controlador.
            assertNotNull(IControladorSistema.class.getMethod(m.getName(), m.getParameterTypes()),
                    m.getName() + " no existe en IControladorSistema");
        }
    }

    private static void assertViaja(String operacion, Class<?> tipo) {
        assertFalse(Set.class.isAssignableFrom(tipo), operacion + " usa Set: devolve List");
        assertFalse(tipo.getName().startsWith("java.time."), operacion + " usa " + tipo.getSimpleName()
                + ": usa DTFecha");
    }

    // =====================================================================
    //  Lo que viaja por la red
    // =====================================================================

    @Test
    @DisplayName("una lista llega como elementos repetidos y el parametro ausente llega como null")
    void listaDeEventos() throws Exception {
        Respuesta r = soap("<ns:listarEventos xmlns:ns=\"" + NS + "\"/>");

        assertEquals(200, r.estado(), r.cuerpo());
        assertEquals(2, contar(r.cuerpo(), "<return>") + contar(r.cuerpo(), "<return "), r.cuerpo());
        assertContiene(r, "<nombre>JIAP</nombre>");
        assertContiene(r, "<nombre>Expo Tecnologia</nombre>");
        // DTFecha viaja como un elemento con tres hijos.
        assertContiene(r, "<dia>20</dia>");
        assertContiene(r, "<anio>2026</anio>");
        // Una List<String> dentro de un DT tambien viaja como elementos repetidos.
        assertEquals(3, contar(r.cuerpo(), "<categorias>"), r.cuerpo());

        assertArrayEquals(new Object[]{null}, ultimosArgumentos("listarEventos"),
                "Sin el elemento <categoria>, la logica tiene que recibir null");
    }

    @Test
    @DisplayName("el parametro con valor llega a la logica tal cual")
    void parametroConValor() throws Exception {
        soap("<ns:listarEventos xmlns:ns=\"" + NS + "\"><categoria>Tecnologia</categoria></ns:listarEventos>");

        assertArrayEquals(new Object[]{"Tecnologia"}, ultimosArgumentos("listarEventos"));
    }

    @Test
    @DisplayName("un asistente viaja como DTUsuario sin perder su tipo ni sus datos")
    void subtipoAsistente() throws Exception {
        Respuesta r = soap("<ns:consultarUsuario xmlns:ns=\"" + NS + "\"><nickname>ana</nickname></ns:consultarUsuario>");

        assertEquals(200, r.estado(), r.cuerpo());
        assertContiene(r, "xsi:type=");
        assertContiene(r, "<apellido>Perez</apellido>");
        assertContiene(r, "<tipo>ASISTENTE</tipo>");
        assertArrayEquals(new Object[]{"ana", null}, ultimosArgumentos("consultarUsuario"),
                "El solicitante ausente (un visitante) tiene que llegar como null");
    }

    @Test
    @DisplayName("una lista de usuarios mezcla asistentes y organizadores")
    void listaDeUsuariosMezclada() throws Exception {
        Respuesta r = soap("<ns:listarUsuarios xmlns:ns=\"" + NS + "\"/>");

        assertEquals(200, r.estado(), r.cuerpo());
        assertContiene(r, "<apellido>Perez</apellido>");
        assertContiene(r, "<descripcion>Intendencia de Montevideo</descripcion>");
    }

    @Test
    @DisplayName("una edicion completa viaja con sus listas y sus fechas anidadas")
    void edicionCompleta() throws Exception {
        Respuesta r = soap("<ns:consultarEdicion xmlns:ns=\"" + NS + "\"><nombreEdicion>JIAP 2027</nombreEdicion>"
                + "<solicitante>imm</solicitante></ns:consultarEdicion>");

        assertEquals(200, r.estado(), r.cuerpo());
        assertContiene(r, "<estado>ACEPTADA</estado>");
        assertContiene(r, "<fechaIni>");
        assertEquals(2, contar(r.cuerpo(), "<registros>"), r.cuerpo());
        assertEquals(1, contar(r.cuerpo(), "<tiposRegistro>"), r.cuerpo());
        assertEquals(1, contar(r.cuerpo(), "<patrocinios>"), r.cuerpo());
        assertContiene(r, "<nickname>imm</nickname>");
    }

    @Test
    @DisplayName("un error de negocio llega como SOAP Fault con el mensaje original")
    void faultConElMensaje() throws Exception {
        Respuesta r = soap("<ns:consultarEvento xmlns:ns=\"" + NS + "\"><nombre>NoExiste</nombre></ns:consultarEvento>");

        assertEquals(500, r.estado(), "SOAP 1.1 contesta un fault con HTTP 500: " + r.cuerpo());
        assertContiene(r, "<faultstring>No existe el evento NoExiste.</faultstring>");
        assertContiene(r, "ReglaNegocioException");
    }

    @Test
    @DisplayName("los bytes de una imagen viajan en Base64 y su tipo con ellos")
    void imagenEnBase64() throws Exception {
        Respuesta r = soap("<ns:obtenerImagenEvento xmlns:ns=\"" + NS + "\"><nombreEvento>JIAP</nombreEvento>"
                + "</ns:obtenerImagenEvento>");

        assertEquals(200, r.estado(), r.cuerpo());
        assertContiene(r, "<contenido>AQID</contenido>"); // {1, 2, 3} en Base64
        assertContiene(r, "<tipo>image/png</tipo>");
    }

    @Test
    @DisplayName("una operacion que devuelve null responde sin elemento return")
    void respuestaNull() throws Exception {
        Respuesta r = soap("<ns:obtenerImagenEvento xmlns:ns=\"" + NS + "\"><nombreEvento>SinImagen</nombreEvento>"
                + "</ns:obtenerImagenEvento>");

        assertEquals(200, r.estado(), r.cuerpo());
        assertFalse(r.cuerpo().contains("<contenido>"), r.cuerpo());
        assertFalse(r.cuerpo().contains("<return"), r.cuerpo());
    }

    @Test
    @DisplayName("altaRegistro sin codigo de patrocinio le llega a la logica con null")
    void altaRegistroSinCodigo() throws Exception {
        Respuesta r = soap("<ns:altaRegistro xmlns:ns=\"" + NS + "\"><nickname>ana</nickname>"
                + "<nombreEdicion>JIAP 2027</nombreEdicion><nombreTipo>General</nombreTipo></ns:altaRegistro>");

        assertEquals(200, r.estado(), r.cuerpo());
        assertArrayEquals(new Object[]{"ana", "JIAP 2027", "General", null}, ultimosArgumentos("altaRegistro"));
    }

    @Test
    @DisplayName("altaRegistro con codigo de patrocinio, y con tildes y enie, llega bien")
    void altaRegistroConCodigoYTildes() throws Exception {
        soap("<ns:altaRegistro xmlns:ns=\"" + NS + "\"><nickname>ana</nickname>"
                + "<nombreEdicion>Edición Ñandú</nombreEdicion><nombreTipo>General</nombreTipo>"
                + "<codigoPatrocinio>ANTEL-27</codigoPatrocinio></ns:altaRegistro>");

        assertArrayEquals(new Object[]{"ana", "Edición Ñandú", "General", "ANTEL-27"},
                ultimosArgumentos("altaRegistro"));
    }

    @Test
    @DisplayName("un DT que llega como parametro se reconstruye con todos sus campos")
    void dtComoParametro() throws Exception {
        soap("<ns:altaEdicion xmlns:ns=\"" + NS + "\"><solicitante>imm</solicitante>"
                + "<nombreEvento>JIAP</nombreEvento><datos><nombre>JIAP 2028</nombre><sigla>JIAP28</sigla>"
                + "<ciudad>Salto</ciudad><pais>Uruguay</pais>"
                + "<fechaInicio><dia>2</dia><mes>10</mes><anio>2028</anio></fechaInicio>"
                + "<fechaFin><dia>4</dia><mes>10</mes><anio>2028</anio></fechaFin></datos></ns:altaEdicion>");

        Object[] args = ultimosArgumentos("altaEdicion");
        assertEquals("imm", args[0]);
        assertEquals("JIAP", args[1]);
        var dt = (logica.DTEdicionEvento) args[2];
        assertEquals("JIAP 2028", dt.getNombre());
        assertEquals("Salto", dt.getCiudad());
        assertEquals(java.time.LocalDate.of(2028, 10, 4), dt.getFechaFin().aLocalDate());
        assertNull(args[3], "La imagen es opcional: ausente llega como null");
    }

    // =====================================================================
    //  El controlador de mentira
    // =====================================================================

    private static IControladorSistema controladorDeMentira() {
        return (IControladorSistema) Proxy.newProxyInstance(
                IControladorSistema.class.getClassLoader(),
                new Class<?>[]{IControladorSistema.class},
                (proxy, metodo, argumentos) -> {
                    Object[] args = argumentos == null ? new Object[0] : argumentos;
                    LLAMADAS.add(new Object[]{metodo.getName(), args});
                    return switch (metodo.getName()) {
                        case "listarEventos" -> new java.util.LinkedHashSet<>(List.of(
                                evento("JIAP", List.of("Tecnologia", "Educacion")),
                                evento("Expo Tecnologia", List.of("Tecnologia"))));
                        case "consultarUsuario" -> {
                            if ("ana".equals(args[0])) {
                                yield asistente();
                            }
                            throw new ReglaNegocioException("No existe el usuario " + args[0] + ".");
                        }
                        case "listarUsuarios" -> new LinkedHashSet<>(List.of(asistente(), organizador()));
                        case "consultarEvento" -> {
                            if ("JIAP".equals(args[0])) {
                                yield evento("JIAP", List.of("Tecnologia"));
                            }
                            throw new ReglaNegocioException("No existe el evento " + args[0] + ".");
                        }
                        case "consultarEdicion" -> ejemploDeEdicionCompleta();
                        case "obtenerImagenEvento" -> "JIAP".equals(args[0])
                                ? new DTImagen(new byte[]{1, 2, 3}, "image/png")
                                : null;
                        case "altaRegistro", "altaEdicion" -> null;
                        default -> throw new UnsupportedOperationException(
                                "El controlador de mentira no sabe contestar " + metodo.getName());
                    };
                });
    }

    private static DTEvento evento(String nombre, List<String> categorias) {
        return new DTEvento(nombre, nombre.substring(0, 4).toUpperCase(), "Descripcion de " + nombre,
                new DTFecha(20, 3, 2026), new ArrayList<>(categorias), true);
    }

    private static DTAsistente asistente() {
        DTAsistente a = new DTAsistente("ana", "Ana", "ana@correo.uy", "Perez",
                new DTFecha(5, 3, 1998), "ANCAP", false);
        a.setFechaAlta(new DTFecha(1, 1, 2026));
        return a;
    }

    private static DTOrganizador organizador() {
        return new DTOrganizador("imm", "IMM", "imm@correo.uy", "Intendencia de Montevideo",
                "https://montevideo.gub.uy", false);
    }

    private static DTEdicionCompleto ejemploDeEdicionCompleta() {
        DTRegistro uno = new DTRegistro("ana", "JIAP 2027", "General", 500.0, new DTFecha(1, 11, 2026), false);
        DTRegistro dos = new DTRegistro("luis", "JIAP 2027", "General", 500.0, new DTFecha(2, 11, 2026), true);
        return new DTEdicionCompleto("JIAP 2027", "JIAP27", new DTFecha(1, 3, 2026),
                new DTFecha(2, 10, 2027), new DTFecha(4, 10, 2027), "Uruguay", "Montevideo", organizador(),
                new ArrayList<>(List.of(new DTTipoRegistro("General", "Entrada general", 500.0, 40))),
                new ArrayList<>(List.of(uno, dos)),
                new ArrayList<>(List.of(new DTPatrocinio("ANTEL-27", new DTFecha(1, 3, 2027), 100000.0,
                        "ORO", 5, "ANTEL", "General"))),
                EstadoEdicion.ACEPTADA, false, "JIAP");
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    private record Respuesta(int estado, String cuerpo) {
    }

    /** Manda un sobre SOAP con ese cuerpo y devuelve lo que contesta el servicio. */
    private static Respuesta soap(String cuerpo) throws Exception {
        String sobre = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + cuerpo + "</S:Body></S:Envelope>";
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(sobre, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> respuesta = HTTP.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Respuesta(respuesta.statusCode(), respuesta.body());
    }

    private static String obtener(String direccion) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(direccion)).timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<String> respuesta = HTTP.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, respuesta.statusCode(), "GET " + direccion);
        return respuesta.body();
    }

    /** El WSDL y todos los XSD que importa, seguidos uno por uno, en un solo texto. */
    private static String contratoCompleto() throws Exception {
        Set<String> vistos = new LinkedHashSet<>();
        Deque<String> pendientes = new ArrayDeque<>(List.of(url + "?wsdl"));
        StringBuilder todo = new StringBuilder();
        Pattern importa = Pattern.compile("schemaLocation=\"([^\"]+)\"");
        while (!pendientes.isEmpty()) {
            String actual = pendientes.poll();
            if (!vistos.add(actual)) {
                continue;
            }
            String xml = obtener(actual);
            todo.append(xml).append('\n');
            Matcher m = importa.matcher(xml);
            while (m.find()) {
                pendientes.add(m.group(1).replace("&amp;", "&"));
            }
        }
        return todo.toString();
    }

    private static Set<String> operacionesDelWsdl() throws Exception {
        var fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        Document wsdl = fabrica.newDocumentBuilder()
                .parse(new InputSource(new StringReader(obtener(url + "?wsdl"))));
        Element portType = (Element) wsdl.getElementsByTagNameNS(WSDL_NS, "portType").item(0);
        NodeList operaciones = portType.getElementsByTagNameNS(WSDL_NS, "operation");
        Set<String> nombres = new TreeSet<>();
        for (int i = 0; i < operaciones.getLength(); i++) {
            nombres.add(((Element) operaciones.item(i)).getAttribute("name"));
        }
        return nombres;
    }

    /** Los metodos publicos de Publicador: cada uno es una operacion del contrato. */
    private static List<Method> metodosPublicados() {
        List<Method> publicos = new ArrayList<>();
        for (Method m : Publicador.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !m.isSynthetic()) {
                publicos.add(m);
            }
        }
        return publicos;
    }

    private static Object[] ultimosArgumentos(String operacion) {
        for (int i = LLAMADAS.size() - 1; i >= 0; i--) {
            if (LLAMADAS.get(i)[0].equals(operacion)) {
                return (Object[]) LLAMADAS.get(i)[1];
            }
        }
        throw new AssertionError("El controlador no recibio ninguna llamada a " + operacion
                + ". Llamadas: " + LLAMADAS.stream().map(l -> l[0] + Arrays.toString((Object[]) l[1])).toList());
    }

    private static int contar(String texto, String fragmento) {
        int cantidad = 0;
        for (int desde = texto.indexOf(fragmento); desde >= 0; desde = texto.indexOf(fragmento, desde + 1)) {
            cantidad++;
        }
        return cantidad;
    }

    private static void assertContiene(Respuesta r, String fragmento) {
        assertTrue(r.cuerpo().contains(fragmento),
                "Se esperaba encontrar " + fragmento + " en la respuesta:\n" + r.cuerpo());
    }
}
