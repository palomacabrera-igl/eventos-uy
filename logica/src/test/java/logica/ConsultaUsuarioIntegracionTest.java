package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Consulta de Usuario (CU 4) contra la base, ejercitando Sistema.
 *
 * Las reglas de quien ve que ya estan probadas sin base en
 * ConsultaUsuarioTest; aca se comprueba que Sistema las aplique de verdad
 * sobre los datos cargados, y que busque y rechace como corresponde.
 *
 * <pre>
 *  consultarUsuario(nickname, solicitante)
 *  CV  un usuario que existe                    -&gt; su perfil
 *  CI  uno que no existe                        -&gt; ReglaNegocioException
 *
 *  listarEdicionesDeOrganizador(nickname, solicitante)
 *  CV  el propio organizador                    -&gt; incluye Ingresada y Rechazada
 *  CI  otro usuario, o un visitante (null)      -&gt; solo las Aceptada
 *  CI  el nickname es de un asistente           -&gt; ReglaNegocioException
 *
 *  listarRegistrosDeAsistente(nickname, solicitante)
 *  CV  el propio asistente                      -&gt; sus registros
 *  CI  otro usuario, o un visitante             -&gt; conjunto vacio, no error
 *  CI  el nickname es de un organizador         -&gt; ReglaNegocioException
 * </pre>
 */
@DisplayName("Consulta de Usuario contra la base (CU 4)")
class ConsultaUsuarioIntegracionTest extends PruebaConBase {

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    private Set<EstadoEdicion> estadosQueVe(String solicitante) throws ReglaNegocioException {
        return sistema.listarEdicionesDeOrganizador("imm", solicitante).stream()
                .map(DTEdicionEvento::getEstado)
                .collect(Collectors.toSet());
    }

    // ----- El perfil -----

    @Test
    @DisplayName("devuelve el perfil de alguien que existe")
    void perfilDeUnUsuario() throws ReglaNegocioException {
        DTUsuario perfil = sistema.consultarUsuario("imm", null);

        assertEquals("imm", perfil.getNickname());
        assertTrue(perfil.esOrganizador());
    }

    @Test
    @DisplayName("rechaza un nickname que no existe")
    void perfilInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarUsuario("no-existe-nadie", null));
    }

    // ----- Las ediciones del organizador -----

    @Test
    @DisplayName("el organizador ve sus ediciones en los tres estados")
    void elDuenioVeLosTresEstados() throws ReglaNegocioException {
        Set<EstadoEdicion> suyas = estadosQueVe("imm");

        assertTrue(suyas.contains(EstadoEdicion.ACEPTADA));
        assertTrue(suyas.contains(EstadoEdicion.INGRESADA));
        assertTrue(suyas.contains(EstadoEdicion.RECHAZADA));
    }

    @Test
    @DisplayName("otro usuario y un visitante ven solo las Aceptada")
    void losDemasVenSoloLasAceptadas() throws ReglaNegocioException {
        assertEquals(Set.of(EstadoEdicion.ACEPTADA), estadosQueVe("vale23"));
        assertEquals(Set.of(EstadoEdicion.ACEPTADA), estadosQueVe(null));
    }

    @Test
    @DisplayName("un visitante ve menos ediciones que el propio organizador")
    void elDuenioVeMas() throws ReglaNegocioException {
        int conSesion = sistema.listarEdicionesDeOrganizador("imm", "imm").size();
        int sinSesion = sistema.listarEdicionesDeOrganizador("imm", null).size();

        assertTrue(conSesion > sinSesion,
                "el organizador tiene que ver mas que un visitante");
    }

    @Test
    @DisplayName("pedir las ediciones de un asistente es un error")
    void edicionesDeUnAsistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarEdicionesDeOrganizador("vale23", "vale23"));
    }

    // ----- Los registros del asistente -----

    @Test
    @DisplayName("el asistente ve sus registros en su propio perfil")
    void elDuenioVeSusRegistros() throws ReglaNegocioException {
        assertTrue(sistema.listarRegistrosDeAsistente("vale23", "vale23").size() > 0,
                "vale23 tiene registros en los datos de prueba");
    }

    @Test
    @DisplayName("otro usuario y un visitante no ven ningun registro, y no es un error")
    void losDemasNoVenLosRegistros() throws ReglaNegocioException {
        assertTrue(sistema.listarRegistrosDeAsistente("vale23", "imm").isEmpty());
        assertTrue(sistema.listarRegistrosDeAsistente("vale23", null).isEmpty());
    }

    @Test
    @DisplayName("pedir los registros de un organizador es un error")
    void registrosDeUnOrganizador() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.listarRegistrosDeAsistente("imm", "imm"));
    }
}
