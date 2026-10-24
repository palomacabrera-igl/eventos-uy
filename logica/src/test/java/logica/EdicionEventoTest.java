package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class EdicionEventoTest {

    @Test
    void unaEdicionIngresadaPuedeAceptarseUnaSolaVez() throws ReglaNegocioException {
        EdicionEvento edicion = nuevaEdicion();

        edicion.aceptar();

        assertEquals(EstadoEdicion.ACEPTADA, edicion.getEstado());
        assertThrows(ReglaNegocioException.class, edicion::aceptar);
        assertThrows(ReglaNegocioException.class, edicion::rechazar);
    }

    @Test
    void unaEdicionIngresadaPuedeRechazarseUnaSolaVez() throws ReglaNegocioException {
        EdicionEvento edicion = nuevaEdicion();

        edicion.rechazar();

        assertEquals(EstadoEdicion.RECHAZADA, edicion.getEstado());
        assertThrows(ReglaNegocioException.class, edicion::aceptar);
    }

    private EdicionEvento nuevaEdicion() {
        LocalDate inicio = LocalDate.of(2027, 5, 10);
        return new EdicionEvento("Jornada de pruebas", "JP", inicio,
                inicio.plusDays(1), LocalDate.of(2026, 10, 24),
                "Montevideo", "Uruguay", null);
    }
}
