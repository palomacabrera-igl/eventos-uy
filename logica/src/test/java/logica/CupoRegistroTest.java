package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CupoRegistroTest {

    @Test
    void noHayCupoCuandoLosRegistrosAlcanzanElLimiteDelTipo() {
        EdicionEvento edicion = nuevaEdicion();
        TipoRegistro general = edicion.crearTipoRegistro("General", "Acceso general", 500.0, 1);
        Asistente asistente = new Asistente("ana", "Ana", "ana@mail.com", "Pérez",
                LocalDate.of(2000, 3, 15));

        assertTrue(edicion.hayCupo(general));

        Registro registro = edicion.altaRegistro(asistente, general, LocalDate.of(2026, 10, 24));

        assertEquals(500.0, registro.getCosto());
        assertEquals(1, asistente.getRegistros().size());
        assertFalse(edicion.hayCupo(general));
    }

    private EdicionEvento nuevaEdicion() {
        LocalDate inicio = LocalDate.of(2027, 5, 10);
        return new EdicionEvento("Jornada de pruebas", "JP", inicio,
                inicio.plusDays(1), LocalDate.of(2026, 10, 24),
                "Montevideo", "Uruguay", null);
    }
}
