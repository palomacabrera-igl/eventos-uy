package logica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import persistencia.Persistencia;

/**
 * Registro de Asistencia a Edicion (letra, 6.3) contra la base: lo que cambia
 * se guarda al terminar la operacion, y un rechazo no deja nada a medias.
 *
 * <pre>
 *  Escenario que arma cada prueba, y que borra al terminar:
 *
 *      edicion "Test IA 2027" (Aceptada) del organizador test-ia-org
 *      test-ia-ana y test-ia-beto, registrados en el tipo "General"
 *      test-ia-carlos, que NO se registro
 *
 *  CV  el asistente registrado registra su asistencia   -&gt; queda guardada
 *  CV  el organizador la ve en el listado de registros
 *  CI  la segunda vez                                    -&gt; ReglaNegocioException
 *  CI  quien no se registro, el organizador, la edicion que no existe
 * </pre>
 */
@DisplayName("Registro de Asistencia a Edicion contra la base")
class RegistroAsistenciaIntegracionTest extends PruebaConBase {

    private static final String EDICION = "Test IA 2027";
    private static final String ORGANIZADOR = "test-ia-org";
    private static final String ANA = "test-ia-ana";
    private static final String BETO = "test-ia-beto";
    private static final String CARLOS = "test-ia-carlos";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    void armarElEscenario() throws ReglaNegocioException {
        sinRastros();
        Persistencia.enTransaccion(em -> {
            Categoria categoria = em.createQuery("SELECT c FROM Categoria c", Categoria.class)
                    .getResultStream().findFirst().orElseThrow();

            Organizador organizador = new Organizador(ORGANIZADOR, "Org de prueba",
                    ORGANIZADOR + "@mail.com", "Organiza la edicion de prueba", null, "Clave1!");
            Evento evento = new Evento("Test IA Evento", "Evento de prueba",
                    LocalDate.now(), "TIA", List.of(categoria));
            // Ya empezo y no termino: es cuando un asistente registra que asistio.
            EdicionEvento edicion = new EdicionEvento(EDICION, "TIA27",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                    LocalDate.now(), "Montevideo", "Uruguay", organizador);
            evento.agregarEdicion(edicion);
            organizador.agregarEdicion(edicion);
            edicion.crearTipoRegistro("General", "Entrada general", 1500.0, 10);

            em.persist(organizador);
            em.persist(evento);
            em.persist(asistente(ANA, "Ana"));
            em.persist(asistente(BETO, "Beto"));
            em.persist(asistente(CARLOS, "Carlos"));
        });

        // Una edicion nace Ingresada y a una Ingresada no se registra nadie: se
        // acepta por el caso de uso, como lo hace el administrador.
        sistema.aceptarEdicion(EDICION);
        sistema.altaRegistro(ANA, EDICION, "General");
        sistema.altaRegistro(BETO, EDICION, "General");
    }

    private static Asistente asistente(String nickname, String nombre) {
        return new Asistente(nickname, nombre, nickname + "@mail.com", "Prueba",
                LocalDate.of(2000, 1, 1), "Clave1!");
    }

    @AfterEach
    void sinRastros() {
        // El orden importa: primero lo que apunta, despues lo apuntado.
        borrar("registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "')");
        borrar("tipo_registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "')");
        borrar("edicion_evento", "nombre = '" + EDICION + "'");
        borrar("evento_categoria",
                "evento_id IN (SELECT id FROM evento WHERE nombre = 'Test IA Evento')");
        borrar("evento", "nombre = 'Test IA Evento'");
        borrar("usuario", "nickname LIKE 'test-ia-%'");
    }

    private boolean asistio(String nickname) throws ReglaNegocioException {
        return sistema.consultarRegistro(EDICION, nickname, nickname).getAsistio();
    }

    // ================================================== casos validos

    @Test
    @DisplayName("CV · un registro nuevo todavia no tiene asistencia")
    void alPrincipioNoAsistio() throws ReglaNegocioException {
        assertFalse(asistio(ANA));
    }

    @Test
    @DisplayName("CV · el asistente registrado registra su asistencia y queda guardada")
    void registraLaAsistencia() throws ReglaNegocioException {
        sistema.registrarAsistencia(ANA, EDICION);

        // Cada operacion lee la base de verdad: si se ve, es que se guardo.
        assertTrue(asistio(ANA));
        assertFalse(asistio(BETO));
    }

    @Test
    @DisplayName("CV · el organizador ve en el listado de registros quien asistio")
    void elOrganizadorLaVe() throws ReglaNegocioException {
        sistema.registrarAsistencia(ANA, EDICION);

        var registros = sistema.listarRegistrosDeEdicion(EDICION, ORGANIZADOR);

        assertTrue(registros.stream().filter(r -> ANA.equals(r.getNickname()))
                .findFirst().orElseThrow().getAsistio());
        assertFalse(registros.stream().filter(r -> BETO.equals(r.getNickname()))
                .findFirst().orElseThrow().getAsistio());
    }

    // ================================================== rechazos

    @Test
    @DisplayName("CI · la segunda vez es un error y no se pierde la primera")
    void noSePuedeDosVeces() throws ReglaNegocioException {
        sistema.registrarAsistencia(ANA, EDICION);

        assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(ANA, EDICION));

        assertTrue(asistio(ANA));
    }

    @Test
    @DisplayName("CI · quien no se registro a la edicion no puede registrar asistencia")
    void sinRegistroNoPuede() throws ReglaNegocioException {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(CARLOS, EDICION));
    }

    @Test
    @DisplayName("CI · el organizador de la edicion no es un asistente registrado")
    void elOrganizadorNoPuede() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(ORGANIZADOR, EDICION));
    }

    @Test
    @DisplayName("CI · una edicion que todavia no empezo no admite registrar asistencia")
    void edicionQueNoEmpezo() throws ReglaNegocioException {
        cambiarEdicion("fechaini = CURRENT_DATE + 10, fechafin = CURRENT_DATE + 12");

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(ANA, EDICION));

        assertTrue(error.getMessage().contains("todavía no empezó"));
        assertFalse(asistio(ANA));
    }

    @Test
    @DisplayName("CI · una edicion archivada no admite registrar asistencia")
    void edicionArchivada() throws ReglaNegocioException {
        cambiarEdicion("estado = 'ARCHIVADA'");

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(ANA, EDICION));

        assertTrue(error.getMessage().contains("archivada"));
        assertFalse(asistio(ANA));
    }

    @Test
    @DisplayName("CI · si ya la habia registrado, el mensaje es ese aunque despues se archive")
    void yaRegistradaYArchivada() throws ReglaNegocioException {
        sistema.registrarAsistencia(ANA, EDICION);
        cambiarEdicion("estado = 'ARCHIVADA'");

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(ANA, EDICION));

        assertTrue(error.getMessage().contains("Ya registraste"));
        assertTrue(asistio(ANA));
    }

    /** Cambia la edicion de prueba con SQL directo: asi no depende de casos de uso de otros roles. */
    private static void cambiarEdicion(String asignaciones) {
        Persistencia.enTransaccion(em -> em.createNativeQuery(
                "UPDATE edicion_evento SET " + asignaciones + " WHERE nombre = '" + EDICION + "'")
                .executeUpdate());
    }

    @Test
    @DisplayName("CI · una edicion que no existe, o sin nombre")
    void edicionInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(ANA, "No existe esta edicion"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(ANA, null));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarAsistencia(null, EDICION));
    }
}
