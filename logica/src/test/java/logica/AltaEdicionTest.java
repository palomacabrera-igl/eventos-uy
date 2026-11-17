package logica;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Pruebas de Alta de Edicion (CU 8), sin base de datos. El nombre repetido no
 * se prueba aca: lo controla Sistema buscando en la base.
 *
 * <pre>
 *  validarDatos(nombre, sigla, ciudad, pais, inicio, fin)
 *  CV  todos los datos, fin despues del inicio            -&gt; se acepta
 *  CI  nombre, sigla, ciudad o pais vacio (de a uno)      -&gt; se rechaza
 *  CI  sin fecha de inicio o sin fecha de fin             -&gt; se rechaza
 *  CI  fin anterior al inicio                             -&gt; se rechaza
 *  Valor limite: fin el mismo dia que el inicio           -&gt; se acepta
 *  Valor limite: fin un dia antes del inicio              -&gt; se rechaza
 *
 *  Evento.altaEdicion(dt, organizador)
 *  La edicion nace INGRESADA, queda del organizador que la dio de alta y no
 *  se publica hasta que la acepten.
 * </pre>
 */
@DisplayName("Alta de Edicion (CU 8)")
class AltaEdicionTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 5, 10);

    // ----- validarDatos -----

    @Test
    @DisplayName("con todos los datos y el fin despues del inicio se acepta")
    void datosValidos() {
        assertDoesNotThrow(() -> EdicionEvento.validarDatos("Maratón 2027", "M27",
                "Montevideo", "Uruguay", INICIO, INICIO.plusDays(2)));
    }

    @ParameterizedTest(name = "nombre=\"{0}\" sigla=\"{1}\" ciudad=\"{2}\" pais=\"{3}\"")
    @DisplayName("un dato de texto vacio, de a uno por vez, se rechaza")
    @CsvSource({
            "'',           M27, Montevideo, Uruguay",
            "Maratón 2027, '',  Montevideo, Uruguay",
            "Maratón 2027, M27, '',         Uruguay",
            "Maratón 2027, M27, Montevideo, ''",
            "'   ',        M27, Montevideo, Uruguay"
    })
    void datoDeTextoVacio(String nombre, String sigla, String ciudad, String pais) {
        assertThrows(ReglaNegocioException.class, () -> EdicionEvento.validarDatos(
                nombre, sigla, ciudad, pais, INICIO, INICIO.plusDays(1)));
    }

    @Test
    @DisplayName("sin fecha de inicio o sin fecha de fin se rechaza")
    void sinFechas() {
        assertThrows(ReglaNegocioException.class, () -> EdicionEvento.validarDatos(
                "Maratón 2027", "M27", "Montevideo", "Uruguay", null, INICIO));
        assertThrows(ReglaNegocioException.class, () -> EdicionEvento.validarDatos(
                "Maratón 2027", "M27", "Montevideo", "Uruguay", INICIO, null));
    }

    @Test
    @DisplayName("valor limite: fin el mismo dia que el inicio se acepta (edicion de un dia)")
    void finElMismoDia() {
        assertDoesNotThrow(() -> EdicionEvento.validarDatos("Maratón 2027", "M27",
                "Montevideo", "Uruguay", INICIO, INICIO));
    }

    @Test
    @DisplayName("valor limite: fin un dia antes del inicio se rechaza")
    void finUnDiaAntes() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> EdicionEvento.validarDatos("Maratón 2027", "M27",
                        "Montevideo", "Uruguay", INICIO, INICIO.minusDays(1)));
        assertEquals("La fecha de fin no puede ser anterior a la de inicio.", e.getMessage());
    }

    // ----- La edicion creada -----

    @Test
    @DisplayName("la edicion nace Ingresada, es de quien la dio de alta y no se publica")
    void naceIngresadaYDelOrganizador() {
        // Datos de prueba
        Organizador imm = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos", null, "Imm2026!");
        Evento evento = new Evento("Maratón de Montevideo", "Carrera anual",
                LocalDate.of(2024, 3, 5), "MARA", List.of(new Categoria("Deporte")));
        DTEdicionEvento datos = new DTEdicionEvento("Maratón 2027", "M27", "Montevideo",
                "Uruguay", DTFecha.desde(INICIO), DTFecha.desde(INICIO),
                DTFecha.desde(LocalDate.of(2026, 10, 28)));

        // Ejecucion
        EdicionEvento nueva = evento.altaEdicion(datos, imm);

        // Resultado esperado y comparacion
        assertEquals(EstadoEdicion.INGRESADA, nueva.getEstado());
        assertSame(imm, nueva.getOrganizador());
        assertSame(evento, nueva.getEvento());
        assertEquals(1, evento.obtenerEdiciones().size());
        assertTrue(evento.obtenerEdicionesAceptadas().isEmpty());
    }
}
