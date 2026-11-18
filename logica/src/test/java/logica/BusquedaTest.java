package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Busqueda del cabezal del sitio (letra, 7.8), sin base: Busqueda recibe
 * entidades armadas aca.
 *
 * <pre>
 *  Eventos                        alta        estado
 *  Nube Festival  "musica"        2026-03-01
 *  Ñandú Rock     "festival"      2026-05-20
 *  Oro Expo       "negocios"      2026-05-20
 *  Feria Vieja    "festival"      2026-06-01  finalizado
 *
 *  Ediciones                      alta        estado
 *  Nube 2026      (Nube)          2026-04-01  Aceptada
 *  Ñandú 2026     (Ñandú)         2026-06-10  Aceptada
 *  Oro 2026       (Oro)           2026-06-10  Aceptada
 *  Oro 2027       (Oro)           2026-07-01  Ingresada
 *  Feria 2026     (Feria Vieja)   2026-07-15  Aceptada, pero el evento esta finalizado
 * </pre>
 */
@DisplayName("Busqueda de eventos y ediciones")
class BusquedaTest {

    private final List<Evento> eventos = new ArrayList<>();

    @BeforeEach
    void armar() throws ReglaNegocioException {
        Organizador org = new Organizador("org", "Org", "org@ejemplo.uy", "Organiza", null);

        Evento nube = evento("Nube Festival", "Encuentro de musica", LocalDate.of(2026, 3, 1));
        Evento nandu = evento("Ñandú Rock", "Un festival al aire libre", LocalDate.of(2026, 5, 20));
        Evento oro = evento("Oro Expo", "Feria de negocios", LocalDate.of(2026, 5, 20));
        Evento vieja = evento("Feria Vieja", "Otro festival", LocalDate.of(2026, 6, 1));
        vieja.finalizar();

        edicion(nube, org, "Nube 2026", LocalDate.of(2026, 4, 1), true);
        edicion(nandu, org, "Ñandú 2026", LocalDate.of(2026, 6, 10), true);
        edicion(oro, org, "Oro 2026", LocalDate.of(2026, 6, 10), true);
        edicion(oro, org, "Oro 2027", LocalDate.of(2026, 7, 1), false);
        edicion(vieja, org, "Feria 2026", LocalDate.of(2026, 7, 15), true);
    }

    private Evento evento(String nombre, String descripcion, LocalDate alta) {
        Evento e = new Evento(nombre, descripcion, alta, "SIG", List.of());
        eventos.add(e);
        return e;
    }

    private static void edicion(Evento evento, Organizador org, String nombre, LocalDate alta,
                                boolean aceptada) throws ReglaNegocioException {
        LocalDate inicio = LocalDate.of(2027, 1, 10);
        EdicionEvento ed = evento.altaEdicion(new DTEdicionEvento(nombre, "SIG", "Montevideo",
                "Uruguay", DTFecha.desde(inicio), DTFecha.desde(inicio), DTFecha.desde(alta)), org);
        if (aceptada) {
            ed.aceptar();
        }
    }

    private List<String> eventosEncontrados(String texto, OrdenBusqueda orden) {
        return Busqueda.eventos(eventos, texto, orden).stream().map(DTEvento::getNombre).toList();
    }

    private List<String> edicionesEncontradas(String texto, OrdenBusqueda orden) {
        return Busqueda.ediciones(eventos, texto, orden).stream()
                .map(DTEdicionEvento::getNombre).toList();
    }

    // ----- que se encuentra -----

    @ParameterizedTest(name = "\"{0}\"")
    @DisplayName("sin texto se listan todos los eventos no finalizados")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void sinTextoListaTodos(String texto) {
        assertEquals(List.of("Ñandú Rock", "Oro Expo", "Nube Festival"),
                eventosEncontrados(texto, OrdenBusqueda.FECHA_ALTA));
    }

    @Test
    @DisplayName("un evento se encuentra por su nombre, sin distinguir mayusculas")
    void eventoPorNombre() {
        assertEquals(List.of("Oro Expo"), eventosEncontrados("ORO", OrdenBusqueda.FECHA_ALTA));
        assertEquals(List.of("Ñandú Rock"), eventosEncontrados("ñandú", OrdenBusqueda.FECHA_ALTA));
    }

    @Test
    @DisplayName("un evento se encuentra por su descripcion")
    void eventoPorDescripcion() {
        assertEquals(List.of("Nube Festival"), eventosEncontrados("MUSICA", OrdenBusqueda.FECHA_ALTA));
    }

    @Test
    @DisplayName("un evento finalizado no aparece aunque coincida")
    void eventoFinalizadoNoAparece() {
        assertEquals(List.of("Ñandú Rock", "Nube Festival"),
                eventosEncontrados("festival", OrdenBusqueda.FECHA_ALTA));
        assertTrue(eventosEncontrados("vieja", OrdenBusqueda.FECHA_ALTA).isEmpty());
    }

    @Test
    @DisplayName("un texto que no coincide con nada devuelve una lista vacia")
    void sinCoincidencias() {
        assertTrue(eventosEncontrados("zzz", OrdenBusqueda.FECHA_ALTA).isEmpty());
        assertTrue(edicionesEncontradas("zzz", OrdenBusqueda.FECHA_ALTA).isEmpty());
    }

    @Test
    @DisplayName("solo aparecen las ediciones Aceptadas de eventos no finalizados")
    void soloEdicionesAceptadas() {
        assertEquals(List.of("Oro 2026"), edicionesEncontradas("oro", OrdenBusqueda.FECHA_ALTA));
        assertTrue(edicionesEncontradas("feria", OrdenBusqueda.FECHA_ALTA).isEmpty());
        assertEquals(List.of("Ñandú 2026", "Oro 2026", "Nube 2026"),
                edicionesEncontradas(null, OrdenBusqueda.FECHA_ALTA));
    }

    @Test
    @DisplayName("una edicion se busca por su nombre, no por la descripcion de su evento")
    void edicionSoloPorNombre() {
        assertTrue(edicionesEncontradas("musica", OrdenBusqueda.FECHA_ALTA).isEmpty());
        assertEquals(List.of("Nube 2026"), edicionesEncontradas("NUBE", OrdenBusqueda.FECHA_ALTA));
    }

    // ----- ordenes -----

    @Test
    @DisplayName("por fecha de alta van primero los mas nuevos y, si empatan, por nombre")
    void porFechaDeAlta() {
        assertEquals(List.of("Ñandú Rock", "Oro Expo", "Nube Festival"),
                eventosEncontrados("", OrdenBusqueda.FECHA_ALTA));
    }

    @Test
    @DisplayName("sin orden se usa el de fecha de alta")
    void sinOrdenEsFechaDeAlta() {
        assertEquals(eventosEncontrados("", OrdenBusqueda.FECHA_ALTA), eventosEncontrados("", null));
        assertEquals(edicionesEncontradas("", OrdenBusqueda.FECHA_ALTA), edicionesEncontradas("", null));
    }

    @Test
    @DisplayName("alfabetico ascendente, con la Ñ despues de la N")
    void alfabeticoAscendente() {
        assertEquals(List.of("Nube Festival", "Ñandú Rock", "Oro Expo"),
                eventosEncontrados("", OrdenBusqueda.ALFABETICO_ASC));
        assertEquals(List.of("Nube 2026", "Ñandú 2026", "Oro 2026"),
                edicionesEncontradas("", OrdenBusqueda.ALFABETICO_ASC));
    }

    @Test
    @DisplayName("alfabetico descendente")
    void alfabeticoDescendente() {
        assertEquals(List.of("Oro Expo", "Ñandú Rock", "Nube Festival"),
                eventosEncontrados("", OrdenBusqueda.ALFABETICO_DESC));
        assertEquals(List.of("Oro 2026", "Ñandú 2026", "Nube 2026"),
                edicionesEncontradas("", OrdenBusqueda.ALFABETICO_DESC));
    }
}
