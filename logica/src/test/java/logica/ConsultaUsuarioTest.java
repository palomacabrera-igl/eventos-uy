package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de caja negra de la Consulta de Usuario (CU 4): se construyen solo
 * a partir de la especificacion de la letra. No necesitan base de datos,
 * porque la regla de quien ve que vive en las entidades.
 *
 * <p>La letra, CU 4: «El organizador puede abrir, desde su perfil, el detalle
 * de sus ediciones Ingresada y Rechazada; ese detalle no esta disponible para
 * el resto de los usuarios».
 *
 * <pre>
 *  obtenerEdicionesPara(solicitante) -- perfil de un organizador
 *
 *  La entrada es quien esta mirando el perfil. Las clases salen de la
 *  relacion entre ese dato y el duenio del perfil, no de su valor.
 *
 *  Clase de equivalencia              Ejemplos            Que ve
 *  ---------------------------------  ------------------  -------------------
 *  CV  es el duenio del perfil        "imm", "  imm  "    las tres
 *  CV  es otro usuario                "vale23", "antel"   solo las Aceptada
 *  CV  no hay sesion                  null                solo las Aceptada
 *  CI  se parece pero no es           "IMM", "imm2"       solo las Aceptada
 *
 *  Valor limite: un organizador sin ninguna edicion -> conjunto vacio
 *
 *  obtenerRegistrosPara(solicitante) -- perfil de un asistente
 *  CV  es el duenio del perfil        -> sus registros
 *  CV  cualquier otro, o nadie        -> conjunto vacio, y no un error
 *  Valor limite: un asistente sin registros -> conjunto vacio
 * </pre>
 */
@DisplayName("Consulta de Usuario (CU 4)")
class ConsultaUsuarioTest {

    private Organizador organizador;
    private Asistente asistente;

    @BeforeEach
    void crearPerfiles() throws ReglaNegocioException {
        organizador = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos de la ciudad", "https://montevideo.gub.uy", "Imm2026!");

        // Datos de prueba: una edicion en cada estado, que es lo que
        // distingue a este caso de uso.
        organizador.agregarEdicion(edicion("Ingresada 2027"));

        EdicionEvento aceptada = edicion("Aceptada 2027");
        aceptada.aceptar();
        organizador.agregarEdicion(aceptada);

        EdicionEvento rechazada = edicion("Rechazada 2027");
        rechazada.rechazar();
        organizador.agregarEdicion(rechazada);

        asistente = new Asistente("vale23", "Valentina", "valentina@mail.com",
                "Pereira", LocalDate.of(2000, 5, 10), "Vale2026!");
        asistente.agregarRegistro(new Registro(asistente, aceptada,
                new TipoRegistro("General", "Entrada general", 500.0, 100),
                500.0, LocalDate.of(2026, 10, 1)));
    }

    private EdicionEvento edicion(String nombre) {
        return new EdicionEvento(nombre, "ED",
                LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 5),
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", organizador);
    }

    private Set<String> nombresDeEdiciones(String solicitante) {
        return organizador.obtenerEdicionesPara(solicitante).stream()
                .map(DTEdicionEvento::getNombre)
                .collect(Collectors.toSet());
    }

    // ------------------------------- las ediciones de un organizador

    @ParameterizedTest(name = "lo mira \"{0}\"")
    @DisplayName("el duenio del perfil ve sus tres ediciones, con o sin espacios de mas")
    @ValueSource(strings = {"imm", "  imm  ", "imm  "})
    void elDuenioVeLasTres(String solicitante) {
        assertEquals(Set.of("Ingresada 2027", "Aceptada 2027", "Rechazada 2027"),
                nombresDeEdiciones(solicitante));
    }

    @ParameterizedTest(name = "lo mira \"{0}\"")
    @DisplayName("cualquier otro ve solo la Aceptada")
    @ValueSource(strings = {"vale23", "antel", "IMM", "imm2", "im", ""})
    void losDemasVenSoloLaAceptada(String solicitante) {
        assertEquals(Set.of("Aceptada 2027"), nombresDeEdiciones(solicitante));
    }

    @ParameterizedTest(name = "sin sesion")
    @DisplayName("un visitante sin sesion ve solo la Aceptada")
    @NullSource
    void visitanteVeSoloLaAceptada(String sinSesion) {
        assertEquals(Set.of("Aceptada 2027"), nombresDeEdiciones(sinSesion));
    }

    @Test
    @DisplayName("valor limite: un organizador sin ediciones no muestra ninguna")
    void organizadorSinEdiciones() {
        Organizador nuevo = new Organizador("antel", "Antel", "eventos@antel.com.uy",
                "Telecomunicaciones", null, "Antel2026!");

        assertTrue(nuevo.obtenerEdicionesPara("antel").isEmpty());
    }

    // --------------------------------- los registros de un asistente

    @Test
    @DisplayName("el asistente ve sus registros en su propio perfil")
    void elDuenioVeSusRegistros() {
        // Datos de prueba: vale23, con un registro a "Aceptada 2027"
        // Ejecucion
        Set<DTRegistro> registros = asistente.obtenerRegistrosPara("vale23");

        // Resultado esperado
        assertEquals(1, registros.size());
        assertEquals("Aceptada 2027", registros.iterator().next().getNombreEdicion());
    }

    @ParameterizedTest(name = "los mira \"{0}\"")
    @DisplayName("cualquier otro no ve ningun registro, y no es un error")
    @ValueSource(strings = {"imm", "antel", "VALE23", "vale"})
    void losDemasNoVenLosRegistros(String solicitante) {
        assertTrue(asistente.obtenerRegistrosPara(solicitante).isEmpty());
    }

    @ParameterizedTest(name = "sin sesion")
    @DisplayName("un visitante sin sesion no ve ningun registro")
    @NullSource
    void visitanteNoVeLosRegistros(String sinSesion) {
        assertTrue(asistente.obtenerRegistrosPara(sinSesion).isEmpty());
    }

    @Test
    @DisplayName("valor limite: un asistente sin registros devuelve el conjunto vacio")
    void asistenteSinRegistros() {
        Asistente nuevo = new Asistente("jperez", "Juan", "juan@mail.com",
                "Perez", LocalDate.of(1995, 1, 1), "Juan2026!");

        assertTrue(nuevo.obtenerRegistrosPara("jperez").isEmpty());
    }

    // ------------------------------------------------ el perfil en si

    @Test
    @DisplayName("el DT del perfil lleva el tipo y no lleva la contrasena")
    void elPerfilNoLlevaLaContrasena() {
        DTUsuario perfil = organizador.obtenerDT();

        assertTrue(perfil.esOrganizador());
        assertFalse(perfil.esAsistente());
        assertEquals("imm", perfil.getNickname());

        // DTUsuario no tiene getPassword(): si alguien se lo agrega, esta
        // prueba falla y se entera antes de publicarlo.
        assertEquals(0, Set.of(DTUsuario.class.getMethods()).stream()
                .filter(m -> m.getName().toLowerCase().contains("password")
                        || m.getName().toLowerCase().contains("contrasena"))
                .count());
    }
}
