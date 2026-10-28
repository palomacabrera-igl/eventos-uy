package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de caja negra del Alta de Patrocinio (CU 15): se construyen solo a
 * partir de la especificacion de la letra, con clases de equivalencia y
 * valores limite. No necesitan base de datos.
 *
 * <p>La letra, seccion 4: «el tope del 20 % del aporte para el costo de los
 * registros gratuitos del patrocinio».
 *
 * <pre>
 *  superaElTope(aporte, costoDelTipo, cantidad)
 *
 *  Con un aporte de $100.000 y un tipo de $500, el tope son $20.000,
 *  o sea 40 registros gratuitos.
 *
 *  Clase de equivalencia          Rango (cantidad)  Valores limite
 *  -----------------------------  ----------------  ------------------
 *  CV  no se pasa del tope        [0, 40]           0, 40
 *  CI  se pasa del tope           [41, maxint]      41, Integer.MAX_VALUE
 *
 *  Casos aparte, del comportamiento y no del rango:
 *  CV  un tipo de registro que no cuesta nada  -> nunca se pasa
 *  CV  cero registros gratuitos                -> nunca se pasa
 *
 *  tienePatrocinioDe(institucion)
 *  CV  una institucion que todavia no patrocina la edicion
 *  CI  una que ya la patrocina
 *
 *  loOrganiza(nickname)
 *  CV  el nickname del organizador, con y sin espacios
 *  CI  otro nickname, otra capitalizacion, o nadie (null)
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
                LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 5),
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", duenio);

        general = edicion.crearTipoRegistro("General", "Entrada general", 500.0, 100);
        utec = new Institucion("UTEC", "Universidad Tecnologica", "https://utec.edu.uy");
    }

    private Patrocinio patrocinio(String codigo, Institucion institucion) {
        return new Patrocinio(LocalDate.of(2026, 10, 27), 100000.0, 10, codigo,
                NivelPatrocinio.ORO, institucion, general);
    }

    // ------------------------------------------------ el tope del 20 %

    @ParameterizedTest(name = "{0} registros de $500 sobre un aporte de $100.000 -> se acepta")
    @DisplayName("dentro del tope: los extremos de la clase valida")
    @ValueSource(ints = {0, 1, 39, 40})
    void dentroDelTope(int cantidad) {
        assertFalse(Patrocinio.superaElTope(100000.0, 500.0, cantidad));
    }

    @ParameterizedTest(name = "{0} registros de $500 sobre un aporte de $100.000 -> se rechaza")
    @DisplayName("fuera del tope: los extremos de la clase invalida")
    @ValueSource(ints = {41, 100, Integer.MAX_VALUE})
    void fueraDelTope(int cantidad) {
        assertTrue(Patrocinio.superaElTope(100000.0, 500.0, cantidad));
    }

    @Test
    @DisplayName("valor limite: justo el 20 % se acepta, y un registro mas ya no")
    void elLimiteExacto() {
        // Datos de prueba: 40 registros de $500 son $20.000, que es
        // exactamente el 20 % de $100.000.
        double aporte = 100000.0;
        double costo = 500.0;

        // Ejecucion y resultado esperado
        assertEquals(20000.0, Patrocinio.topeDeGratis(aporte));
        assertEquals(20000.0, Patrocinio.valorDeGratis(costo, 40));

        // La letra dice "supera", asi que justo en el limite se acepta.
        assertFalse(Patrocinio.superaElTope(aporte, costo, 40));
        assertTrue(Patrocinio.superaElTope(aporte, costo, 41));
    }

    @ParameterizedTest(name = "aporte ${0}, tipo de ${1}, {2} gratuitos -> {3}")
    @DisplayName("el tope se calcula sobre el aporte de cada patrocinio")
    @CsvSource({
            // aporte,  costo,  cantidad,  se pasa?
            "  1000.0,  500.0,         0,  false",   // CV: nada gratis, nunca se pasa
            "  1000.0,    0.0,       999,  false",   // CV: tipo sin costo, nunca se pasa
            "  1000.0,  500.0,         1,  true",    // CI: $500 sobre un tope de $200
            " 10000.0,  500.0,         4,  false",   // CV: $2.000 = el 20 % justo
            " 10000.0,  500.0,         5,  true"     // CI: $2.500 pasa el tope
    })
    void elTopeSegunElAporte(double aporte, double costo, int cantidad, boolean sePasa) {
        assertEquals(sePasa, Patrocinio.superaElTope(aporte, costo, cantidad));
    }

    // -------------------------- una institucion, un patrocinio por edicion

    @Test
    @DisplayName("la institucion que ya patrocina la edicion no puede patrocinarla de nuevo")
    void institucionRepetida() {
        // Datos de prueba: una edicion sin patrocinios
        assertFalse(edicion.tienePatrocinioDe("UTEC"));

        // Ejecucion
        edicion.agregarPatrocinio(patrocinio("UTEC-2027", utec));

        // Resultado esperado
        assertTrue(edicion.tienePatrocinioDe("UTEC"));
        assertFalse(edicion.tienePatrocinioDe("ANCAP"));
    }

    @Test
    @DisplayName("el codigo se encuentra dentro de la edicion que lo tiene")
    void buscarPorCodigo() {
        edicion.agregarPatrocinio(patrocinio("UTEC-2027", utec));

        assertEquals("UTEC-2027", edicion.buscarPatrocinio("UTEC-2027").getCodigo());
        assertNull(edicion.buscarPatrocinio("NO-EXISTE"));
    }

    // ----------------------------------------- de quien es la edicion

    @ParameterizedTest(name = "\"{0}\" organiza la edicion")
    @DisplayName("el organizador de la edicion, con o sin espacios de mas")
    @ValueSource(strings = {"imm", "  imm  ", "imm   "})
    void reconoceAlOrganizador(String solicitante) {
        assertTrue(edicion.loOrganiza(solicitante));
    }

    @ParameterizedTest(name = "\"{0}\" NO organiza la edicion")
    @DisplayName("otro organizador o la misma palabra escrita distinto, no")
    @ValueSource(strings = {"antel", "IMM", "imm2", "im", ""})
    void noReconoceAOtros(String solicitante) {
        assertFalse(edicion.loOrganiza(solicitante));
    }

    @Test
    @DisplayName("valor limite: sin sesion (null) nadie organiza nada")
    void sinSesion() {
        assertFalse(edicion.loOrganiza(null));
    }

    // ------------------------------------- el patrocinio que queda armado

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
