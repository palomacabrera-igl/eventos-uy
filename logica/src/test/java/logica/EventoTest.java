package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de Consulta de Evento (CU 7) sobre la entidad Evento, sin base de
 * datos: las reglas viven en la entidad y Sistema solo busca y delega.
 *
 * Caja negra, con clases de equivalencia (teorico de Verificacion):
 *
 * <pre>
 *  tieneCategoria(nombre)
 *  CV  una categoria del evento                  -&gt; true
 *  CI  una categoria que existe pero no es suya  -&gt; false
 *  CI  una categoria que no existe               -&gt; false
 *  CI  null o vacio                              -&gt; false
 *  CI  mismo nombre con otras mayusculas         -&gt; false (compara exacto)
 *
 *  obtenerEdicionesAceptadas()
 *  CV  ediciones Aceptada                        -&gt; aparecen
 *  CI  ediciones Ingresada o Rechazada           -&gt; no aparecen
 *  Valor limite: evento sin ediciones            -&gt; conjunto vacio
 * </pre>
 */
@DisplayName("Evento · Consulta de Evento (CU 7)")
class EventoTest {

    private Evento evento;

    @BeforeEach
    void crearEvento() {
        evento = new Evento("Jornadas de Prueba", "Jornadas para probar la consulta",
                LocalDate.of(2026, 10, 26), "JP",
                List.of(new Categoria("Tecnología"), new Categoria("Cultura")));
    }

    // ----- tieneCategoria -----

    @ParameterizedTest(name = "\"{0}\" es una de sus categorias")
    @DisplayName("una categoria propia del evento")
    @ValueSource(strings = {"Tecnología", "Cultura"})
    void categoriaPropia(String categoria) {
        assertTrue(evento.tieneCategoria(categoria));
    }

    @ParameterizedTest(name = "\"{0}\" no es una de sus categorias")
    @DisplayName("una categoria que no es del evento")
    @ValueSource(strings = {"Deporte", "No existe", "tecnología", "Tecno"})
    void categoriaAjena(String categoria) {
        assertFalse(evento.tieneCategoria(categoria));
    }

    @ParameterizedTest(name = "categoria \"{0}\"")
    @DisplayName("sin categoria (null o vacio)")
    @NullAndEmptySource
    void sinCategoria(String categoria) {
        assertFalse(evento.tieneCategoria(categoria));
    }

    // ----- obtenerEdicionesAceptadas -----

    @Test
    @DisplayName("valor limite: un evento sin ediciones no tiene ediciones aceptadas")
    void sinEdiciones() {
        assertTrue(evento.obtenerEdicionesAceptadas().isEmpty());
    }

    @Test
    @DisplayName("una edicion recien creada nace Ingresada y no se publica")
    void edicionIngresadaNoSePublica() {
        // Datos de prueba
        evento.agregarEdicion(edicion("Prueba 2027", LocalDate.of(2027, 5, 10)));

        // Ejecucion
        Set<DTEdicionEvento> aceptadas = evento.obtenerEdicionesAceptadas();

        // Resultado esperado y comparacion
        assertTrue(aceptadas.isEmpty());
    }

    @Test
    @DisplayName("solo se publican las Aceptada, nunca las Ingresada ni las Rechazada")
    void soloLasAceptadas() throws ReglaNegocioException {
        // Datos de prueba: una edicion en cada estado, y una segunda aceptada
        EdicionEvento aceptada = edicion("Prueba 2026", LocalDate.of(2026, 5, 10));
        EdicionEvento otraAceptada = edicion("Prueba 2028", LocalDate.of(2028, 5, 10));
        EdicionEvento ingresada = edicion("Prueba 2027", LocalDate.of(2027, 5, 10));
        EdicionEvento rechazada = edicion("Prueba 2025", LocalDate.of(2025, 5, 10));
        aceptada.aceptar();
        otraAceptada.aceptar();
        rechazada.rechazar();
        evento.agregarEdicion(aceptada);
        evento.agregarEdicion(ingresada);
        evento.agregarEdicion(rechazada);
        evento.agregarEdicion(otraAceptada);

        // Ejecucion
        Set<DTEdicionEvento> publicas = evento.obtenerEdicionesAceptadas();

        // Resultado esperado y comparacion
        assertEquals(Set.of("Prueba 2026", "Prueba 2028"), nombres(publicas));
        for (DTEdicionEvento dt : publicas) {
            assertEquals(EstadoEdicion.ACEPTADA, dt.getEstado());
        }
    }

    @Test
    @DisplayName("obtenerEdiciones sigue devolviendo todas (la Estacion de Trabajo las necesita)")
    void obtenerEdicionesNoFiltra() throws ReglaNegocioException {
        EdicionEvento rechazada = edicion("Prueba 2025", LocalDate.of(2025, 5, 10));
        rechazada.rechazar();
        evento.agregarEdicion(rechazada);
        evento.agregarEdicion(edicion("Prueba 2027", LocalDate.of(2027, 5, 10)));

        assertEquals(2, evento.obtenerEdiciones().size());
        assertTrue(evento.obtenerEdicionesAceptadas().isEmpty());
    }

    // ----- Utilidades -----

    private static EdicionEvento edicion(String nombre, LocalDate inicio) {
        return new EdicionEvento(nombre, "JP", inicio, inicio.plusDays(1),
                LocalDate.of(2026, 10, 26), "Montevideo", "Uruguay", null);
    }

    private static Set<String> nombres(Set<DTEdicionEvento> ediciones) {
        return ediciones.stream().map(DTEdicionEvento::getNombre).collect(Collectors.toSet());
    }
}
