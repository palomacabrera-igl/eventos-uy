package logica;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Alta de Evento (CU 6) contra la base, ejercitando Sistema.
 *
 * <pre>
 *  altaEvento(solicitante, nombre, descripcion, sigla, categorias, imagen)
 *  CV  un organizador con datos validos      -&gt; queda creado con la fecha de hoy
 *  CV  con imagen                            -&gt; se guarda y se puede pedir
 *  CI  un asistente o un visitante           -&gt; ReglaNegocioException
 *  CI  un nombre que ya existe               -&gt; ReglaNegocioException
 *  CI  una categoria que no existe           -&gt; ReglaNegocioException, sin crear nada
 *  Valor limite: la misma categoria dos veces -&gt; queda una sola vez
 * </pre>
 *
 * Cada prueba crea sus eventos con el prefijo "IT CU6" y los borra al
 * terminar: no depende de DatosDePrueba ni lo ensucia.
 */
@DisplayName("Alta de Evento contra la base (CU 6)")
class AltaEventoIntegracionTest extends PruebaConBase {

    private static final String NOMBRE = "IT CU6 Evento";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @AfterEach
    void sinRastros() {
        // Primero la tabla de union, que apunta al evento.
        borrar("evento_categoria",
                "evento_id IN (SELECT id FROM evento WHERE nombre LIKE 'IT CU6%')");
        borrar("evento", "nombre LIKE 'IT CU6%'");
    }

    @Test
    @DisplayName("un organizador lo crea con la fecha de hoy y sus categorias")
    void organizadorLoCrea() throws ReglaNegocioException {
        // Ejecucion
        sistema.altaEvento("imm", NOMBRE, "Evento de prueba", "ITC6",
                List.of("Deporte", "Cultura"), null);

        // Resultado esperado y comparacion
        DTEvento dt = sistema.consultarEvento(NOMBRE);
        assertEquals("ITC6", dt.getSigla());
        assertEquals(LocalDate.now(), dt.getFechaAlta().aLocalDate());
        assertTrue(dt.getCategorias().containsAll(List.of("Deporte", "Cultura")));
        assertFalse(dt.getTieneImagen());
        assertTrue(nombres(sistema.listarEventos("Deporte")).contains(NOMBRE));
    }

    @Test
    @DisplayName("los espacios alrededor del nombre no se guardan")
    void nombreSinEspacios() throws ReglaNegocioException {
        sistema.altaEvento("imm", "  " + NOMBRE + "  ", "Evento de prueba", "ITC6",
                List.of("Deporte"), null);

        assertEquals(NOMBRE, sistema.consultarEvento(NOMBRE).getNombre());
    }

    @Test
    @DisplayName("con imagen: queda guardada y se puede pedir")
    void conImagen() throws ReglaNegocioException {
        byte[] bytes = {1, 2, 3, 4};

        sistema.altaEvento("imm", NOMBRE, "Evento de prueba", "ITC6",
                List.of("Deporte"), new DTImagen(bytes, "image/png"));

        assertTrue(sistema.consultarEvento(NOMBRE).getTieneImagen());
        DTImagen guardada = sistema.obtenerImagenEvento(NOMBRE);
        assertArrayEquals(bytes, guardada.getContenido());
        assertEquals("image/png", guardada.getTipo());
    }

    @Test
    @DisplayName("valor limite: la misma categoria dos veces queda una sola vez")
    void categoriaRepetida() throws ReglaNegocioException {
        sistema.altaEvento("imm", NOMBRE, "Evento de prueba", "ITC6",
                List.of("Deporte", "Deporte"), null);

        assertEquals(List.of("Deporte"), sistema.consultarEvento(NOMBRE).getCategorias());
    }

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("un asistente, un visitante o alguien que no existe no puede")
    @NullSource
    @ValueSource(strings = {"vale23", "no-existe-nadie"})
    void soloOrganizadores(String solicitante) {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaEvento(solicitante,
                NOMBRE, "Evento de prueba", "ITC6", List.of("Deporte"), null));
    }

    @Test
    @DisplayName("un nombre que ya existe se rechaza")
    void nombreRepetido() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaEvento("imm", "JIAP", "Otro", "J2", List.of("Deporte"), null));
        assertTrue(e.getMessage().contains("Ya existe un evento"));
    }

    @Test
    @DisplayName("una categoria que no existe se rechaza y no se crea nada")
    void categoriaInexistente() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaEvento("imm", NOMBRE,
                "Evento de prueba", "ITC6", List.of("Deporte", "No existe"), null));

        assertThrows(ReglaNegocioException.class, () -> sistema.consultarEvento(NOMBRE));
    }

    private static List<String> nombres(java.util.Set<DTEvento> eventos) {
        return eventos.stream().map(DTEvento::getNombre).toList();
    }
}
