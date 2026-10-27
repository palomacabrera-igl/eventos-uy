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

/**
 * Pruebas de Consulta de Usuario (CU 4), sin base de datos: la regla de quien
 * ve que vive en las entidades y Sistema solo busca y delega.
 *
 * <pre>
 *  obtenerEdicionesPara(solicitante) -- perfil de un organizador
 *  CV  el propio organizador                -&gt; las tres: Ingresada, Aceptada y Rechazada
 *  CI  otro usuario                         -&gt; solo la Aceptada
 *  CI  un visitante sin sesion (null)       -&gt; solo la Aceptada
 *  CI  un nickname parecido pero distinto   -&gt; solo la Aceptada
 *  Valor limite: organizador sin ediciones  -&gt; conjunto vacio
 *
 *  obtenerRegistrosPara(solicitante) -- perfil de un asistente
 *  CV  el propio asistente                  -&gt; sus registros
 *  CI  otro usuario                         -&gt; conjunto vacio, no error
 *  CI  un visitante sin sesion (null)       -&gt; conjunto vacio
 *  Valor limite: asistente sin registros    -&gt; conjunto vacio
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

        // Una edicion en cada estado, que es lo que distingue a este caso de uso.
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

    // ----- Las ediciones de un organizador -----

    @Test
    @DisplayName("el organizador ve sus tres ediciones en su propio perfil")
    void elDuenioVeLasTres() {
        assertEquals(Set.of("Ingresada 2027", "Aceptada 2027", "Rechazada 2027"),
                nombresDeEdiciones("imm"));
    }

    @Test
    @DisplayName("otro usuario ve solo la Aceptada")
    void otroVeSoloLaAceptada() {
        assertEquals(Set.of("Aceptada 2027"), nombresDeEdiciones("vale23"));
    }

    @Test
    @DisplayName("un visitante sin sesion ve solo la Aceptada")
    void visitanteVeSoloLaAceptada() {
        assertEquals(Set.of("Aceptada 2027"), nombresDeEdiciones(null));
    }

    @Test
    @DisplayName("un nickname parecido no alcanza para ver las tres")
    void nicknameParecidoNoAlcanza() {
        assertEquals(Set.of("Aceptada 2027"), nombresDeEdiciones("imm2"));
        assertEquals(Set.of("Aceptada 2027"), nombresDeEdiciones("IMM"));
    }

    @Test
    @DisplayName("los espacios de mas no cambian quien ve que")
    void losEspaciosNoDeciden() {
        assertEquals(Set.of("Ingresada 2027", "Aceptada 2027", "Rechazada 2027"),
                nombresDeEdiciones("  imm  "));
    }

    @Test
    @DisplayName("valor limite: un organizador sin ediciones no muestra ninguna")
    void organizadorSinEdiciones() {
        Organizador nuevo = new Organizador("antel", "Antel", "eventos@antel.com.uy",
                "Telecomunicaciones", null, "Antel2026!");
        assertTrue(nuevo.obtenerEdicionesPara("antel").isEmpty());
    }

    // ----- Los registros de un asistente -----

    @Test
    @DisplayName("el asistente ve sus registros en su propio perfil")
    void elDuenioVeSusRegistros() {
        Set<DTRegistro> registros = asistente.obtenerRegistrosPara("vale23");
        assertEquals(1, registros.size());
        assertEquals("Aceptada 2027", registros.iterator().next().getNombreEdicion());
    }

    @Test
    @DisplayName("otro usuario no ve ningun registro, y no es un error")
    void otroNoVeLosRegistros() {
        assertTrue(asistente.obtenerRegistrosPara("imm").isEmpty());
    }

    @Test
    @DisplayName("un visitante sin sesion no ve ningun registro")
    void visitanteNoVeLosRegistros() {
        assertTrue(asistente.obtenerRegistrosPara(null).isEmpty());
    }

    @Test
    @DisplayName("valor limite: un asistente sin registros devuelve el conjunto vacio")
    void asistenteSinRegistros() {
        Asistente nuevo = new Asistente("jperez", "Juan", "juan@mail.com",
                "Perez", LocalDate.of(1995, 1, 1), "Juan2026!");
        assertTrue(nuevo.obtenerRegistrosPara("jperez").isEmpty());
    }

    // ----- El perfil en si -----

    @Test
    @DisplayName("el DT del perfil lleva el tipo y no lleva la contrasena")
    void elPerfilNoLlevaLaContrasena() {
        DTUsuario perfil = organizador.obtenerDT();
        assertTrue(perfil.esOrganizador());
        assertFalse(perfil.esAsistente());
        assertEquals("imm", perfil.getNickname());

        // DTUsuario no tiene getPassword(): si alguien se lo agrega, esta
        // prueba no compila y se entera antes de publicarlo.
        assertEquals(0, Set.of(DTUsuario.class.getMethods()).stream()
                .filter(m -> m.getName().toLowerCase().contains("password")
                        || m.getName().toLowerCase().contains("contrasena"))
                .count());
    }
}
