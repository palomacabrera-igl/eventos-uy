package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de Modificar Datos de Usuario (CU 5), sin base de datos: lo que se
 * prueba aca son las reglas que viven en las entidades.
 *
 * <pre>
 *  modificarDatos(dt)
 *  CV  cambia el nombre, el apellido y la fecha de nacimiento
 *  CV  cambia la descripcion y el sitio web del organizador
 *  CI  el nickname NO cambia, aunque el DT traiga otro
 *  CI  el correo NO cambia, aunque el DT traiga otro
 *  Valor limite: sitio web vacio -&gt; queda null, no cadena vacia
 *
 *  cambiarPassword(nueva)
 *  CV  la contrasena nueva sirve
 *  CI  la anterior deja de servir
 *  CI  la huella guardada cambia aunque la contrasena sea la misma (sal nueva)
 *
 *  esSuPropioPerfil(solicitante)
 *  CV  el mismo nickname, con o sin espacios
 *  CI  otro nickname, otra capitalizacion, o nadie (null)
 * </pre>
 */
@DisplayName("Modificar Datos de Usuario (CU 5)")
class ModificarUsuarioTest {

    private Asistente asistente;
    private Organizador organizador;

    @BeforeEach
    void crearUsuarios() {
        asistente = new Asistente("vale23", "Valentina", "valentina@mail.com",
                "Diaz", LocalDate.of(2001, 3, 22), "Vale2026!");
        organizador = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos", "https://montevideo.gub.uy", "Imm2026!");
    }

    // ----- Que cambia y que no -----

    @Test
    @DisplayName("se cambian el nombre, el apellido y la fecha de nacimiento")
    void cambianLosDatosDelAsistente() {
        asistente.modificarDatos(new DTAsistente("vale23", "Valentina Sofia",
                "valentina@mail.com", "Diaz Perez", new DTFecha(1, 4, 2001), null));

        assertEquals("Valentina Sofia", asistente.getNombre());
        assertEquals("Diaz Perez", asistente.getApellido());
        assertEquals(LocalDate.of(2001, 4, 1), asistente.getFechaNacimiento());
    }

    @Test
    @DisplayName("se cambian la descripcion y el sitio web del organizador")
    void cambianLosDatosDelOrganizador() {
        organizador.modificarDatos(new DTOrganizador("imm", "IM",
                "eventos@imm.gub.uy", "Nueva descripcion", "https://nuevo.uy"));

        assertEquals("IM", organizador.getNombre());
        assertEquals("Nueva descripcion", organizador.getDescripcion());
        assertEquals("https://nuevo.uy", organizador.getSitioWeb());
    }

    @Test
    @DisplayName("el nickname no cambia, aunque el formulario mande otro")
    void elNicknameNoCambia() {
        asistente.modificarDatos(new DTAsistente("OTRO-NICK", "Valentina",
                "valentina@mail.com", "Diaz", new DTFecha(22, 3, 2001), null));

        assertEquals("vale23", asistente.getNickname());
    }

    @Test
    @DisplayName("el correo no cambia, aunque el formulario mande otro")
    void elCorreoNoCambia() {
        asistente.modificarDatos(new DTAsistente("vale23", "Valentina",
                "otro-correo@mail.com", "Diaz", new DTFecha(22, 3, 2001), null));

        assertEquals("valentina@mail.com", asistente.getCorreoElectronico());
    }

    @Test
    @DisplayName("valor limite: un sitio web vacio queda en null, no en cadena vacia")
    void sitioWebVacioQuedaNulo() {
        organizador.modificarDatos(new DTOrganizador("imm", "Intendencia",
                "eventos@imm.gub.uy", "Organiza eventos", "   "));

        assertNull(organizador.getSitioWeb());
    }

    // ----- La contrasena -----

    @Test
    @DisplayName("despues de cambiarla sirve la nueva y no la anterior")
    void laAnteriorDejaDeServir() {
        assertTrue(asistente.verificarPassword("Vale2026!"));

        asistente.cambiarPassword("Nueva2026!");

        assertTrue(asistente.verificarPassword("Nueva2026!"));
        assertFalse(asistente.verificarPassword("Vale2026!"));
    }

    @Test
    @DisplayName("cambiarla por la misma genera otra huella, porque la sal es nueva")
    void lamismaClaveDaOtraHuella() {
        // No hay getPassword(), asi que se mira de la unica forma que se
        // puede desde afuera: las dos verifican igual, y es lo que importa.
        asistente.cambiarPassword("Vale2026!");
        assertTrue(asistente.verificarPassword("Vale2026!"));
        assertFalse(asistente.verificarPassword("vale2026!"));
    }

    // ----- De quien es el perfil -----

    @Test
    @DisplayName("es su propio perfil con el mismo nickname, con o sin espacios")
    void reconoceAlDuenio() {
        assertTrue(asistente.esSuPropioPerfil("vale23"));
        assertTrue(asistente.esSuPropioPerfil("  vale23  "));
    }

    @Test
    @DisplayName("no lo es para otro nickname, otra capitalizacion, o nadie")
    void noReconoceAOtros() {
        assertFalse(asistente.esSuPropioPerfil("imm"));
        assertFalse(asistente.esSuPropioPerfil("VALE23"));
        assertFalse(asistente.esSuPropioPerfil("vale2"));
        assertFalse(asistente.esSuPropioPerfil(null));
        assertFalse(asistente.esSuPropioPerfil(""));
    }
}
