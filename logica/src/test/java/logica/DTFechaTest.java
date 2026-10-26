package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DTFechaTest {

    @Test
    void convierteUnaFechaEnAmbosSentidos() {
        LocalDate fecha = LocalDate.of(2026, 10, 22);

        DTFecha dto = DTFecha.desde(fecha);

        assertEquals(22, dto.getDia());
        assertEquals(10, dto.getMes());
        assertEquals(2026, dto.getAnio());
        assertEquals(fecha, dto.aLocalDate());
    }
}
