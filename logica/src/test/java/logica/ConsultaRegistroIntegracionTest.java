package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

/**
 * Consulta de Registro (CU 13) contra la base, con los datos de prueba:
 * vale23 se registro a JIAP 2026 (de imm) con codigo de patrocinio, y jperez
 * a Maraton 2026 (tambien de imm) pagando. Solo lee, no deja nada en la base.
 *
 * <pre>
 *  listarRegistrosDeEdicion(edicion, solicitante)
 *  consultarRegistro(edicion, nickname, solicitante)
 *  CV  el organizador de la edicion            -&gt; los ve
 *  CV  el asistente, su propio registro        -&gt; lo ve
 *  CI  otro asistente, otro organizador        -&gt; ReglaNegocioException
 *  CI  edicion que no existe
 *  Valor limite: sin nombre de edicion o nickname, visitante (null)
 * </pre>
 */
@DisplayName("Consulta de Registro contra la base (CU 13)")
class ConsultaRegistroIntegracionTest extends PruebaConBase {

    private static final String EDICION = "JIAP 2026";
    private static final String ORGANIZADOR = "imm";
    private static final String ASISTENTE = "vale23";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @Test
    @DisplayName("el organizador ve los registros de su edicion")
    void organizadorVeElListado() throws ReglaNegocioException {
        Set<DTRegistro> registros = sistema.listarRegistrosDeEdicion(EDICION, ORGANIZADOR);

        assertTrue(registros.stream().anyMatch(r -> ASISTENTE.equals(r.getNickname())));
    }

    @Test
    @DisplayName("ni un asistente ni otro organizador ven el listado")
    void otrosNoVenElListado() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarRegistrosDeEdicion(EDICION, ASISTENTE));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarRegistrosDeEdicion(EDICION, "antel"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarRegistrosDeEdicion(EDICION, null));
    }

    @Test
    @DisplayName("el asistente ve su registro: con codigo de patrocinio y sin costo")
    void asistenteVeElSuyo() throws ReglaNegocioException {
        DTRegistro dt = sistema.consultarRegistro(EDICION, ASISTENTE, ASISTENTE);

        assertEquals(ASISTENTE, dt.getNickname());
        assertTrue(dt.getUsoCodigo());
        assertEquals(0.0, dt.getCosto());
    }

    @Test
    @DisplayName("el organizador ve el registro de un asistente de su edicion")
    void organizadorVeElRegistro() throws ReglaNegocioException {
        assertEquals(ASISTENTE,
                sistema.consultarRegistro(EDICION, ASISTENTE, ORGANIZADOR).getNickname());
    }

    @Test
    @DisplayName("otro asistente, otro organizador o un visitante no ven el registro")
    void otrosNoVenElRegistro() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarRegistro(EDICION, ASISTENTE, "jperez"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarRegistro(EDICION, ASISTENTE, "antel"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarRegistro(EDICION, ASISTENTE, null));
    }

    @Test
    @DisplayName("una edicion que no existe da error")
    void edicionInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarRegistro("No existe", ASISTENTE, ASISTENTE));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarRegistrosDeEdicion("No existe", ORGANIZADOR));
    }

    @ParameterizedTest(name = "nombre \"{0}\"")
    @DisplayName("valor limite: sin nombre de edicion o sin nickname")
    @NullAndEmptySource
    void sinNombres(String vacio) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarRegistro(vacio, ASISTENTE, ASISTENTE));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarRegistro(EDICION, vacio, ORGANIZADOR));
    }
}
