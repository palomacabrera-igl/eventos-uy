package logica;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Pruebas de Alta de Evento (CU 6) sobre la entidad, sin base de datos. El
 * nombre repetido y que las categorias existan se prueban contra la base, en
 * AltaEventoIntegracionTest.
 *
 * <pre>
 *  Evento.validarDatos(nombre, descripcion, sigla, categorias)
 *  Clase de equivalencia                         Valores
 *  --------------------------------------------  ------------------------------
 *  CV  todos los datos y alguna categoria        "JIAP", "Jornadas", "JIAP", [Tecnología]
 *  CI  nombre, descripcion o sigla vacio         "" o "   ", de a uno por vez
 *  CI  sin categorias                            null, lista vacia, solo vacias
 *  Valor limite: exactamente una categoria       [Tecnología]
 * </pre>
 */
@DisplayName("Alta de Evento (CU 6)")
class AltaEventoTest {

    private static final List<String> UNA = List.of("Tecnología");

    @Test
    @DisplayName("con todos los datos y varias categorias se acepta")
    void datosValidos() {
        assertDoesNotThrow(() -> Evento.validarDatos("JIAP", "Jornadas de informatica",
                "JIAP", List.of("Tecnología", "Cultura")));
    }

    @Test
    @DisplayName("valor limite: con una sola categoria se acepta")
    void unaSolaCategoria() {
        assertDoesNotThrow(() -> Evento.validarDatos("JIAP", "Jornadas", "JIAP", UNA));
    }

    @ParameterizedTest(name = "nombre=\"{0}\" descripcion=\"{1}\" sigla=\"{2}\"")
    @DisplayName("un dato de texto vacio, de a uno por vez, se rechaza")
    @CsvSource({
            "'',   Jornadas, JIAP",
            "JIAP, '',       JIAP",
            "JIAP, Jornadas, ''",
            "'  ', Jornadas, JIAP"
    })
    void textoVacio(String nombre, String descripcion, String sigla) {
        assertThrows(ReglaNegocioException.class,
                () -> Evento.validarDatos(nombre, descripcion, sigla, UNA));
    }

    @Test
    @DisplayName("sin categorias (null) se rechaza")
    void categoriasNull() {
        assertThrows(ReglaNegocioException.class,
                () -> Evento.validarDatos("JIAP", "Jornadas", "JIAP", null));
    }

    @Test
    @DisplayName("con la lista de categorias vacia se rechaza")
    void categoriasVacias() {
        assertThrows(ReglaNegocioException.class,
                () -> Evento.validarDatos("JIAP", "Jornadas", "JIAP", List.of()));
    }

    @Test
    @DisplayName("con categorias que son todas texto vacio se rechaza")
    void categoriasEnBlanco() {
        assertThrows(ReglaNegocioException.class,
                () -> Evento.validarDatos("JIAP", "Jornadas", "JIAP", Arrays.asList("", "  ", null)));
    }
}
