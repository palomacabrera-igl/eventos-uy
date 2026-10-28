package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Inicio de Sesion (CU 1) contra la base, ejercitando Sistema.
 *
 * <pre>
 *  iniciarSesion(identificador, password)
 *  CV  nickname y contrasena correctos        -&gt; devuelve el DT de esa persona
 *  CV  el correo en lugar del nickname        -&gt; la letra acepta los dos
 *  CV  el DT dice si es asistente u organizador
 *  CI  contrasena equivocada                  -&gt; ReglaNegocioException
 *  CI  usuario que no existe                  -&gt; ReglaNegocioException
 *  CI  otra capitalizacion de la contrasena   -&gt; ReglaNegocioException
 *  CI  el mensaje es el MISMO en los dos casos, para no delatar
 *      que usuarios existen
 *  Valor limite: identificador vacio, contrasena vacia, null
 * </pre>
 */
@DisplayName("Inicio de Sesion contra la base (CU 1)")
class IniciarSesionIntegracionTest extends PruebaConBase {

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    // ----- Lo que tiene que dejar entrar -----

    @Test
    @DisplayName("entra con su nickname y su contrasena")
    void entraConNickname() throws ReglaNegocioException {
        DTUsuario quien = sistema.iniciarSesion("vale23", "Vale2026!");

        assertEquals("vale23", quien.getNickname());
        assertTrue(quien.esAsistente());
        assertFalse(quien.esOrganizador());
    }

    @Test
    @DisplayName("entra con su correo, que la letra acepta igual que el nickname")
    void entraConCorreo() throws ReglaNegocioException {
        DTUsuario quien = sistema.iniciarSesion("valentina@mail.com", "Vale2026!");

        assertEquals("vale23", quien.getNickname());
    }

    @Test
    @DisplayName("el DT de un organizador se declara organizador")
    void elTipoViajaEnElDT() throws ReglaNegocioException {
        DTUsuario organizador = sistema.iniciarSesion("imm", "Imm2026!");

        assertTrue(organizador.esOrganizador());
        assertFalse(organizador.esAsistente());
        assertEquals(TipoUsuario.ORGANIZADOR, organizador.getTipo());
    }

    // ----- Lo que NO tiene que dejar entrar -----

    @Test
    @DisplayName("no entra con la contrasena equivocada")
    void contrasenaEquivocada() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion("vale23", "la-que-no-es"));
    }

    @Test
    @DisplayName("no entra un usuario que no existe")
    void usuarioInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion("no-existe-nadie", "Vale2026!"));
    }

    @Test
    @DisplayName("la contrasena distingue mayusculas de minusculas")
    void laContrasenaDistingueMayusculas() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion("vale23", "vale2026!"));
    }

    @Test
    @DisplayName("valor limite: identificador o contrasena vacios, o null")
    void vaciosYNulos() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion("", "Vale2026!"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion("   ", "Vale2026!"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion("vale23", ""));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion(null, "Vale2026!"));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.iniciarSesion("vale23", null));
    }

    // ----- La regla que no se ve en la pantalla -----

    @Test
    @DisplayName("el mensaje no delata si el usuario existe o no")
    void elMensajeNoDelata() {
        String conUsuarioReal = mensajeDe("vale23", "la-que-no-es");
        String conUsuarioFalso = mensajeDe("no-existe-nadie", "la-que-no-es");

        // Si fueran distintos, cualquiera podria ir probando nicknames y
        // armar la lista de quienes estan registrados.
        assertEquals(conUsuarioReal, conUsuarioFalso);
    }

    private String mensajeDe(String identificador, String password) {
        try {
            sistema.iniciarSesion(identificador, password);
            return "(entro, y no deberia)";
        } catch (ReglaNegocioException esperado) {
            return esperado.getMessage();
        }
    }
}
