package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Alta y Consulta de Tipo de Registro contra la base (CU 10 y 11).
 *
 * <p>TipoRegistroTest ya prueba la entidad: el cupo, el nombre unico dentro de
 * la edicion. Esta prueba el caso de uso, que es el camino por donde entra el
 * organizador desde el sitio.
 *
 * <p>La letra, CU 10: «El sistema lista las ediciones que organiza el usuario,
 * en cualquier estado. El organizador elige una e ingresa nombre, descripcion,
 * costo y cupo. Si ya existe ese nombre de tipo de registro en la edicion,
 * puede elegir otro o cancelar».
 *
 * <pre>
 *  altaTipoRegistro(solicitante, edicion, nombre, descripcion, costo, cupo)
 *  CV  el organizador de la edicion            -&gt; queda creado
 *  CV  sobre una edicion Ingresada             -&gt; vale: la letra dice
 *                                                 "en cualquier estado"
 *  CI  1. otro organizador                     -&gt; la edicion no es suya
 *  CI  2. un asistente o un visitante
 *  CI  3. nombre repetido en la misma edicion
 *  CI  4. una edicion que no existe
 *
 *  consultarTipoRegistro(edicion, nombre)
 *  CV  devuelve todos sus datos
 *  CI  un tipo que no esta en esa edicion
 * </pre>
 */
@DisplayName("Alta y Consulta de Tipo de Registro contra la base (CU 10 y 11)")
class TipoRegistroIntegracionTest extends PruebaConBase {

    /** Una edicion propia, que se crea y se borra en cada prueba. */
    private static final String EDICION = "IT CU10 Edicion";
    private static final String TIPO = "IT CU10 General";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @AfterEach
    void sinRastros() {
        borrar("tipo_registro", "edicion_id IN (SELECT id FROM edicion_evento "
                + "WHERE nombre LIKE 'IT CU10%')");
        borrar("edicion_evento", "nombre LIKE 'IT CU10%'");
    }

    /** La edicion queda Ingresada: la letra permite agregarle tipos igual. */
    private void crearLaEdicion() throws ReglaNegocioException {
        sistema.altaEdicion("imm", "JIAP", new DTEdicionEvento(EDICION, "ITC10",
                "Montevideo", "Uruguay",
                DTFecha.desde(java.time.LocalDate.of(2027, 9, 1)),
                DTFecha.desde(java.time.LocalDate.of(2027, 9, 3)), null), null);
    }

    // ----- altaTipoRegistro -----

    @Test
    @DisplayName("el organizador lo crea sobre su edicion Ingresada, con todos sus datos")
    void loCreaSuOrganizador() throws ReglaNegocioException {
        crearLaEdicion();

        sistema.altaTipoRegistro("imm", EDICION, TIPO, "Entrada general", 1500.0, 50);

        DTTipoRegistro dt = sistema.listarTiposDeRegistro(EDICION).stream()
                .filter(t -> t.getNombre().equals(TIPO))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no quedo creado"));
        assertEquals("Entrada general", dt.getDescripcion());
        assertEquals(1500.0, dt.getCosto());
        assertEquals(50, dt.getCupo());
    }

    @Test
    @DisplayName("CU 11 · la consulta publica necesita que la edicion este Aceptada")
    void laConsultaPideEdicionAceptada() throws ReglaNegocioException {
        crearLaEdicion();
        sistema.altaTipoRegistro("imm", EDICION, TIPO, "Entrada general", 1500.0, 50);

        // Mientras esta Ingresada, la consulta publica no la muestra. La letra,
        // CU 11: "ve las ediciones en estado Aceptada".
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarTipoRegistro(EDICION, TIPO));

        sistema.aceptarEdicion(EDICION);

        DTTipoRegistro dt = sistema.consultarTipoRegistro(EDICION, TIPO);
        assertEquals(TIPO, dt.getNombre());
        assertEquals(1500.0, dt.getCosto());
        assertEquals(50, dt.getCupo());
    }

    @Test
    @DisplayName("aparece en el listado de tipos de esa edicion")
    void apareceEnElListado() throws ReglaNegocioException {
        crearLaEdicion();
        sistema.altaTipoRegistro("imm", EDICION, TIPO, "Entrada general", 1500.0, 50);

        assertTrue(sistema.listarTiposDeRegistro(EDICION).stream()
                .anyMatch(t -> t.getNombre().equals(TIPO)));
    }

    @Test
    @DisplayName("CI 1 · otro organizador no puede agregarle tipos a una edicion ajena")
    void otroOrganizadorNoPuede() throws ReglaNegocioException {
        crearLaEdicion();

        assertThrows(ReglaNegocioException.class, () -> sistema.altaTipoRegistro(
                "antel", EDICION, TIPO, "Entrada general", 1500.0, 50));
    }

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("CI 2 · un asistente o un visitante tampoco")
    @NullSource
    @ValueSource(strings = {"vale23"})
    void soloOrganizadores(String solicitante) throws ReglaNegocioException {
        crearLaEdicion();

        assertThrows(ReglaNegocioException.class, () -> sistema.altaTipoRegistro(
                solicitante, EDICION, TIPO, "Entrada general", 1500.0, 50));
    }

    @Test
    @DisplayName("CI 3 · el nombre del tipo es unico dentro de la edicion")
    void nombreRepetidoEnLaEdicion() throws ReglaNegocioException {
        crearLaEdicion();
        sistema.altaTipoRegistro("imm", EDICION, TIPO, "Entrada general", 1500.0, 50);

        assertThrows(ReglaNegocioException.class, () -> sistema.altaTipoRegistro(
                "imm", EDICION, TIPO, "Otra descripcion", 900.0, 10));
    }

    @Test
    @DisplayName("CI 4 · una edicion que no existe")
    void edicionInexistente() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaTipoRegistro(
                "imm", "No existe 2099", TIPO, "Entrada general", 1500.0, 50));
    }

    // ----- consultarTipoRegistro -----

    @Test
    @DisplayName("CI · un tipo que no esta en esa edicion")
    void tipoInexistente() throws ReglaNegocioException {
        crearLaEdicion();
        sistema.aceptarEdicion(EDICION);

        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarTipoRegistro(EDICION, "No existe"));
    }
}
