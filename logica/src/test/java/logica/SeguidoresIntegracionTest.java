package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Seguidores contra PostgreSQL, a traves del controlador de Fabrica. Cubre la
 * relacion en ambos sentidos y las reglas que no puede suplir la restriccion
 * unica de la base.
 */
@DisplayName("Seguidores contra la base")
class SeguidoresIntegracionTest extends PruebaConBase {

    private static final String ANA = "test-seguidora-ana";
    private static final String BETO = "test-seguidor-beto";
    private static final String CATA = "test-seguidora-cata";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    void crearUsuarios() throws ReglaNegocioException {
        borrarRastros();
        altaAsistente(ANA);
        altaOrganizador(BETO);
        altaAsistente(CATA);
    }

    @AfterEach
    void borrarUsuarios() {
        borrarRastros();
    }

    private void borrarRastros() {
        borrar("usuario_seguidor", "seguidor_id IN (SELECT id FROM usuario WHERE nickname LIKE 'test-seguidor%')"
                + " OR seguido_id IN (SELECT id FROM usuario WHERE nickname LIKE 'test-seguidor%')");
        borrar("usuario", "nickname LIKE 'test-seguidor%'");
    }

    private void altaAsistente(String nickname) throws ReglaNegocioException {
        sistema.altaUsuario(new DTAsistente(nickname, "Nombre", nickname + "@mail.com",
                        "Apellido", new DTFecha(1, 1, 2000), null),
                TipoUsuario.ASISTENTE, "Clave1!", "Clave1!", null, null);
    }

    private void altaOrganizador(String nickname) throws ReglaNegocioException {
        sistema.altaUsuario(new DTOrganizador(nickname, "Nombre", nickname + "@mail.com",
                        "Organizador de prueba", null),
                TipoUsuario.ORGANIZADOR, "Clave1!", "Clave1!", null, null);
    }

    private List<String> nicknames(List<DTUsuario> usuarios) {
        return usuarios.stream().map(DTUsuario::getNickname).toList();
    }

    @Test
    @DisplayName("seguir vincula ambos perfiles y los listados quedan ordenados")
    void seguirVinculaAmbosLados() throws ReglaNegocioException {
        sistema.seguirUsuario(CATA, BETO);
        sistema.seguirUsuario(ANA, BETO);

        assertEquals(List.of(ANA, CATA), nicknames(sistema.listarSeguidores(BETO)));
        assertEquals(List.of(BETO), nicknames(sistema.listarSeguidos(ANA)));
        assertEquals(List.of(BETO), nicknames(sistema.listarSeguidos(CATA)));
    }

    @Test
    @DisplayName("dejar de seguir elimina el vinculo de ambos perfiles")
    void dejarDeSeguirEliminaAmbosLados() throws ReglaNegocioException {
        sistema.seguirUsuario(ANA, BETO);
        sistema.dejarDeSeguirUsuario(ANA, BETO);

        assertTrue(sistema.listarSeguidores(BETO).isEmpty());
        assertTrue(sistema.listarSeguidos(ANA).isEmpty());
    }

    @Test
    @DisplayName("rechaza seguirse a si mismo y repetir un seguimiento")
    void rechazaSeguimientosInvalidos() throws ReglaNegocioException {
        assertThrows(ReglaNegocioException.class, () -> sistema.seguirUsuario(ANA, ANA));

        sistema.seguirUsuario(ANA, BETO);
        assertThrows(ReglaNegocioException.class, () -> sistema.seguirUsuario(ANA, BETO));
    }

    @Test
    @DisplayName("rechaza dejar de seguir cuando no existe el vinculo o el usuario")
    void rechazaDejarDeSeguirInvalido() {
        assertThrows(ReglaNegocioException.class, () -> sistema.dejarDeSeguirUsuario(ANA, BETO));
        assertThrows(ReglaNegocioException.class,
                () -> sistema.seguirUsuario(ANA, "no-existe-nadie"));
    }
}
