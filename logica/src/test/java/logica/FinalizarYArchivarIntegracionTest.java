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
    @DisplayName("un organizador finaliza un evento y ya no admite ediciones ni aparece al público")
    void finalizarEvento() throws ReglaNegocioException {
        sistema.finalizarEvento(OTRO_ORGANIZADOR, EVENTO);

        assertTrue(sistema.listarEventos(null).stream()
                .noneMatch(evento -> EVENTO.equals(evento.getNombre())));
        assertThrows(ReglaNegocioException.class, () -> sistema.consultarEvento(EVENTO));
        assertThrows(ReglaNegocioException.class, () -> altaEdicion(EDICION,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));
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

    @Test
    @DisplayName("no se archivan ediciones ingresadas ni ediciones que no terminaron")
    void archivarRechazaEstadoYFecha() throws ReglaNegocioException {
        altaEdicion(EDICION, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        assertThrows(ReglaNegocioException.class, () -> sistema.archivarEdicion(DUENIO, EDICION));

        sistema.aceptarEdicion(EDICION);
        assertThrows(ReglaNegocioException.class, () -> sistema.archivarEdicion(DUENIO, EDICION));
    }
}
