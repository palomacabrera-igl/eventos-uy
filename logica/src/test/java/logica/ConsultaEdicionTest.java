package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de Consulta de Edicion (CU 9), sin base de datos: la regla de quien
 * ve que vive en EdicionEvento y Sistema solo busca y delega.
 *
 * <pre>
 *  puedeVerla(solicitante) -- letra, seccion 4
 *  CV  edicion Aceptada, cualquiera (visitante incluido)  -&gt; true
 *  CV  edicion Ingresada o Rechazada, su organizador      -&gt; true
 *  CI  Ingresada o Rechazada, otro organizador            -&gt; false
 *  CI  Ingresada o Rechazada, un asistente                -&gt; false
 *  CI  Ingresada o Rechazada, visitante (null)            -&gt; false
 *
 *  obtenerDTCompletoPara(solicitante) -- requisito R14
 *  CV  su organizador     -&gt; con registros y codigos de patrocinio
 *  CI  cualquier otro     -&gt; sin registros y con los patrocinios sin codigo
 *  Los tipos de registro y los patrocinios los ven todos.
 * </pre>
 */
@DisplayName("Consulta de Edicion (CU 9)")
class ConsultaEdicionTest {

    private Organizador duenio;
    private Evento evento;
    private EdicionEvento edicion;

    @BeforeEach
    void armarLaEdicion() {
        duenio = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos", null, "Imm2026!");
        evento = new Evento("Maratón de Montevideo", "Carrera anual",
                LocalDate.of(2024, 3, 5), "MARA", List.of(new Categoria("Deporte")));
        edicion = new EdicionEvento("Maratón 2027", "M27", LocalDate.of(2027, 5, 10),
                LocalDate.of(2027, 5, 10), LocalDate.of(2026, 10, 28),
                "Montevideo", "Uruguay", duenio);
        evento.agregarEdicion(edicion);

        // Un tipo de registro, un patrocinio con codigo y un registro.
        TipoRegistro general = edicion.crearTipoRegistro("General", "Entrada general", 500.0, 100);
        Institucion antel = new Institucion("ANTEL", "Telecomunicaciones", "https://antel.com.uy");
        edicion.agregarPatrocinio(new Patrocinio(LocalDate.of(2026, 10, 28), 100000.0, 5,
                "ANTEL-M27", NivelPatrocinio.ORO, antel, general));
        Asistente vale = new Asistente("vale23", "Valentina", "valentina@mail.com",
                "Pereira", LocalDate.of(2000, 5, 10), "Vale2026!");
        edicion.altaRegistro(vale, general, LocalDate.of(2026, 10, 28));
    }

    // ----- puedeVerla -----

    @ParameterizedTest(name = "\"{0}\" ve una edicion Aceptada")
    @DisplayName("una edicion Aceptada la ve cualquiera")
    @NullSource
    @ValueSource(strings = {"imm", "utec", "vale23"})
    void aceptadaLaVeCualquiera(String solicitante) throws ReglaNegocioException {
        edicion.aceptar();

        assertTrue(edicion.puedeVerla(solicitante));
    }

    @Test
    @DisplayName("una edicion Ingresada la ve su organizador")
    void ingresadaLaVeSuOrganizador() {
        assertTrue(edicion.puedeVerla("imm"));
    }

    @ParameterizedTest(name = "\"{0}\" no ve una edicion Ingresada")
    @DisplayName("una edicion Ingresada no la ve nadie mas")
    @NullSource
    @ValueSource(strings = {"utec", "vale23", "Imm", ""})
    void ingresadaNoLaVeOtro(String solicitante) {
        assertFalse(edicion.puedeVerla(solicitante));
    }

    @Test
    @DisplayName("una edicion Rechazada solo la ve su organizador")
    void rechazadaSoloSuOrganizador() throws ReglaNegocioException {
        edicion.rechazar();

        assertTrue(edicion.puedeVerla("imm"));
        assertFalse(edicion.puedeVerla("utec"));
        assertFalse(edicion.puedeVerla(null));
    }

    // ----- obtenerDTCompletoPara -----

    @Test
    @DisplayName("su organizador recibe los registros y los codigos de patrocinio")
    void organizadorVeTodo() {
        // Ejecucion
        DTEdicionCompleto dt = edicion.obtenerDTCompletoPara("imm");

        // Resultado esperado y comparacion
        assertEquals(1, dt.getRegistros().size());
        assertEquals("vale23", dt.getRegistros().iterator().next().getNickname());
        assertEquals("ANTEL-M27", dt.getPatrocinios().iterator().next().getCodigoPatrocinio());
    }

    @ParameterizedTest(name = "\"{0}\" no recibe registros ni codigos")
    @DisplayName("cualquier otro recibe la edicion sin registros y sin codigos")
    @NullSource
    @ValueSource(strings = {"utec", "vale23"})
    void otroNoVeRegistrosNiCodigos(String solicitante) throws ReglaNegocioException {
        edicion.aceptar();

        DTEdicionCompleto dt = edicion.obtenerDTCompletoPara(solicitante);

        assertTrue(dt.getRegistros().isEmpty());
        assertEquals(1, dt.getPatrocinios().size());
        DTPatrocinio patrocinio = dt.getPatrocinios().iterator().next();
        assertNull(patrocinio.getCodigoPatrocinio());
        assertEquals("ANTEL", patrocinio.getInstitucion());
        assertEquals(1, dt.getTiposRegistro().size());
    }

    @Test
    @DisplayName("el DT trae el estado, el organizador y el evento de la edicion")
    void datosDeLaEdicion() {
        DTEdicionCompleto dt = edicion.obtenerDTCompletoPara(null);

        assertEquals("Maratón 2027", dt.getNombre());
        assertEquals(EstadoEdicion.INGRESADA, dt.getEstado());
        assertEquals("imm", dt.getOrganizador().getNickname());
        assertEquals("Maratón de Montevideo", dt.getNombreEvento());
    }
}
