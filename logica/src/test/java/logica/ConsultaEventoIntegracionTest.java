package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Consulta de Evento (CU 7) contra la base, ejercitando Sistema con los datos
 * de prueba. Solo lee: no crea ni borra nada.
 *
 * <pre>
 *  listarEventos(categoria)
 *  CV  null o vacio                  -&gt; la cartelera completa
 *  CV  una categoria con eventos     -&gt; solo los de esa categoria
 *  CI  una categoria que no existe   -&gt; conjunto vacio, no error
 *
 *  consultarEvento(nombre)
 *  CV  un evento que existe          -&gt; sus datos
 *  CI  uno que no existe, vacio o null -&gt; ReglaNegocioException
 *
 *  listarEdicionesAceptadas(evento)
 *  CV  JIAP, con una edicion en cada estado -&gt; solo la Aceptada
 *  CI  un evento que no existe       -&gt; ReglaNegocioException
 * </pre>
 */
@DisplayName("Consulta de Evento contra la base (CU 7)")
class ConsultaEventoIntegracionTest extends PruebaConBase {

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    // ----- listarEventos(categoria) -----

    @ParameterizedTest(name = "categoria \"{0}\"")
    @DisplayName("sin categoria devuelve la cartelera completa")
    @NullAndEmptySource
    void sinCategoria(String categoria) {
        assertEquals(nombres(sistema.listarEventos()), nombres(sistema.listarEventos(categoria)));
    }

    @Test
    @DisplayName("con una categoria devuelve solo sus eventos")
    void porCategoria() {
        Set<DTEvento> deporte = sistema.listarEventos("Deporte");

        assertTrue(nombres(deporte).contains("Maratón de Montevideo"));
        for (DTEvento e : deporte) {
            assertTrue(e.getCategorias().contains("Deporte"), e.getNombre());
        }
    }

    @Test
    @DisplayName("una categoria que no existe no es un error: no tiene eventos")
    void categoriaInexistente() {
        assertTrue(sistema.listarEventos("No existe").isEmpty());
    }

    // ----- consultarEvento(nombre) -----

    @Test
    @DisplayName("un evento que existe devuelve sus datos")
    void eventoExistente() throws ReglaNegocioException {
        DTEvento jiap = sistema.consultarEvento("JIAP");

        assertEquals("JIAP", jiap.getSigla());
        assertTrue(jiap.getCategorias().contains("Tecnología"));
    }

    @ParameterizedTest(name = "nombre \"{0}\"")
    @DisplayName("un evento que no existe, vacio o null se rechaza")
    @NullAndEmptySource
    @ValueSource(strings = {"No existe"})
    void eventoInexistente(String nombre) {
        assertThrows(ReglaNegocioException.class, () -> sistema.consultarEvento(nombre));
    }

    // ----- listarEdicionesAceptadas(evento) -----

    @Test
    @DisplayName("de JIAP solo se publica la Aceptada, no la Ingresada ni la Rechazada")
    void soloAceptadas() throws ReglaNegocioException {
        Set<DTEdicionEvento> publicas = sistema.listarEdicionesAceptadas("JIAP");
        List<String> nombres = publicas.stream().map(DTEdicionEvento::getNombre).toList();

        assertTrue(nombres.contains("JIAP 2026"));
        assertFalse(nombres.contains("JIAP 2027"));
        assertFalse(nombres.contains("JIAP 2025"));
        for (DTEdicionEvento ed : publicas) {
            assertEquals(EstadoEdicion.ACEPTADA, ed.getEstado());
        }
    }

    @Test
    @DisplayName("las ediciones de un evento que no existe se rechazan")
    void edicionesDeEventoInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarEdicionesAceptadas("No existe"));
    }

    private static Set<String> nombres(Set<DTEvento> eventos) {
        return eventos.stream().map(DTEvento::getNombre)
                .collect(java.util.stream.Collectors.toSet());
    }
}
