package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Alta de Usuario (CU 3) contra la base, ejercitando Sistema.
 *
 * <pre>
 *  altaUsuario(datos, tipo, password, confirmacion, institucion, imagen)
 *  CV  asistente con institucion              -&gt; queda creado y puede entrar
 *  CV  organizador con imagen                 -&gt; queda creado y con imagen
 *  CV  asistente sin institucion              -&gt; la institucion es opcional
 *  CI  nickname repetido                      -&gt; lo dice, para poder corregir
 *  CI  correo repetido                        -&gt; idem
 *  CI  contrasena y confirmacion distintas
 *  CI  institucion que no existe
 *  CI  el DT no corresponde al tipo pedido
 *  Valor limite: nickname, nombre, correo o contrasena vacios; apellido
 *                vacio; descripcion vacia
 * </pre>
 */
@DisplayName("Alta de Usuario contra la base (CU 3)")
class AltaUsuarioIntegracionTest extends PruebaConBase {

    private static final String ASIS = "test-it-asis";
    private static final String ORG = "test-it-org";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    @AfterEach
    void sinRastros() {
        borrar("usuario", "nickname LIKE 'test-it-%'");
    }

    private DTAsistente asistente(String nickname, String correo) {
        return new DTAsistente(nickname, "Test", correo, "Prueba",
                new DTFecha(1, 1, 2000), null);
    }

    private DTOrganizador organizador(String nickname, String correo) {
        return new DTOrganizador(nickname, "Test", correo,
                "Organizador de prueba", "https://ejemplo.uy");
    }

    // ----- Las altas que tienen que salir bien -----

    @Test
    @DisplayName("crea un asistente con institucion, y despues puede iniciar sesion")
    void altaDeAsistente() throws ReglaNegocioException {
        sistema.altaUsuario(asistente(ASIS, "test-it@mail.com"), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", "UTEC", null);

        // Que pueda entrar es la prueba de que la contrasena quedo bien
        // hasheada: si se guardara mal, verificarPassword fallaria.
        DTUsuario creado = sistema.iniciarSesion(ASIS, "Clave1!");
        assertEquals(ASIS, creado.getNickname());
        assertTrue(creado.esAsistente());
        assertEquals("UTEC", ((DTAsistente) sistema.consultarUsuario(ASIS, ASIS)).getInstitucion());
    }

    @Test
    @DisplayName("crea un asistente sin institucion, que es opcional")
    void altaSinInstitucion() throws ReglaNegocioException {
        sistema.altaUsuario(asistente(ASIS, "test-it@mail.com"), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null);

        assertTrue(sistema.iniciarSesion(ASIS, "Clave1!").esAsistente());
    }

    @Test
    @DisplayName("crea un organizador con imagen")
    void altaDeOrganizadorConImagen() throws ReglaNegocioException {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G'};

        sistema.altaUsuario(organizador(ORG, "test-it-org@mail.com"), TipoUsuario.ORGANIZADOR,
                "Clave1!", "Clave1!", null, new DTImagen(png, "image/png"));

        DTUsuario creado = sistema.iniciarSesion(ORG, "Clave1!");
        assertTrue(creado.esOrganizador());
        assertTrue(creado.getTieneImagen());
    }

    // ----- Lo que no tiene que dejar crear -----

    @Test
    @DisplayName("rechaza un nickname que ya existe")
    void nicknameRepetido() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaUsuario(asistente("vale23", "otro-correo@mail.com"),
                        TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", null, null));

        // Aca el mensaje SI dice que esta repetido: quien se registra
        // necesita saber que corregir.
        assertTrue(error.getMessage().contains("vale23"));
    }

    @Test
    @DisplayName("rechaza un correo que ya existe")
    void correoRepetido() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaUsuario(asistente(ASIS, "valentina@mail.com"),
                        TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", null, null));

        assertTrue(error.getMessage().contains("valentina@mail.com"));
    }

    @Test
    @DisplayName("rechaza si la contrasena no coincide con su confirmacion")
    void contrasenasDistintas() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaUsuario(asistente(ASIS, "test-it@mail.com"),
                        TipoUsuario.ASISTENTE, "Clave1!", "Otra2!", null, null));
    }

    @Test
    @DisplayName("rechaza una institucion que no existe")
    void institucionInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaUsuario(asistente(ASIS, "test-it@mail.com"),
                        TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", "Instituto Fantasma", null));
    }

    @Test
    @DisplayName("rechaza si los datos no son los del tipo pedido")
    void elTipoNoCoincide() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaUsuario(asistente(ASIS, "test-it@mail.com"),
                        TipoUsuario.ORGANIZADOR, "Clave1!", "Clave1!", null, null));
    }

    @ParameterizedTest(name = "el nickname es \"{0}\"")
    @DisplayName("valor limite: el nickname vacio o en blanco")
    @ValueSource(strings = {"", " ", "   "})
    void nicknameVacio(String nickname) {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                asistente(nickname, "test-it@mail.com"), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null));
    }

    @ParameterizedTest(name = "el correo es \"{0}\"")
    @DisplayName("valor limite: el correo vacio o en blanco")
    @ValueSource(strings = {"", "   "})
    void correoVacio(String correo) {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                asistente(ASIS, correo), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null));
    }

    @Test
    @DisplayName("valor limite: la contrasena vacia")
    void contrasenaVacia() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                asistente(ASIS, "test-it@mail.com"), TipoUsuario.ASISTENTE,
                "", "", null, null));
    }

    @Test
    @DisplayName("valor limite: sin datos (null)")
    void sinDatos() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                null, TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", null, null));
    }

    @Test
    @DisplayName("valor limite: al asistente le falta el apellido")
    void asistenteSinApellido() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                new DTAsistente(ASIS, "Test", "test-it@mail.com", "",
                        new DTFecha(1, 1, 2000), null),
                TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", null, null));
    }

    @Test
    @DisplayName("valor limite: al organizador le falta la descripcion")
    void organizadorSinDescripcion() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                new DTOrganizador(ORG, "Test", "test-it-org@mail.com", "", null),
                TipoUsuario.ORGANIZADOR, "Clave1!", "Clave1!", null, null));
    }

    @Test
    @DisplayName("el mismo nickname no se puede crear dos veces seguidas")
    void noSePuedeRepetirElAltaRecienHecha() throws ReglaNegocioException {
        sistema.altaUsuario(asistente(ASIS, "test-it@mail.com"), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null);

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaUsuario(asistente(ASIS, "otro-test-it@mail.com"),
                        TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", null, null));
    }
}
