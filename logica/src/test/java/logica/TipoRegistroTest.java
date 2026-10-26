package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TipoRegistroTest {

    @Test
    void obtenerDTConservaLosDatosDelTipoDeRegistro() {
        TipoRegistro tipo = new TipoRegistro("General", "Acceso a todas las charlas", 500.0, 80);

        DTTipoRegistro resultado = tipo.obtenerDT();

        assertEquals("General", resultado.getNombre());
        assertEquals("Acceso a todas las charlas", resultado.getDescripcion());
        assertEquals(500.0, resultado.getCosto());
        assertEquals(80, resultado.getCupo());
    }
}
