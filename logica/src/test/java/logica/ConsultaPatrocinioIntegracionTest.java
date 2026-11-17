package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Consulta de Patrocinio (CU 16) contra la base, con los datos de prueba:
 * JIAP 2026 esta Aceptada y JIAP 2027 Ingresada; las dos son de imm y UTEC
 * patrocina las dos. Solo lee, no deja nada en la base.
 *
 * <pre>
 *  listarPatrociniosDeEdicion(edicion, solicitante)
 *  consultarPatrocinio(edicion, institucion, solicitante)
 *  CV  edicion Aceptada, cualquiera la consulta  -&gt; codigo solo para imm
 *  CV  edicion Ingresada, la consulta imm        -&gt; la ve, con codigo
 *  CI  edicion Ingresada, la consulta otro       -&gt; ReglaNegocioException
 *  CI  edicion o institucion que no existe
 *  Valor limite: sin nombre de edicion o de institucion
 * </pre>
 */
@DisplayName("Consulta de Patrocinio contra la base (CU 16)")
class ConsultaPatrocinioIntegracionTest extends PruebaConBase {

    private static final String ACEPTADA = "JIAP 2026";
    private static final String INGRESADA = "JIAP 2027";
    private static final String ORGANIZADOR = "imm";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @Test
    @DisplayName("el organizador ve el codigo de su patrocinio")
    void organizadorVeElCodigo() throws ReglaNegocioException {
        DTPatrocinio dt = sistema.consultarPatrocinio(ACEPTADA, "UTEC", ORGANIZADOR);

        assertEquals("UTEC", dt.getInstitucion());
        assertFalse(dt.getCodigoPatrocinio() == null || dt.getCodigoPatrocinio().isBlank());
    }

    @ParameterizedTest(name = "\"{0}\" ve el patrocinio sin codigo")
    @DisplayName("un visitante u otro usuario ven el patrocinio, sin codigo")
    @ValueSource(strings = {"antel", "sofia"})
    void otroNoVeElCodigo(String solicitante) throws ReglaNegocioException {
        DTPatrocinio dt = sistema.consultarPatrocinio(ACEPTADA, "UTEC", solicitante);

        assertEquals("UTEC", dt.getInstitucion());
        assertNull(dt.getCodigoPatrocinio());
    }

    @Test
    @DisplayName("valor limite: un visitante (null) tampoco ve el codigo")
    void visitanteNoVeElCodigo() throws ReglaNegocioException {
        Set<DTPatrocinio> patrocinios = sistema.listarPatrociniosDeEdicion(ACEPTADA, null);

        assertFalse(patrocinios.isEmpty());
        for (DTPatrocinio p : patrocinios) {
            assertNull(p.getCodigoPatrocinio());
        }
    }

    @Test
    @DisplayName("una edicion Ingresada la ve solo su organizador")
    void ingresadaSoloParaSuOrganizador() throws ReglaNegocioException {
        DTPatrocinio dt = sistema.consultarPatrocinio(INGRESADA, "UTEC", ORGANIZADOR);
        assertEquals("UTEC", dt.getInstitucion());

        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarPatrocinio(INGRESADA, "UTEC", "antel"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarPatrociniosDeEdicion(INGRESADA, null));
    }

    @Test
    @DisplayName("una edicion o una institucion que no existen dan error")
    void noExiste() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarPatrocinio("No existe", "UTEC", ORGANIZADOR));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarPatrocinio(ACEPTADA, "No patrocina", ORGANIZADOR));
    }

    @ParameterizedTest(name = "nombre \"{0}\"")
    @DisplayName("valor limite: sin nombre de edicion o de institucion")
    @NullAndEmptySource
    void sinNombres(String vacio) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarPatrocinio(vacio, "UTEC", ORGANIZADOR));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarPatrocinio(ACEPTADA, vacio, ORGANIZADOR));
    }
}
