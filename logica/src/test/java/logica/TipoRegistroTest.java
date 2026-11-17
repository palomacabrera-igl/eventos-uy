package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Pruebas de caja negra de {@link TipoRegistro}.
 *
 * <p>La letra, CU 10: «El organizador elige una [edicion] e ingresa nombre,
 * descripcion, costo y cupo».
 *
 * <pre>
 *  hayCupo(tipo) -- visto desde la edicion que lo contiene
 *
 *  Clase de equivalencia       Registros hechos   Hay cupo
 *  --------------------------  -----------------  ---------
 *  CV  queda lugar             menos que el cupo  si
 *  CI  cupo agotado            el cupo entero     no
 *
 *  Valor limite: con cupo 2, el ultimo lugar (1 -&gt; si, 2 -&gt; no)
 * </pre>
 */
@DisplayName("Tipo de Registro")
class TipoRegistroTest {

    private EdicionEvento edicion;

    @BeforeEach
    void crearEdicion() {
        Organizador organizador = new Organizador("imm", "Intendencia",
                "eventos@imm.gub.uy", "Organiza eventos", null, "Imm2026!");
        edicion = new EdicionEvento("Jornada de pruebas", "JP",
                LocalDate.of(2027, 5, 10), LocalDate.of(2027, 5, 11),
                LocalDate.of(2026, 10, 28), "Montevideo", "Uruguay", organizador);
    }

    private Asistente asistente(String nickname) {
        return new Asistente(nickname, nickname, nickname + "@mail.com",
                "Prueba", LocalDate.of(2000, 1, 1));
    }

    // ------------------------------------------- lo que lleva el DT

    @Test
    @DisplayName("el DT conserva el nombre, la descripcion, el costo y el cupo")
    void elDtLlevaLosDatos() {
        TipoRegistro tipo = new TipoRegistro("General", "Acceso a todas las charlas", 500.0, 80);

        DTTipoRegistro dt = tipo.obtenerDT();

        assertEquals("General", dt.getNombre());
        assertEquals("Acceso a todas las charlas", dt.getDescripcion());
        assertEquals(500.0, dt.getCosto());
        assertEquals(80, dt.getCupo());
    }

    @Test
    @DisplayName("los datos se pueden modificar y el DT refleja el cambio")
    void modificarLosDatos() {
        TipoRegistro tipo = new TipoRegistro("General", "Acceso general", 500.0, 80);

        tipo.setNombre("General Plus");
        tipo.setDescripcion("Acceso general y taller");
        tipo.setCosto(900.0);
        tipo.setCupo(40);

        DTTipoRegistro dt = tipo.obtenerDT();
        assertEquals("General Plus", dt.getNombre());
        assertEquals("Acceso general y taller", dt.getDescripcion());
        assertEquals(900.0, dt.getCosto());
        assertEquals(40, dt.getCupo());
    }

    @Test
    @DisplayName("un tipo creado desde la edicion queda atado a ella")
    void creadoDesdeLaEdicion() {
        TipoRegistro general = edicion.crearTipoRegistro("General", "Acceso general", 500.0, 80);

        assertSame(edicion, general.getEdicion());
        assertSame(general, edicion.buscarTipoRegistro("General"));
        assertNull(edicion.buscarTipoRegistro("No Existe"));
    }

    // ------------------------------------------------------- el cupo

    @ParameterizedTest(name = "cupo {0}, con {1} registros hechos -> queda lugar: {2}")
    @DisplayName("el cupo se agota al llegar al limite del tipo")
    @CsvSource({
            "2, 0, true",   // CV: nadie se anoto
            "2, 1, true",   // Valor limite: queda el ultimo lugar
            "2, 2, false",  // Valor limite: justo se agoto
            "1, 1, false"   // CI: cupo de uno, ya usado
    })
    void elCupoSeAgota(int cupo, int registros, boolean quedaLugar) {
        // Datos de prueba
        TipoRegistro tipo = edicion.crearTipoRegistro("General", "Acceso general", 500.0, cupo);

        // Ejecucion: se anotan tantos asistentes como dice la fila
        for (int i = 0; i < registros; i++) {
            edicion.altaRegistro(asistente("asis" + i), tipo, LocalDate.of(2026, 10, 28));
        }

        // Resultado esperado
        assertEquals(quedaLugar, edicion.hayCupo(tipo));
    }

    @Test
    @DisplayName("el registro guarda el costo del tipo y queda del lado del asistente")
    void elRegistroGuardaElCosto() {
        TipoRegistro general = edicion.crearTipoRegistro("General", "Acceso general", 500.0, 10);
        Asistente ana = asistente("ana");

        Registro registro = edicion.altaRegistro(ana, general, LocalDate.of(2026, 10, 28));

        assertEquals(500.0, registro.getCosto());
        assertEquals(1, ana.getRegistros().size());
        assertTrue(edicion.estaRegistrado("ana"));
        assertFalse(edicion.estaRegistrado("otro"));
    }
}
