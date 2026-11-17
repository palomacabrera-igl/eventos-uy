package logica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Validacion asincronica del nickname y del correo (letra, 7.6) contra la
 * base: el formulario de Alta de Usuario pregunta, mientras la persona
 * escribe, si el valor ya esta en uso. Esa pregunta no reemplaza a la
 * validacion de altaUsuario: tienen que decir lo mismo.
 *
 * <pre>
 *  existeNickname(nickname) / existeCorreo(correo)
 *  CV  uno que ya esta en uso (asistente u organizador)  -&gt; true
 *  CV  uno que nadie tiene                               -&gt; false
 *  CV  aparece apenas se da de alta el usuario
 *  CV  con espacios a los costados                       -&gt; los ignora, como el alta
 *  CI  mayusculas distintas, o solo el principio del valor -&gt; false (exacta, como el alta)
 *  Valor limite: null, vacio, solo espacios              -&gt; false
 *  Coherencia: lo que dice "existe", altaUsuario lo rechaza
 * </pre>
 */
@DisplayName("Validacion de disponibilidad del nickname y del correo contra la base")
class ValidacionDisponibilidadIntegracionTest extends PruebaConBase {

    private static final String ANA = "test-vd-ana";
    private static final String ANA_CORREO = "test-vd-ana@mail.com";
    private static final String ORG = "test-vd-org";
    private static final String ORG_CORREO = "test-vd-org@mail.com";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    void armarLosUsuarios() throws ReglaNegocioException {
        sinRastros();
        sistema.altaUsuario(new DTAsistente(ANA, "Ana", ANA_CORREO, "Prueba",
                new DTFecha(1, 1, 2000), null), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null);
        sistema.altaUsuario(new DTOrganizador(ORG, "Org", ORG_CORREO,
                "Organizador de prueba", "https://ejemplo.uy"), TipoUsuario.ORGANIZADOR,
                "Clave1!", "Clave1!", null, null);
    }

    @AfterEach
    void sinRastros() {
        borrar("usuario", "nickname LIKE 'test-vd-%'");
    }

    // ----- Nickname -----

    @ParameterizedTest(name = "\"{0}\" esta en uso")
    @DisplayName("un nickname en uso, de asistente o de organizador, existe")
    @ValueSource(strings = {ANA, ORG})
    void nicknameEnUso(String nickname) {
        assertTrue(sistema.existeNickname(nickname));
    }

    @Test
    @DisplayName("un nickname que nadie tiene no existe")
    void nicknameLibre() {
        assertFalse(sistema.existeNickname("test-vd-libre"));
    }

    @Test
    @DisplayName("los espacios a los costados no importan, como en el alta")
    void nicknameConEspacios() {
        assertTrue(sistema.existeNickname("  " + ANA + " "));
    }

    @ParameterizedTest(name = "\"{0}\" no es el nickname de nadie")
    @DisplayName("la comparacion es exacta: otras mayusculas o solo el principio no cuentan")
    @ValueSource(strings = {"TEST-VD-ANA", "test-vd-an", "test-vd-ana2"})
    void nicknameParecido(String nickname) {
        assertFalse(sistema.existeNickname(nickname));
    }

    @ParameterizedTest(name = "nickname \"{0}\"")
    @DisplayName("valor limite: null, vacio o solo espacios no existe")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void nicknameVacio(String nickname) {
        assertFalse(sistema.existeNickname(nickname));
    }

    // ----- Correo -----

    @ParameterizedTest(name = "\"{0}\" esta en uso")
    @DisplayName("un correo en uso, de asistente o de organizador, existe")
    @ValueSource(strings = {ANA_CORREO, ORG_CORREO})
    void correoEnUso(String correo) {
        assertTrue(sistema.existeCorreo(correo));
    }

    @Test
    @DisplayName("un correo que nadie tiene no existe")
    void correoLibre() {
        assertFalse(sistema.existeCorreo("test-vd-libre@mail.com"));
    }

    @Test
    @DisplayName("los espacios a los costados del correo no importan")
    void correoConEspacios() {
        assertTrue(sistema.existeCorreo(" " + ANA_CORREO + "  "));
    }

    @ParameterizedTest(name = "\"{0}\" no es el correo de nadie")
    @DisplayName("la comparacion es exacta: otras mayusculas o solo el principio no cuentan")
    @ValueSource(strings = {"TEST-VD-ANA@MAIL.COM", "test-vd-ana@mail", "test-vd-ana"})
    void correoParecido(String correo) {
        assertFalse(sistema.existeCorreo(correo));
    }

    @ParameterizedTest(name = "correo \"{0}\"")
    @DisplayName("valor limite: null, vacio o solo espacios no existe")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void correoVacio(String correo) {
        assertFalse(sistema.existeCorreo(correo));
    }

    // ----- Coherencia con el alta -----

    @Test
    @DisplayName("el valor aparece apenas se da de alta el usuario, sin esperar nada")
    void apareceAlDarDeAlta() throws ReglaNegocioException {
        assertFalse(sistema.existeNickname("test-vd-nuevo"));
        assertFalse(sistema.existeCorreo("test-vd-nuevo@mail.com"));

        sistema.altaUsuario(new DTAsistente("test-vd-nuevo", "Nuevo", "test-vd-nuevo@mail.com",
                "Prueba", new DTFecha(1, 1, 2000), null), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null);

        assertTrue(sistema.existeNickname("test-vd-nuevo"));
        assertTrue(sistema.existeCorreo("test-vd-nuevo@mail.com"));
    }

    @Test
    @DisplayName("lo que dice que existe, altaUsuario lo rechaza: el nickname")
    void elAltaRechazaElNickname() {
        assertTrue(sistema.existeNickname(ANA));

        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                new DTAsistente(ANA, "Otra", "test-vd-otra@mail.com", "Prueba",
                        new DTFecha(1, 1, 2000), null), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null));
    }

    @Test
    @DisplayName("lo que dice que existe, altaUsuario lo rechaza: el correo")
    void elAltaRechazaElCorreo() {
        assertTrue(sistema.existeCorreo(ANA_CORREO));

        assertThrows(ReglaNegocioException.class, () -> sistema.altaUsuario(
                new DTAsistente("test-vd-otra", "Otra", ANA_CORREO, "Prueba",
                        new DTFecha(1, 1, 2000), null), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null));
    }

    @Test
    @DisplayName("lo que dice que esta libre, altaUsuario lo acepta")
    void elAltaAceptaLoLibre() throws ReglaNegocioException {
        assertFalse(sistema.existeNickname("test-vd-libre"));
        assertFalse(sistema.existeCorreo("test-vd-libre@mail.com"));

        sistema.altaUsuario(new DTAsistente("test-vd-libre", "Libre", "test-vd-libre@mail.com",
                "Prueba", new DTFecha(1, 1, 2000), null), TipoUsuario.ASISTENTE,
                "Clave1!", "Clave1!", null, null);

        assertTrue(sistema.iniciarSesion("test-vd-libre", "Clave1!").esAsistente());
    }
}
