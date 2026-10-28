package logica;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Alta y Consulta de Edicion (CU 8 y 9) contra la base, ejercitando Sistema.
 *
 * <pre>
 *  altaEdicion(solicitante, evento, datos, imagen)
 *  CV  un organizador con datos validos -&gt; nace Ingresada, es suya, fecha de hoy
 *  CI  un asistente o un visitante      -&gt; ReglaNegocioException
 *  CI  un nombre de edicion que existe  -&gt; ReglaNegocioException
 *  CI  un evento que no existe          -&gt; ReglaNegocioException
 *  CI  fin anterior al inicio           -&gt; ReglaNegocioException
 *
 *  consultarEdicion(nombre, solicitante)
 *  CV  una Aceptada, cualquiera         -&gt; sin registros ni codigos si no es su organizador
 *  CV  una Aceptada, su organizador     -&gt; con registros y codigos
 *  CV  una Ingresada, su organizador    -&gt; la ve
 *  CI  una Ingresada, otro o visitante  -&gt; ReglaNegocioException
 *  CI  una que no existe                -&gt; ReglaNegocioException
 * </pre>
 *
 * Las ediciones que se crean llevan el prefijo "IT CU8" y se borran al terminar.
 */
@DisplayName("Alta y Consulta de Edicion contra la base (CU 8 y 9)")
class EdicionIntegracionTest extends PruebaConBase {

    private static final String NUEVA = "IT CU8 Edicion";
    private static final LocalDate INICIO = LocalDate.of(2027, 9, 1);

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @AfterEach
    void sinRastros() {
        borrar("edicion_evento", "nombre LIKE 'IT CU8%'");
    }

    private static DTEdicionEvento datos(String nombre, LocalDate inicio, LocalDate fin) {
        return new DTEdicionEvento(nombre, "ITC8", "Montevideo", "Uruguay",
                DTFecha.desde(inicio), DTFecha.desde(fin), null);
    }

    // ----- altaEdicion -----

    @Test
    @DisplayName("un organizador la crea: nace Ingresada, es suya y tiene la fecha de hoy")
    void organizadorLaCrea() throws ReglaNegocioException {
        sistema.altaEdicion("imm", "JIAP", datos(NUEVA, INICIO, INICIO.plusDays(1)), null);

        DTEdicionCompleto dt = sistema.consultarEdicion(NUEVA, "imm");
        assertEquals(EstadoEdicion.INGRESADA, dt.getEstado());
        assertEquals("imm", dt.getOrganizador().getNickname());
        assertEquals(LocalDate.now(), dt.getFechaAlta());
        assertEquals("JIAP", dt.getNombreEvento());
        assertFalse(sistema.listarEdicionesAceptadas("JIAP").stream()
                .anyMatch(ed -> ed.getNombre().equals(NUEVA)));
    }

    @Test
    @DisplayName("con imagen: queda guardada y se puede pedir")
    void conImagen() throws ReglaNegocioException {
        byte[] bytes = {9, 8, 7};

        sistema.altaEdicion("imm", "JIAP", datos(NUEVA, INICIO, INICIO),
                new DTImagen(bytes, "image/webp"));

        assertTrue(sistema.consultarEdicion(NUEVA, "imm").getTieneImagen());
        assertArrayEquals(bytes, sistema.obtenerImagenEdicion(NUEVA).getContenido());
    }

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("un asistente o un visitante no puede crear ediciones")
    @NullSource
    @ValueSource(strings = {"vale23"})
    void soloOrganizadores(String solicitante) {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaEdicion(solicitante,
                "JIAP", datos(NUEVA, INICIO, INICIO), null));
    }

    @Test
    @DisplayName("un nombre de edicion que ya existe se rechaza")
    void nombreRepetido() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaEdicion("imm",
                "JIAP", datos("JIAP 2026", INICIO, INICIO), null));
    }

    @Test
    @DisplayName("un evento que no existe se rechaza")
    void eventoInexistente() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaEdicion("imm",
                "No existe", datos(NUEVA, INICIO, INICIO), null));
    }

    @Test
    @DisplayName("valor limite: fin un dia antes del inicio se rechaza")
    void finAntesDelInicio() {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaEdicion("imm",
                "JIAP", datos(NUEVA, INICIO, INICIO.minusDays(1)), null));
    }

    // ----- consultarEdicion -----

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("una Aceptada la ve cualquiera, sin registros ni codigos")
    @NullSource
    @ValueSource(strings = {"antel", "vale23"})
    void aceptadaParaCualquiera(String solicitante) throws ReglaNegocioException {
        DTEdicionCompleto dt = sistema.consultarEdicion("JIAP 2026", solicitante);

        assertTrue(dt.getRegistros().isEmpty());
        for (DTPatrocinio p : dt.getPatrocinios()) {
            assertNull(p.getCodigoPatrocinio());
        }
    }

    @Test
    @DisplayName("su organizador ve los registros y los codigos de patrocinio")
    void aceptadaParaSuOrganizador() throws ReglaNegocioException {
        DTEdicionCompleto dt = sistema.consultarEdicion("JIAP 2026", "imm");

        assertFalse(dt.getRegistros().isEmpty());
        assertFalse(dt.getPatrocinios().isEmpty());
        for (DTPatrocinio p : dt.getPatrocinios()) {
            assertNotNull(p.getCodigoPatrocinio());
        }
    }

    @Test
    @DisplayName("una Ingresada la ve su organizador")
    void ingresadaParaSuOrganizador() throws ReglaNegocioException {
        assertEquals(EstadoEdicion.INGRESADA,
                sistema.consultarEdicion("JIAP 2027", "imm").getEstado());
    }

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("una Ingresada no la ve otro organizador, un asistente ni un visitante")
    @NullSource
    @ValueSource(strings = {"antel", "vale23"})
    void ingresadaParaOtro(String solicitante) {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarEdicion("JIAP 2027", solicitante));
    }

    @Test
    @DisplayName("una edicion que no existe se rechaza")
    void edicionInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.consultarEdicion("No existe", "imm"));
    }
}
