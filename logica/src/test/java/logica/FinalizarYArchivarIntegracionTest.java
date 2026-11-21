package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Finalizar eventos y archivar ediciones contra la base")
class FinalizarYArchivarIntegracionTest extends PruebaConBase {

    private static final String PREFIJO = "IT R2 Estado";
    private static final String DUENIO = "it-r2-duenio";
    private static final String OTRO_ORGANIZADOR = "it-r2-otro";
    private static final String EVENTO = PREFIJO + " Evento";
    private static final String EDICION = PREFIJO + " Edicion";

    private final IControladorSistema sistema = Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        borrarRastros();
        altaOrganizador(DUENIO);
        altaOrganizador(OTRO_ORGANIZADOR);
        sistema.altaEvento(DUENIO, EVENTO, "Evento para probar estados", "R2",
                List.of("Tecnología"), null);
    }

    @AfterEach
    void limpiar() {
        borrarRastros();
    }

    private void borrarRastros() {
        borrar("edicion_evento", "nombre LIKE '" + PREFIJO + "%'");
        borrar("evento_categoria", "evento_id IN (SELECT id FROM evento WHERE nombre LIKE '" + PREFIJO + "%')");
        borrar("evento", "nombre LIKE '" + PREFIJO + "%'");
        borrar("usuario", "nickname IN ('" + DUENIO + "', '" + OTRO_ORGANIZADOR
                + "', 'it-r2-asistente')");
    }

    private void altaOrganizador(String nickname) throws ReglaNegocioException {
        sistema.altaUsuario(new DTOrganizador(nickname, "Prueba", nickname + "@mail.com",
                        "Organizador de pruebas", null),
                TipoUsuario.ORGANIZADOR, "Clave1!", "Clave1!", null, null);
    }

    private void altaEdicion(String nombre, LocalDate inicio, LocalDate fin)
            throws ReglaNegocioException {
        sistema.altaEdicion(DUENIO, EVENTO, new DTEdicionEvento(nombre, "R2", "Montevideo",
                "Uruguay", DTFecha.desde(inicio), DTFecha.desde(fin), null), null);
    }

    @Test
    @DisplayName("solo quien creó el evento puede finalizarlo")
    void finalizarEvento() throws ReglaNegocioException {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.finalizarEvento(OTRO_ORGANIZADOR, EVENTO));
        assertEquals(List.of(EVENTO), sistema.listarEventosFinalizables(DUENIO).stream()
                .map(DTEvento::getNombre).toList());

        sistema.finalizarEvento(DUENIO, EVENTO);

        assertTrue(sistema.listarEventos(null).stream()
                .noneMatch(evento -> EVENTO.equals(evento.getNombre())));
        assertThrows(ReglaNegocioException.class, () -> sistema.consultarEvento(EVENTO));
        assertThrows(ReglaNegocioException.class, () -> altaEdicion(EDICION,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));
        assertTrue(sistema.listarEventosFinalizables(DUENIO).isEmpty());
        assertThrows(ReglaNegocioException.class, () -> sistema.finalizarEvento(DUENIO, EVENTO));
    }

    @Test
    @DisplayName("un asistente no puede finalizar eventos")
    void soloOrganizadorFinaliza() throws ReglaNegocioException {
        sistema.altaUsuario(new DTAsistente("it-r2-asistente", "Prueba", "it-r2-asistente@mail.com",
                        "Asistente", new DTFecha(1, 1, 2000), null),
                TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", null, null);

        assertThrows(ReglaNegocioException.class, () -> sistema.finalizarEvento("it-r2-asistente", EVENTO));
    }

    @Test
    @DisplayName("solo el dueño archiva una edición aceptada que ya terminó")
    void archivarEdicion() throws ReglaNegocioException {
        altaEdicion(EDICION, LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));
        sistema.aceptarEdicion(EDICION);

        assertEquals(List.of(EDICION), sistema.listarEdicionesArchivables(DUENIO).stream()
                .map(DTEdicionEvento::getNombre).toList());
        assertThrows(ReglaNegocioException.class,
                () -> sistema.archivarEdicion(OTRO_ORGANIZADOR, EDICION));

        sistema.archivarEdicion(DUENIO, EDICION);

        assertEquals(EstadoEdicion.ARCHIVADA,
                sistema.consultarEdicion(EDICION, DUENIO).getEstado());
        assertTrue(sistema.listarEdicionesArchivables(DUENIO).isEmpty());
        assertThrows(ReglaNegocioException.class, () -> sistema.consultarEdicion(EDICION, null));
    }

    // ----- Lo que encontro la auditoria del 21/11 -----

    @Test
    @DisplayName("un evento de la Estacion queda a cargo del organizador elegido, que lo puede finalizar")
    void eventoDeLaEstacionTieneResponsable() throws ReglaNegocioException {
        String deLaEstacion = PREFIJO + " Estacion";

        // Sin organizador, o con alguien que no es organizador, no se crea:
        // si no, el evento no lo podria finalizar nadie.
        assertThrows(ReglaNegocioException.class, () -> sistema.ingresarDatosEvento(deLaEstacion,
                "Desde la Estacion", LocalDate.now(), "R2E", List.of("Tecnología"), null));
        assertThrows(ReglaNegocioException.class, () -> sistema.ingresarDatosEvento(deLaEstacion,
                "Desde la Estacion", LocalDate.now(), "R2E", List.of("Tecnología"), "no-existe"));

        sistema.ingresarDatosEvento(deLaEstacion, "Desde la Estacion", LocalDate.now(), "R2E",
                List.of("Tecnología"), DUENIO);

        assertTrue(sistema.listarEventosFinalizables(DUENIO).stream()
                .anyMatch(evento -> deLaEstacion.equals(evento.getNombre())));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.finalizarEvento(OTRO_ORGANIZADOR, deLaEstacion));
        sistema.finalizarEvento(DUENIO, deLaEstacion);
        assertTrue(sistema.listarEventos(null).stream()
                .noneMatch(evento -> deLaEstacion.equals(evento.getNombre())));
    }

    @Test
    @DisplayName("la Estacion tampoco le agrega ediciones a un evento finalizado")
    void estacionNoAgregaEdicionesAFinalizado() throws ReglaNegocioException {
        sistema.finalizarEvento(DUENIO, EVENTO);

        sistema.seleccionarEvento(EVENTO);
        sistema.seleccionarOrganizador(DUENIO);
        assertThrows(ReglaNegocioException.class, () -> sistema.ingresarDatosEdicion(
                new DTEdicionEvento(EDICION, "R2", "Montevideo", "Uruguay",
                        DTFecha.desde(LocalDate.now().plusDays(1)),
                        DTFecha.desde(LocalDate.now().plusDays(2)),
                        DTFecha.desde(LocalDate.now()))));
    }

    @Test
    @DisplayName("una edicion archivada no admite tipos de registro ni patrocinios nuevos")
    void archivadaNoAdmiteAgregados() throws ReglaNegocioException {
        altaEdicion(EDICION, LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));
        sistema.aceptarEdicion(EDICION);
        sistema.archivarEdicion(DUENIO, EDICION);

        ReglaNegocioException tipo = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaTipoRegistro(DUENIO, EDICION, "General", "Entrada", 100, 10));
        assertTrue(tipo.getMessage().contains("archivada"));

        ReglaNegocioException patrocinio = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio(DUENIO, EDICION, new DTPatrocinio("R2COD",
                        (DTFecha) null, 1000, "ORO", 0, "UTEC", "General")));
        assertTrue(patrocinio.getMessage().contains("archivada"));
    }

    @Test
    @DisplayName("no se archivan ediciones ingresadas ni ediciones que no terminaron")
    void archivarRechazaEstadoYFecha() throws ReglaNegocioException {
        altaEdicion(EDICION, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        assertThrows(ReglaNegocioException.class, () -> sistema.archivarEdicion(DUENIO, EDICION));

        sistema.aceptarEdicion(EDICION);
        assertThrows(ReglaNegocioException.class, () -> sistema.archivarEdicion(DUENIO, EDICION));
    }
}
