package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de caja negra de Consulta de Registro (CU 13) sobre las entidades,
 * sin base de datos. La letra: el asistente ve sus registros, y el
 * organizador los registros de sus ediciones; nadie mas.
 *
 * <pre>
 *  obtenerRegistrosPara(solicitante)
 *  CV  el organizador de la edicion            -&gt; todos los registros
 *  CI  un asistente, otro organizador          -&gt; ReglaNegocioException
 *  Valor limite: visitante (null), vacio; edicion sin registros -&gt; vacio
 *
 *  obtenerRegistroPara(nickname, solicitante)
 *  CV  el propio asistente, el organizador     -&gt; el registro
 *  CI  otro asistente, otro organizador        -&gt; ReglaNegocioException
 *  CI  un nickname que no esta registrado      -&gt; ReglaNegocioException
 *  Valor limite: visitante (null)
 * </pre>
 */
@DisplayName("Consulta de Registro (CU 13)")
class ConsultaRegistroTest {

    private EdicionEvento edicion;
    private TipoRegistro general;

    @BeforeEach
    void armarLaEdicion() {
        Organizador duenio = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos", null, "Imm2026!");
        edicion = new EdicionEvento("JIAP 2027", "JIAP",
                LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 5),
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", duenio);
        general = edicion.crearTipoRegistro("General", "Entrada general", 500.0, 100);
    }

    private void registrar(String nickname) {
        Asistente asistente = new Asistente(nickname, "Nombre", nickname + "@mail.com",
                "Apellido", LocalDate.of(2000, 1, 1));
        edicion.altaRegistro(asistente, general, LocalDate.of(2026, 10, 28));
    }

    // ----- El listado de la edicion -----

    @Test
    @DisplayName("el organizador ve todos los registros de su edicion")
    void elOrganizadorVeTodos() throws ReglaNegocioException {
        registrar("ana");
        registrar("beto");

        Set<DTRegistro> registros = edicion.obtenerRegistrosPara("imm");

        assertEquals(2, registros.size());
    }

    @ParameterizedTest(name = "\"{0}\" no ve los registros de la edicion")
    @DisplayName("un asistente u otro organizador no ven el listado")
    @ValueSource(strings = {"ana", "antel", "IMM"})
    void otroNoVeElListado(String solicitante) {
        registrar("ana");

        assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerRegistrosPara(solicitante));
    }

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("valor limite: un visitante no ve el listado")
    @NullAndEmptySource
    void visitanteNoVeElListado(String solicitante) {
        assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerRegistrosPara(solicitante));
    }

    @Test
    @DisplayName("valor limite: una edicion sin registros devuelve el conjunto vacio")
    void sinRegistros() throws ReglaNegocioException {
        assertTrue(edicion.obtenerRegistrosPara("imm").isEmpty());
    }

    // ----- Un registro -----

    @Test
    @DisplayName("el asistente ve su propio registro, con fecha, tipo, costo y si uso codigo")
    void elAsistenteVeElSuyo() throws ReglaNegocioException {
        registrar("ana");

        DTRegistro dt = edicion.obtenerRegistroPara("ana", "ana");

        assertEquals("ana", dt.getNickname());
        assertEquals("JIAP 2027", dt.getNombreEdicion());
        assertEquals("General", dt.getTipoRegistro());
        assertEquals(500.0, dt.getCosto());
        assertEquals(LocalDate.of(2026, 10, 28), dt.getFechaRegistro());
        assertFalse(dt.getUsoCodigo());
    }

    @Test
    @DisplayName("el organizador ve el registro de cualquier asistente de su edicion")
    void elOrganizadorVeCualquiera() throws ReglaNegocioException {
        registrar("ana");

        assertEquals("ana", edicion.obtenerRegistroPara("ana", "imm").getNickname());
    }

    @ParameterizedTest(name = "\"{0}\" no ve el registro de ana")
    @DisplayName("otro asistente u otro organizador no ven el registro")
    @ValueSource(strings = {"beto", "antel"})
    void otroNoLoVe(String solicitante) {
        registrar("ana");
        registrar("beto");

        assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerRegistroPara("ana", solicitante));
    }

    @Test
    @DisplayName("valor limite: un visitante no ve ningun registro")
    void visitanteNoLoVe() {
        registrar("ana");

        assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerRegistroPara("ana", null));
    }

    @Test
    @DisplayName("un nickname que no esta registrado da error, aunque pregunte el organizador")
    void noRegistrado() {
        registrar("ana");

        assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerRegistroPara("zoe", "imm"));
    }

    @Test
    @DisplayName("si no le corresponde, el mensaje es el mismo que si no existiera")
    void mismoMensaje() {
        registrar("ana");

        String noLeToca = assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerRegistroPara("ana", "beto")).getMessage();
        String noExiste = assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerRegistroPara("zoe", "imm")).getMessage();

        assertEquals(noExiste, noLeToca);
    }
}
