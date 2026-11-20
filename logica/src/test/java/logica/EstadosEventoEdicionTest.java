package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Estados finales de una edición")
class EstadosEventoEdicionTest {

    private EdicionEvento edicionAceptada(LocalDate fin) throws ReglaNegocioException {
        EdicionEvento edicion = new EdicionEvento("Prueba", "P", fin.minusDays(1), fin,
                LocalDate.of(2026, 1, 1), "Montevideo", "Uruguay",
                new Organizador("org", "Org", "org@mail.com", "Prueba", null));
        edicion.aceptar();
        return edicion;
    }

    @Test
    @DisplayName("una edición aceptada y terminada se archiva con la fecha de hoy")
    void archivaUnaEdicionTerminada() throws ReglaNegocioException {
        LocalDate hoy = LocalDate.of(2026, 11, 19);
        EdicionEvento edicion = edicionAceptada(hoy.minusDays(1));

        assertTrue(edicion.sePuedeArchivar(hoy));
        edicion.archivar(hoy);

        assertEquals(EstadoEdicion.ARCHIVADA, edicion.getEstado());
        assertEquals(hoy, edicion.getFechaArchivado());
        assertFalse(edicion.sePuedeArchivar(hoy));
    }

    @Test
    @DisplayName("una edición que termina hoy o no está aceptada no se puede archivar")
    void rechazaEstadosOFechasInvalidas() throws ReglaNegocioException {
        LocalDate hoy = LocalDate.of(2026, 11, 19);
        EdicionEvento terminaHoy = edicionAceptada(hoy);
        EdicionEvento ingresada = new EdicionEvento("Ingresada", "I", hoy.minusDays(2), hoy.minusDays(1),
                hoy.minusDays(3), "Montevideo", "Uruguay",
                new Organizador("org", "Org", "org@mail.com", "Prueba", null));

        assertThrows(ReglaNegocioException.class, () -> terminaHoy.archivar(hoy));
        assertThrows(ReglaNegocioException.class, () -> ingresada.archivar(hoy));
    }

    @Test
    @DisplayName("un evento finalizado conserva el dato en su datatype")
    void eventoFinalizadoQuedaMarcado() {
        Evento evento = new Evento("Evento", "Descripción", LocalDate.of(2026, 1, 1), "E", List.of());

        evento.finalizar();

        assertTrue(evento.estaFinalizado());
        assertTrue(evento.obtenerDT().getFinalizado());
    }
}
