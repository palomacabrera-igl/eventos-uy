package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Pruebas de caja negra de Consulta de Patrocinio (CU 16) sobre las
 * entidades, sin base de datos. La regla central es de la letra: el codigo
 * de patrocinio se le muestra solo al organizador de la edicion.
 *
 * <pre>
 *  obtenerPatrociniosPara(solicitante) / obtenerPatrocinioPara(institucion, solicitante)
 *  CV  el organizador de la edicion            -&gt; con codigo
 *  CI  otro organizador, un asistente          -&gt; sin codigo
 *  Valor limite: visitante (null), vacio       -&gt; sin codigo
 *
 *  obtenerPatrocinioPara(institucion, ...)
 *  CV  una institucion que patrocina la edicion
 *  CI  una institucion que no la patrocina     -&gt; ReglaNegocioException
 *  Valor limite: edicion sin patrocinios       -&gt; conjunto vacio
 * </pre>
 */
@DisplayName("Consulta de Patrocinio (CU 16)")
class ConsultaPatrocinioTest {

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

    private void patrocinar(String institucion, String codigo) {
        edicion.agregarPatrocinio(new Patrocinio(LocalDate.of(2026, 10, 27), 100000.0, 10,
                codigo, NivelPatrocinio.ORO,
                new Institucion(institucion, "Descripcion", "https://ejemplo.uy"), general));
    }

    // ----- El codigo, segun quien pregunta -----

    @Test
    @DisplayName("el organizador de la edicion ve el codigo")
    void elOrganizadorVeElCodigo() throws ReglaNegocioException {
        patrocinar("UTEC", "UTEC-JIAP-27");

        DTPatrocinio dt = edicion.obtenerPatrocinioPara("UTEC", "imm");

        assertEquals("UTEC-JIAP-27", dt.getCodigoPatrocinio());
    }

    @ParameterizedTest(name = "\"{0}\" no ve el codigo")
    @DisplayName("cualquier otro no ve el codigo, pero si el resto de los datos")
    @ValueSource(strings = {"antel", "sofia", "IMM", "otro"})
    void otroNoVeElCodigo(String solicitante) throws ReglaNegocioException {
        patrocinar("UTEC", "UTEC-JIAP-27");

        DTPatrocinio dt = edicion.obtenerPatrocinioPara("UTEC", solicitante);

        assertNull(dt.getCodigoPatrocinio());
        assertEquals("UTEC", dt.getInstitucion());
        assertEquals("ORO", dt.getNivel());
        assertEquals(100000.0, dt.getMonto());
        assertEquals("General", dt.getTipoRegistro());
        assertEquals(10, dt.getCantRegistrosGratis());
    }

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("valor limite: un visitante (sin sesion) no ve el codigo")
    @NullAndEmptySource
    void visitanteNoVeElCodigo(String solicitante) throws ReglaNegocioException {
        patrocinar("UTEC", "UTEC-JIAP-27");

        assertNull(edicion.obtenerPatrocinioPara("UTEC", solicitante).getCodigoPatrocinio());
    }

    @Test
    @DisplayName("el listado aplica la misma regla a todos los patrocinios")
    void elListadoAplicaLaRegla() {
        patrocinar("UTEC", "UTEC-JIAP-27");
        patrocinar("ANCAP", "ANCAP-JIAP-27");

        Set<DTPatrocinio> paraElOrganizador = edicion.obtenerPatrociniosPara("imm");
        Set<DTPatrocinio> paraUnVisitante = edicion.obtenerPatrociniosPara(null);

        assertEquals(2, paraElOrganizador.size());
        assertTrue(paraElOrganizador.stream().allMatch(p -> p.getCodigoPatrocinio() != null));
        assertEquals(2, paraUnVisitante.size());
        assertTrue(paraUnVisitante.stream().allMatch(p -> p.getCodigoPatrocinio() == null));
    }

    // ----- Que patrocinio -----

    @Test
    @DisplayName("una institucion que no patrocina la edicion da error")
    void institucionQueNoPatrocina() {
        patrocinar("UTEC", "UTEC-JIAP-27");

        assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerPatrocinioPara("ANCAP", "imm"));
    }

    @Test
    @DisplayName("valor limite: una edicion sin patrocinios devuelve el conjunto vacio")
    void sinPatrocinios() {
        assertTrue(edicion.obtenerPatrociniosPara("imm").isEmpty());
    }
}
