package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de Aceptar/Rechazar Edicion (Estacion de Trabajo), sin base de
 * datos: las reglas viven en las entidades y Sistema solo busca y delega.
 *
 * <pre>
 *  obtenerEdicionesIngresadas()
 *  CV  ediciones Ingresada                       -&gt; aparecen
 *  CI  ediciones Aceptada o Rechazada            -&gt; no aparecen
 *  Valor limite: evento sin ediciones            -&gt; conjunto vacio
 *
 *  Transiciones (letra, Tabla 1)
 *  CV  Ingresada -&gt; Aceptada / Rechazada
 *  CI  Aceptada  -&gt; cualquier otro estado        -&gt; ReglaNegocioException
 *  CI  Rechazada -&gt; cualquier otro estado        -&gt; ReglaNegocioException
 * </pre>
 */
@DisplayName("Aceptar/Rechazar Edicion")
class AceptarRechazarEdicionTest {

    private Evento evento;

    @BeforeEach
    void crearEvento() {
        evento = new Evento("Jornadas de Prueba", "Jornadas para probar el caso de uso",
                LocalDate.of(2026, 10, 27), "JP", List.of(new Categoria("Tecnología")));
    }

    // ----- Listado de ediciones Ingresada -----

    @Test
    @DisplayName("valor limite: un evento sin ediciones no tiene ediciones para revisar")
    void sinEdiciones() {
        assertTrue(evento.obtenerEdicionesIngresadas().isEmpty());
    }

    @Test
    @DisplayName("solo se listan las Ingresada, no las Aceptada ni las Rechazada")
    void soloLasIngresadas() throws ReglaNegocioException {
        // Datos de prueba: una edicion en cada estado, y una segunda ingresada
        EdicionEvento ingresada = edicion("Prueba 2027");
        EdicionEvento otraIngresada = edicion("Prueba 2028");
        EdicionEvento aceptada = edicion("Prueba 2026");
        EdicionEvento rechazada = edicion("Prueba 2025");
        aceptada.aceptar();
        rechazada.rechazar();
        evento.agregarEdicion(ingresada);
        evento.agregarEdicion(otraIngresada);
        evento.agregarEdicion(aceptada);
        evento.agregarEdicion(rechazada);

        // Ejecucion
        Set<DTEdicionEvento> paraRevisar = evento.obtenerEdicionesIngresadas();

        // Resultado esperado y comparacion
        assertEquals(Set.of("Prueba 2027", "Prueba 2028"), nombres(paraRevisar));
        for (DTEdicionEvento dt : paraRevisar) {
            assertEquals(EstadoEdicion.INGRESADA, dt.getEstado());
        }
    }

    @Test
    @DisplayName("una edicion aceptada deja de estar en la lista para revisar y pasa a publicarse")
    void alAceptarSaleDeLaLista() throws ReglaNegocioException {
        EdicionEvento edicion = edicion("Prueba 2027");
        evento.agregarEdicion(edicion);

        edicion.aceptar();

        assertTrue(evento.obtenerEdicionesIngresadas().isEmpty());
        assertEquals(Set.of("Prueba 2027"), nombres(evento.obtenerEdicionesAceptadas()));
    }

    @Test
    @DisplayName("una edicion rechazada deja de estar en la lista y no se publica")
    void alRechazarSaleDeLaLista() throws ReglaNegocioException {
        EdicionEvento edicion = edicion("Prueba 2027");
        evento.agregarEdicion(edicion);

        edicion.rechazar();

        assertTrue(evento.obtenerEdicionesIngresadas().isEmpty());
        assertTrue(evento.obtenerEdicionesAceptadas().isEmpty());
    }

    // ----- Transiciones de estado -----

    @Test
    @DisplayName("toda edicion nueva nace Ingresada")
    void naceIngresada() {
        assertEquals(EstadoEdicion.INGRESADA, edicion("Prueba 2027").getEstado());
    }

    @Test
    @DisplayName("de Aceptada no se vuelve: ni rechazar ni aceptar otra vez")
    void deAceptadaNoSeVuelve() throws ReglaNegocioException {
        EdicionEvento edicion = edicion("Prueba 2027");
        edicion.aceptar();

        assertThrows(ReglaNegocioException.class, edicion::rechazar);
        assertThrows(ReglaNegocioException.class, edicion::aceptar);
        assertEquals(EstadoEdicion.ACEPTADA, edicion.getEstado());
    }

    @Test
    @DisplayName("de Rechazada no se vuelve: ni aceptar ni rechazar otra vez")
    void deRechazadaNoSeVuelve() throws ReglaNegocioException {
        EdicionEvento edicion = edicion("Prueba 2027");
        edicion.rechazar();

        assertThrows(ReglaNegocioException.class, edicion::aceptar);
        assertThrows(ReglaNegocioException.class, edicion::rechazar);
        assertEquals(EstadoEdicion.RECHAZADA, edicion.getEstado());
    }

    // ----- Utilidades -----

    private static EdicionEvento edicion(String nombre) {
        LocalDate inicio = LocalDate.of(2027, 5, 10);
        return new EdicionEvento(nombre, "JP", inicio, inicio.plusDays(1),
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", null);
    }

    private static Set<String> nombres(Set<DTEdicionEvento> ediciones) {
        return ediciones.stream().map(DTEdicionEvento::getNombre).collect(Collectors.toSet());
    }
}
