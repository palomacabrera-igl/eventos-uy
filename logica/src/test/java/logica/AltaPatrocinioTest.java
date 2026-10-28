package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de Alta de Patrocinio (CU 15), sin base de datos: lo que se prueba
 * aca son las reglas que viven en las entidades.
 *
 * <pre>
 *  superaElTope(aporte, costoDelTipo, cantidad) -- letra, seccion 4
 *  CV  lo regalado vale menos que el 20% del aporte   -&gt; se acepta
 *  CI  lo regalado vale mas que el 20%                -&gt; se rechaza
 *  Valor limite: vale EXACTAMENTE el 20%              -&gt; se acepta
 *  Valor limite: cero registros gratuitos             -&gt; se acepta siempre
 *  Valor limite: un tipo de registro gratis           -&gt; se acepta siempre
 *
 *  tienePatrocinioDe(institucion)
 *  CI  la institucion ya patrocina esta edicion       -&gt; se rechaza
 *  CV  otra institucion                               -&gt; se acepta
 *
 *  buscarPatrocinio(codigo) -- el codigo dentro de la edicion
 *  CV  el codigo que existe                           -&gt; lo encuentra
 *  CI  uno que no existe                              -&gt; null
 *
 *  loOrganiza(nickname)
 *  CV  el organizador de la edicion, con o sin espacios
 *  CI  otro organizador, o nadie (null)
 * </pre>
 */
@DisplayName("Alta de Patrocinio (CU 15)")
class AltaPatrocinioTest {

    private Organizador duenio;
    private EdicionEvento edicion;
    private TipoRegistro general;
    private Institucion utec;

    @BeforeEach
    void armarLaEdicion() {
        duenio = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos", null, "Imm2026!");

        edicion = new EdicionEvento("JIAP 2027", "JIAP",
                LocalDate.of(2027, 10, 1), LocalDate.of(2027, 10, 5),
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", duenio);

        general = edicion.crearTipoRegistro("General", "Entrada general", 500.0, 100);
        utec = new Institucion("UTEC", "Universidad Tecnologica", "https://utec.edu.uy");
    }

    private Patrocinio patrocinio(String codigo, Institucion institucion) {
        return new Patrocinio(LocalDate.of(2026, 10, 27), 100000.0, 10, codigo,
                NivelPatrocinio.ORO, institucion, general);
    }

    // ----- El tope del 20 % -----

    @Test
    @DisplayName("lo regalado por debajo del 20 % del aporte se acepta")
    void porDebajoDelTope() {
        // 10 registros de $500 son $5.000; el 20 % de $100.000 son $20.000.
        assertFalse(Patrocinio.superaElTope(100000.0, 500.0, 10));
    }

    @Test
    @DisplayName("lo regalado por encima del 20 % se rechaza")
    void porEncimaDelTope() {
        // 50 registros de $500 son $25.000, y el tope son $20.000.
        assertTrue(Patrocinio.superaElTope(100000.0, 500.0, 50));
    }

    @Test
    @DisplayName("valor limite: exactamente el 20 % se acepta, no se pasa")
    void justoEnElTope() {
        // 40 registros de $500 son $20.000, que es justo el 20 % de $100.000.
        assertEquals(20000.0, Patrocinio.topeDeGratis(100000.0));
        assertEquals(20000.0, Patrocinio.valorDeGratis(500.0, 40));
        assertFalse(Patrocinio.superaElTope(100000.0, 500.0, 40));

        // Un solo registro mas ya se pasa.
        assertTrue(Patrocinio.superaElTope(100000.0, 500.0, 41));
    }

    @Test
    @DisplayName("valor limite: sin registros gratuitos nunca se pasa del tope")
    void sinRegistrosGratis() {
        assertFalse(Patrocinio.superaElTope(1000.0, 500.0, 0));
    }

    @Test
    @DisplayName("valor limite: si el tipo de registro no cuesta nada, tampoco")
    void tipoDeRegistroGratuito() {
        assertFalse(Patrocinio.superaElTope(1000.0, 0.0, 999));
    }

    // ----- Una institucion, un patrocinio por edicion -----

    @Test
    @DisplayName("la institucion que ya patrocina la edicion no puede patrocinarla de nuevo")
    void institucionRepetida() {
        assertFalse(edicion.tienePatrocinioDe("UTEC"));

        edicion.agregarPatrocinio(patrocinio("UTEC-2027", utec));

        assertTrue(edicion.tienePatrocinioDe("UTEC"));
        assertFalse(edicion.tienePatrocinioDe("ANCAP"));
    }

    // ----- El codigo -----

    @Test
    @DisplayName("el codigo se encuentra dentro de la edicion que lo tiene")
    void encuentraElCodigo() {
        edicion.agregarPatrocinio(patrocinio("UTEC-2027", utec));

        assertEquals("UTEC-2027", edicion.buscarPatrocinio("UTEC-2027").getCodigo());
        assertEquals(null, edicion.buscarPatrocinio("NO-EXISTE"));
    }

    // ----- De quien es la edicion -----

    @Test
    @DisplayName("solo el organizador de la edicion la organiza, con o sin espacios")
    void reconoceAlOrganizador() {
        assertTrue(edicion.loOrganiza("imm"));
        assertTrue(edicion.loOrganiza("  imm  "));
    }

    @Test
    @DisplayName("otro organizador, otra capitalizacion o nadie, no")
    void noReconoceAOtros() {
        assertFalse(edicion.loOrganiza("antel"));
        assertFalse(edicion.loOrganiza("IMM"));
        assertFalse(edicion.loOrganiza(null));
    }

    // ----- El patrocinio que queda armado -----

    @Test
    @DisplayName("el patrocinio guarda su nivel, su aporte y su tipo de registro")
    void losDatosDelPatrocinio() {
        Patrocinio p = patrocinio("UTEC-2027", utec);
        edicion.agregarPatrocinio(p);

        assertEquals(NivelPatrocinio.ORO, p.getNivelPatro());
        assertEquals(100000.0, p.getMonto());
        assertEquals(10, p.getCantRegistrosGratis());
        assertEquals("UTEC", p.getInstitucion().getNombre());
        assertEquals("General", p.getTipoRegistro().getNombre());
    }
}
