package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Registro de Asistencia a Edicion (letra, 6.3) sobre las entidades, sin base de
 * datos. La letra: el asistente registrado confirma que asistio; si ya lo habia
 * hecho, el sistema lo indica y no deja registrarla otra vez. Ademas la edicion
 * tiene que estar Aceptada y haber empezado.
 *
 * <pre>
 *  EdicionEvento.registrarAsistencia(nickname, hoy)
 *  CV  un asistente registrado, edicion Aceptada y empezada -&gt; "asistio"
 *  CV  solo el suyo: los demas siguen igual
 *  CI  la segunda vez                        -&gt; ReglaNegocioException
 *  CI  un asistente que no se registro, el organizador -&gt; ReglaNegocioException
 *  CI  edicion que no esta Aceptada          -&gt; ReglaNegocioException
 *  CI  edicion que todavia no empezo         -&gt; ReglaNegocioException
 *  Valor limite: el dia de inicio si, el dia anterior no; despues del fin si
 *  Valor limite: null, vacio, solo espacios; el nickname con espacios a los costados
 * </pre>
 */
@DisplayName("Registro de Asistencia a Edicion")
class RegistroAsistenciaTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 3, 1);
    private static final LocalDate FIN = LocalDate.of(2027, 3, 5);

    /** Un dia en medio de la edicion. */
    private static final LocalDate HOY = LocalDate.of(2027, 3, 2);

    private EdicionEvento edicion;
    private TipoRegistro general;

    @BeforeEach
    void armarLaEdicion() throws ReglaNegocioException {
        Organizador duenio = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos", null, "Imm2026!");
        edicion = new EdicionEvento("JIAP 2027", "JIAP", INICIO, FIN,
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", duenio);
        general = edicion.crearTipoRegistro("General", "Entrada general", 500.0, 100);
        // Nace Ingresada: el administrador la acepta desde la Estacion.
        edicion.aceptar();
    }

    private void registrar(String nickname) {
        Asistente asistente = new Asistente(nickname, "Nombre", nickname + "@mail.com",
                "Apellido", LocalDate.of(2000, 1, 1));
        edicion.altaRegistro(asistente, general, LocalDate.of(2026, 10, 28));
    }

    private boolean asistio(String nickname) throws ReglaNegocioException {
        return edicion.obtenerRegistroPara(nickname, nickname).getAsistio();
    }

    // ----- Los casos validos -----

    @Test
    @DisplayName("un registro recien hecho todavia no tiene asistencia")
    void alPrincipioNoAsistio() throws ReglaNegocioException {
        registrar("ana");

        assertFalse(asistio("ana"));
    }

    @Test
    @DisplayName("el asistente registrado registra su asistencia")
    void registraLaAsistencia() throws ReglaNegocioException {
        registrar("ana");

        edicion.registrarAsistencia("ana", HOY);

        assertTrue(asistio("ana"));
    }

    @Test
    @DisplayName("solo cambia el registro de quien la registra")
    void soloCambiaElSuyo() throws ReglaNegocioException {
        registrar("ana");
        registrar("beto");

        edicion.registrarAsistencia("ana", HOY);

        assertTrue(asistio("ana"));
        assertFalse(asistio("beto"));
    }

    @Test
    @DisplayName("el organizador ve en el listado de la edicion quien asistio y quien no")
    void elOrganizadorVeQuienAsistio() throws ReglaNegocioException {
        registrar("ana");
        registrar("beto");
        edicion.registrarAsistencia("ana", HOY);

        long asistieron = edicion.obtenerRegistrosPara("imm").stream()
                .filter(DTRegistro::getAsistio).count();

        assertEquals(1, asistieron);
        assertTrue(edicion.obtenerRegistrosPara("imm").stream()
                .filter(r -> "ana".equals(r.getNickname())).findFirst().orElseThrow().getAsistio());
    }

    @Test
    @DisplayName("la edicion completa que ve su organizador trae la asistencia de cada registro")
    void laEdicionCompletaLaTrae() throws ReglaNegocioException {
        registrar("ana");
        edicion.registrarAsistencia("ana", HOY);

        DTEdicionCompleto completa = edicion.obtenerDTCompletoPara("imm");

        assertEquals(1, completa.getRegistros().size());
        assertTrue(completa.getRegistros().get(0).getAsistio());
    }

    // ----- Las fechas -----

    @Test
    @DisplayName("valor limite: el mismo dia de inicio ya se puede registrar")
    void elDiaDeInicioSi() throws ReglaNegocioException {
        registrar("ana");

        edicion.registrarAsistencia("ana", INICIO);

        assertTrue(asistio("ana"));
    }

    @Test
    @DisplayName("valor limite: el dia anterior al inicio todavia no, y no queda nada marcado")
    void elDiaAnteriorNo() throws ReglaNegocioException {
        registrar("ana");

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> edicion.registrarAsistencia("ana", INICIO.minusDays(1)));

        assertTrue(error.getMessage().contains("todavía no empezó"));
        assertTrue(error.getMessage().contains("01/03/2027"));
        assertFalse(asistio("ana"));
    }

    @ParameterizedTest(name = "{0} dias despues del fin")
    @DisplayName("despues de terminar se puede registrar mientras no se archive")
    @ValueSource(ints = {0, 1, 30, 400})
    void despuesDelFinSi(int dias) throws ReglaNegocioException {
        registrar("ana");

        edicion.registrarAsistencia("ana", FIN.plusDays(dias));

        assertTrue(asistio("ana"));
    }

    // ----- Los rechazos -----

    @Test
    @DisplayName("una edicion que no esta Aceptada no admite registrar asistencia")
    void sinAceptarNo() {
        Organizador duenio = new Organizador("antel", "Antel", "eventos@antel.uy",
                "Organiza eventos", null, "Antel2026!");
        EdicionEvento ingresada = new EdicionEvento("Antel 2027", "ANT", INICIO, FIN,
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", duenio);
        TipoRegistro tipo = ingresada.crearTipoRegistro("General", "Entrada", 100.0, 10);
        ingresada.altaRegistro(new Asistente("ana", "Ana", "ana@mail.com", "Apellido",
                LocalDate.of(2000, 1, 1)), tipo, LocalDate.of(2026, 10, 28));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ingresada.registrarAsistencia("ana", HOY));

        assertTrue(error.getMessage().contains("no está aceptada"));
    }

    @Test
    @DisplayName("la segunda vez es un error y la asistencia sigue registrada")
    void noSePuedeDosVeces() throws ReglaNegocioException {
        registrar("ana");
        edicion.registrarAsistencia("ana", HOY);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> edicion.registrarAsistencia("ana", HOY));

        assertTrue(error.getMessage().contains("Ya registraste"));
        assertTrue(asistio("ana"));
    }

    @ParameterizedTest(name = "\"{0}\" no puede registrar asistencia")
    @DisplayName("quien no esta registrado en la edicion no puede registrarla")
    @ValueSource(strings = {"carlos", "imm", "ANA"})
    void sinRegistroNoPuede(String nickname) {
        registrar("ana");

        assertThrows(ReglaNegocioException.class, () -> edicion.registrarAsistencia(nickname, HOY));
    }

    @ParameterizedTest(name = "nickname \"{0}\"")
    @DisplayName("valor limite: un visitante o un nickname vacio no puede registrarla")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void visitanteNoPuede(String nickname) {
        registrar("ana");

        assertThrows(ReglaNegocioException.class, () -> edicion.registrarAsistencia(nickname, HOY));
    }

    @Test
    @DisplayName("valor limite: los espacios a los costados del nickname no importan")
    void conEspacios() throws ReglaNegocioException {
        registrar("ana");

        edicion.registrarAsistencia("  ana ", HOY);

        assertTrue(asistio("ana"));
    }

    @Test
    @DisplayName("el rechazo de quien no se registro no toca el registro de otro")
    void elRechazoNoTocaALosOtros() throws ReglaNegocioException {
        registrar("ana");

        assertThrows(ReglaNegocioException.class, () -> edicion.registrarAsistencia("carlos", HOY));

        assertFalse(asistio("ana"));
    }
}
