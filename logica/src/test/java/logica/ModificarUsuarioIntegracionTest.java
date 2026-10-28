package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Modificar Datos de Usuario (CU 5) contra la base, ejercitando Sistema.
 *
 * Trabaja siempre sobre un usuario propio que crea y borra, para no tocar
 * los datos de prueba que usan las demas pruebas y la defensa.
 *
 * <pre>
 *  modificarUsuario(solicitante, datos, nuevaPassword, confirmacion, imagen)
 *  CV  cambiar el nombre sin tocar la contrasena -&gt; la anterior sigue sirviendo
 *  CV  cambiar la contrasena                     -&gt; la nueva sirve y la vieja no
 *  CV  poner y sacar la institucion              -&gt; las dos cosas valen
 *  CV  cambiar la imagen                         -&gt; y sin elegir ninguna se conserva
 *  CI  el nickname NO cambia aunque lo manden
 *  CI  el correo NO cambia aunque lo manden
 *  CI  modificar los datos de otra persona
 *  CI  contrasena y confirmacion distintas
 *  CI  institucion que no existe
 *  Valor limite: sin sesion, sin datos, nombre vacio, apellido vacio
 * </pre>
 */
@DisplayName("Modificar Datos de Usuario contra la base (CU 5)")
class ModificarUsuarioIntegracionTest extends PruebaConBase {

    private static final String NICK = "test-it-mod";
    private static final String CORREO = "test-it-mod@mail.com";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    void crearElUsuarioDePrueba() throws ReglaNegocioException {
        borrar("usuario", "nickname = '" + NICK + "'");
        sistema.altaUsuario(datos("Antes", "Apellido", null), TipoUsuario.ASISTENTE,
                "Vieja1!", "Vieja1!", null, null);
    }

    @AfterEach
    void sinRastros() {
        borrar("usuario", "nickname = '" + NICK + "'");
    }

    private DTAsistente datos(String nombre, String apellido, String institucion) {
        return new DTAsistente(NICK, nombre, CORREO, apellido,
                new DTFecha(1, 1, 2000), institucion);
    }

    private DTAsistente leer() throws ReglaNegocioException {
        return (DTAsistente) sistema.consultarUsuario(NICK, NICK);
    }

    private boolean entraCon(String password) {
        try {
            sistema.iniciarSesion(NICK, password);
            return true;
        } catch (ReglaNegocioException no) {
            return false;
        }
    }

    // ----- Lo que tiene que dejar cambiar -----

    @Test
    @DisplayName("cambia el nombre y el apellido sin tocar la contrasena")
    void cambiaLosDatos() throws ReglaNegocioException {
        sistema.modificarUsuario(NICK, datos("Despues", "Nuevo", null), "", "", null);

        assertEquals("Despues", leer().getNombre());
        assertEquals("Nuevo", leer().getApellido());
        assertTrue(entraCon("Vieja1!"), "los dos campos vacios significan dejarla como esta");
    }

    @Test
    @DisplayName("cambia la contrasena: la nueva sirve y la anterior deja de servir")
    void cambiaLaContrasena() throws ReglaNegocioException {
        sistema.modificarUsuario(NICK, datos("Antes", "Apellido", null),
                "Nueva1!", "Nueva1!", null);

        assertTrue(entraCon("Nueva1!"));
        assertFalse(entraCon("Vieja1!"));
    }

    @Test
    @DisplayName("pone y saca la institucion, que las dos cosas son validas")
    void ponerYSacarLaInstitucion() throws ReglaNegocioException {
        sistema.modificarUsuario(NICK, datos("Antes", "Apellido", "UTEC"), "", "", null);
        assertEquals("UTEC", leer().getInstitucion());

        sistema.modificarUsuario(NICK, datos("Antes", "Apellido", ""), "", "", null);
        assertNull(leer().getInstitucion());
    }

    @Test
    @DisplayName("cambia la imagen, y sin elegir ninguna conserva la que tenia")
    void cambiaLaImagen() throws ReglaNegocioException {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G'};
        assertFalse(leer().getTieneImagen());

        sistema.modificarUsuario(NICK, datos("Antes", "Apellido", null), "", "",
                new DTImagen(png, "image/png"));
        assertTrue(leer().getTieneImagen());

        sistema.modificarUsuario(NICK, datos("Antes", "Apellido", null), "", "", null);
        assertTrue(leer().getTieneImagen(), "sin elegir archivo se conserva la que ya estaba");
    }

    // ----- Lo que no tiene que cambiar nunca -----

    @Test
    @DisplayName("el nickname y el correo no cambian aunque el formulario mande otros")
    void nicknameYCorreoNoCambian() throws ReglaNegocioException {
        sistema.modificarUsuario(NICK,
                new DTAsistente(NICK, "Antes", "OTRO-CORREO@mail.com", "Apellido",
                        new DTFecha(1, 1, 2000), null),
                "", "", null);

        assertEquals(NICK, leer().getNickname());
        assertEquals(CORREO, leer().getCorreo());
    }

    // ----- Lo que no tiene que dejar hacer -----

    @Test
    @DisplayName("no deja modificar los datos de otra persona")
    void losDatosDeOtro() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.modificarUsuario(NICK,
                        new DTAsistente("vale23", "Robada", "valentina@mail.com", "Diaz",
                                new DTFecha(1, 1, 2000), null),
                        "", "", null));
    }

    @Test
    @DisplayName("rechaza una contrasena que no coincide con su confirmacion")
    void contrasenasDistintas() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.modificarUsuario(NICK, datos("Antes", "Apellido", null),
                        "Nueva1!", "Otra2!", null));
    }

    @Test
    @DisplayName("rechaza una institucion que no existe")
    void institucionInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.modificarUsuario(NICK,
                        datos("Antes", "Apellido", "Instituto Fantasma"), "", "", null));
    }

    @Test
    @DisplayName("valor limite: sin sesion")
    void sinSesion() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.modificarUsuario(null, datos("Antes", "Apellido", null),
                        "", "", null));
    }

    @Test
    @DisplayName("valor limite: sin datos")
    void sinDatos() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.modificarUsuario(NICK, null, "", "", null));
    }

    @ParameterizedTest(name = "el nombre es \"{0}\"")
    @DisplayName("valor limite: el nombre vacio o en blanco")
    @ValueSource(strings = {"", "  "})
    void nombreVacio(String nombre) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.modificarUsuario(NICK, datos(nombre, "Apellido", null),
                        "", "", null));
    }

    @ParameterizedTest(name = "el apellido es \"{0}\"")
    @DisplayName("valor limite: el apellido vacio o en blanco")
    @ValueSource(strings = {"", "  "})
    void apellidoVacio(String apellido) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.modificarUsuario(NICK, datos("Antes", apellido, null),
                        "", "", null));
    }
}
