package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Alta de Patrocinio (CU 15) y tipos de registro, contra la base.
 *
 * El tope del 20% ya esta probado sin base en AltaPatrocinioTest; aca se
 * comprueban las reglas que necesitan mirar toda la plataforma: que el
 * codigo sea unico, que la institucion no repita patrocinio en la edicion,
 * y que la edicion sea del organizador que pide.
 *
 * <pre>
 *  altaPatrocinio(solicitante, nombreEdicion, datos)
 *  CV  un patrocinio valido          -&gt; queda guardado, con la fecha del dia
 *  CI  codigo que ya existe en OTRA edicion
 *  CI  la institucion ya patrocina esa edicion
 *  CI  la edicion no es del organizador que pide
 *  CI  nivel que no existe, aporte en cero, cantidad negativa
 *  Valor limite: sin sesion, sin datos, codigo vacio, edicion inexistente
 *
 *  listarTiposDeRegistro(nombreEdicion)
 *  CV  una edicion con tipos         -&gt; los devuelve, sin dejar estado
 *  CI  una edicion que no existe
 * </pre>
 */
@DisplayName("Alta de Patrocinio contra la base (CU 15)")
class AltaPatrocinioIntegracionTest extends PruebaConBase {

    private static final String EDICION = "JIAP 2027";
    private static final String CODIGO = "TEST-IT-PATRO";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    private String tipoDeRegistro;

    @BeforeEach
    void prepararse() throws ReglaNegocioException {
        sinRastros();
        tipoDeRegistro = sistema.listarTiposDeRegistro(EDICION).iterator().next().getNombre();
    }

    @AfterEach
    void sinRastros() {
        borrar("patrocinio", "codigo LIKE 'TEST-IT-%'");
    }

    private DTPatrocinio datos(String codigo, String institucion, double aporte, int gratis) {
        return new DTPatrocinio(codigo, DTFecha.desde(LocalDate.now()), aporte,
                "ORO", gratis, institucion, tipoDeRegistro);
    }

    // ----- El alta que tiene que salir bien -----

    @Test
    @DisplayName("crea el patrocinio y le pone la fecha del dia, no la del formulario")
    void altaValida() throws ReglaNegocioException {
        sistema.altaPatrocinio("imm", EDICION, datos(CODIGO, "ANCAP", 500000, 1));

        // consultarPatrocinio y listarPatrociniosDeEdicion son el CU 16 y
        // todavia no estan implementados, asi que se relee de la base.
        Patrocinio guardado = buscarEnLaBase(CODIGO);

        assertEquals("ANCAP", guardado.getInstitucion().getNombre());
        assertEquals(500000.0, guardado.getMonto());
        assertEquals(NivelPatrocinio.ORO, guardado.getNivelPatro());
        assertEquals(LocalDate.now(), guardado.getFechaIni(),
                "la fecha la pone el sistema, no el formulario");
    }

    // ----- Las reglas que miran toda la plataforma -----

    private Patrocinio buscarEnLaBase(String codigo) {
        Patrocinio[] encontrado = new Patrocinio[1];
        persistencia.Persistencia.enTransaccion(em ->
                encontrado[0] = em.createQuery(
                        "SELECT p FROM Patrocinio p WHERE p.codigo = :c", Patrocinio.class)
                        .setParameter("c", codigo)
                        .getResultStream().findFirst().orElse(null));
        if (encontrado[0] == null) {
            throw new AssertionError("el patrocinio no quedo guardado");
        }
        return encontrado[0];
    }

    @Test
    @DisplayName("rechaza un codigo que ya existe, aunque sea de otra edicion")
    void codigoRepetidoEnLaPlataforma() {
        // 1001 es de otra edicion en los datos de prueba.
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION, datos("1001", "ANCAP", 500000, 1)));

        assertTrue(error.getMessage().contains("1001"));
    }

    @Test
    @DisplayName("una institucion no patrocina dos veces la misma edicion")
    void institucionRepetidaEnLaEdicion() throws ReglaNegocioException {
        sistema.altaPatrocinio("imm", EDICION, datos(CODIGO, "ANCAP", 500000, 1));

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION,
                        datos("TEST-IT-OTRO", "ANCAP", 500000, 1)));
    }

    @Test
    @DisplayName("solo el organizador de la edicion puede patrocinarla")
    void laEdicionEsDeOtro() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("antel", EDICION, datos(CODIGO, "ANCAP", 500000, 1)));
    }

    @ParameterizedTest(name = "nivel \"{0}\"")
    @DisplayName("rechaza un nivel que no esta entre los cuatro de la letra")
    @ValueSource(strings = {"DIAMANTE", "", "oro oscuro", "123"})
    void nivelInvalido(String nivel) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION,
                        new DTPatrocinio(CODIGO, DTFecha.desde(LocalDate.now()), 500000,
                                nivel, 1, "ANCAP", tipoDeRegistro)));
    }

    @ParameterizedTest(name = "aporte de ${0}")
    @DisplayName("valor limite: el aporte tiene que ser mayor que cero")
    @ValueSource(doubles = {0.0, -1.0, -500000.0})
    void aporteNoPositivo(double aporte) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION, datos(CODIGO, "ANCAP", aporte, 1)));
    }

    @ParameterizedTest(name = "{0} registros gratuitos")
    @DisplayName("valor limite: la cantidad de gratuitos no puede ser negativa")
    @ValueSource(ints = {-1, -100})
    void cantidadNegativa(int cantidad) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION,
                        datos(CODIGO, "ANCAP", 500000, cantidad)));
    }

    @Test
    @DisplayName("rechaza pasarse del 20 % del aporte")
    void pasarseDelTope() {
        // Con un aporte de 10 pesos, el tope son 2: cualquier registro
        // gratuito de un tipo que cueste algo se pasa.
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION, datos(CODIGO, "ANCAP", 10, 5)));
    }

    @Test
    @DisplayName("valor limite: sin sesion, sin datos, codigo vacio, edicion inexistente")
    void limites() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio(null, EDICION, datos(CODIGO, "ANCAP", 500000, 1)));

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION, null));

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION, datos("   ", "ANCAP", 500000, 1)));

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", "No Existe 2099",
                        datos(CODIGO, "ANCAP", 500000, 1)));

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaPatrocinio("imm", EDICION,
                        datos(CODIGO, "Instituto Fantasma", 500000, 1)));
    }

    // ----- Los tipos de registro de una edicion -----

    @Test
    @DisplayName("lista los tipos de registro de una edicion sin dejar estado")
    void tiposDeUnaEdicion() throws ReglaNegocioException {
        assertFalse(sistema.listarTiposDeRegistro(EDICION).isEmpty());

        // Dos llamadas seguidas a ediciones distintas tienen que dar cada una
        // lo suyo: si la operacion guardara la edicion en un campo, la segunda
        // arrastraria la primera.
        assertFalse(sistema.listarTiposDeRegistro("JIAP 2026").isEmpty());
        assertFalse(sistema.listarTiposDeRegistro(EDICION).isEmpty());
    }

    @Test
    @DisplayName("rechaza una edicion que no existe o un nombre vacio")
    void tiposDeUnaEdicionInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarTiposDeRegistro("No Existe 2099"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarTiposDeRegistro("  "));
    }
}
