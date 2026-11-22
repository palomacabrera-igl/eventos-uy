package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import persistencia.Persistencia;

/**
 * Video en el alta de edicion y busqueda (letra, 7.4 y 7.8) contra la base,
 * ejercitando Sistema.
 *
 * <pre>
 *  altaEdicion con video
 *  CV  youtu.be/ID             -&gt; se guarda como watch?v=ID y vuelve en la consulta
 *  CV  sin video               -&gt; urlVideo null
 *  CI  una direccion de otro sitio -&gt; ReglaNegocioException y la edicion no se crea
 *
 *  buscarEventos / buscarEdiciones
 *  CV  la edicion Aceptada y su evento se encuentran por nombre
 *  CV  una Ingresada no aparece
 *  CI  con el evento finalizado no aparece ninguno de los dos
 * </pre>
 *
 * El evento y las ediciones que se crean llevan el prefijo "IT R3" y se borran al terminar.
 */
@DisplayName("Video y busqueda contra la base")
class BusquedaYVideoIntegracionTest extends PruebaConBase {

    private static final String EVENTO = "IT R3 Evento";
    private static final String EDICION = "IT R3 Edicion";
    private static final LocalDate INICIO = LocalDate.of(2027, 9, 1);

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    void crearEvento() throws ReglaNegocioException {
        sistema.altaEvento("imm", EVENTO, "Evento de las pruebas de busqueda", "ITR3",
                List.of("Tecnología"), null);
    }

    @AfterEach
    void sinRastros() {
        borrar("edicion_evento", "nombre LIKE 'IT R3%'");
        borrar("evento_categoria",
                "evento_id IN (SELECT id FROM evento WHERE nombre LIKE 'IT R3%')");
        borrar("evento", "nombre LIKE 'IT R3%'");
    }

    private void altaEdicion(String nombre, String video) throws ReglaNegocioException {
        DTEdicionEvento datos = new DTEdicionEvento(nombre, "ITR3", "Montevideo", "Uruguay",
                DTFecha.desde(INICIO), DTFecha.desde(INICIO), null);
        datos.setUrlVideo(video);
        sistema.altaEdicion("imm", EVENTO, datos, null);
    }

    // ----- video -----

    @Test
    @DisplayName("el video se guarda como watch?v=ID y vuelve en la consulta")
    void conVideo() throws ReglaNegocioException {
        altaEdicion(EDICION, "https://youtu.be/jNQXAC9IVRw");

        assertEquals("https://www.youtube.com/watch?v=jNQXAC9IVRw",
                sistema.consultarEdicion(EDICION, "imm").getUrlVideo());
    }

    @Test
    @DisplayName("sin video la edicion se crea igual")
    void sinVideo() throws ReglaNegocioException {
        altaEdicion(EDICION, null);

        assertNull(sistema.consultarEdicion(EDICION, "imm").getUrlVideo());
    }

    @Test
    @DisplayName("un video que no es de YouTube se rechaza y la edicion no se crea")
    void videoInvalido() {
        assertThrows(ReglaNegocioException.class,
                () -> altaEdicion(EDICION, "https://vimeo.com/123456789"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarEdicion(EDICION, "imm"));
    }

    // ----- busqueda -----

    @Test
    @DisplayName("la edicion Aceptada y su evento se encuentran; una Ingresada no")
    void buscarPorNombre() throws ReglaNegocioException {
        altaEdicion(EDICION, null);
        altaEdicion(EDICION + " pendiente", null);
        sistema.aceptarEdicion(EDICION);

        assertEquals(List.of(EVENTO), nombresDeEventos("it r3"));
        assertEquals(List.of(EDICION), nombresDeEdiciones("IT R3"));
    }

    @Test
    @DisplayName("con el evento finalizado no aparece ni el evento ni su edicion")
    void eventoFinalizado() throws ReglaNegocioException {
        altaEdicion(EDICION, null);
        sistema.aceptarEdicion(EDICION);
        Persistencia.enTransaccion(em -> em.createNativeQuery(
                "UPDATE evento SET finalizado = true WHERE nombre = '" + EVENTO + "'")
                .executeUpdate());

        assertTrue(nombresDeEventos("IT R3").isEmpty());
        assertTrue(nombresDeEdiciones("IT R3").isEmpty());
    }

    private List<String> nombresDeEventos(String texto) {
        return sistema.buscarEventos(texto, OrdenBusqueda.ALFABETICO_ASC).stream()
                .map(DTEvento::getNombre).toList();
    }

    private List<String> nombresDeEdiciones(String texto) {
        return sistema.buscarEdiciones(texto, OrdenBusqueda.ALFABETICO_ASC).stream()
                .map(DTEdicionEvento::getNombre).toList();
    }
}
