package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import persistencia.Persistencia;

/**
 * Registro a Edicion (CU 12): el caso valido y los cinco rechazos que la
 * letra enumera uno por uno en la seccion 7.5.
 *
 * <p>La letra, seccion 4: «Si el asistente pertenece a una institucion y
 * utiliza el codigo de patrocinio de esa institucion para la edicion, el
 * costo es cero. El codigo tiene que corresponder a esa edicion, al tipo de
 * registro elegido y a la institucion del asistente, y todavia tiene que
 * disponer de usos. [...] Siguen vigentes el cupo del tipo de registro y la
 * prohibicion de un segundo registro del mismo asistente a la misma edicion».
 *
 * <pre>
 *  Escenario que arma cada prueba, y que borra al terminar:
 *
 *      Institucion UTEC  -- patrocina --&gt;  edicion "Test IT 2027"
 *      codigo TEST-IT-COD, 1 registro gratuito del tipo "General"
 *
 *      Tipo "General"     $1.500,  cupo 2
 *      Tipo "Estudiante"  $800,    cupo 10
 *
 *      test-it-utec    asistente de UTEC
 *      test-it-ancap   asistente de ANCAP
 *      test-it-sin     asistente sin institucion
 *
 *  Casos de prueba
 *  -------------------------------------------------------------------
 *  CV  registro general, sin codigo        -&gt; cuesta lo que el tipo
 *  CV  codigo valido                       -&gt; cuesta CERO
 *
 *  CI  1. codigo de otra institucion       -&gt; el asistente no es de UTEC
 *  CI  2. codigo de otro tipo de registro  -&gt; el codigo da "General"
 *  CI  3. codigo sin usos                  -&gt; el patrocinio dio 1 y ya se uso
 *  CI  4. cupo agotado                     -&gt; el tipo tiene 2 lugares
 *  CI  5. asistente ya registrado          -&gt; uno por edicion
 *
 *  CI  6. asistente sin institucion con codigo  (letra, CU 12: «un
 *         asistente sin institucion solo puede registrarse de forma
 *         general»)
 *  CI  codigo que no existe en esa edicion
 * </pre>
 */
@DisplayName("Registro a Edicion: el caso valido y los cinco rechazos (CU 12)")
class RegistroAEdicionIntegracionTest extends PruebaConBase {

    private static final String EDICION = "Test IT 2027";
    private static final String CODIGO = "TEST-IT-COD";
    private static final String GENERAL = "General";
    private static final String ESTUDIANTE = "Estudiante";

    private static final String DE_UTEC = "test-it-utec";
    private static final String DE_ANCAP = "test-it-ancap";
    private static final String SIN_INSTITUCION = "test-it-sin";
    private static final String OTRO_DE_UTEC = "test-it-utec2";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    // ------------------------------------------------- el escenario

    @BeforeEach
    void armarElEscenario() throws ReglaNegocioException {
        sinRastros();
        Persistencia.enTransaccion(em -> {
            Institucion utec = em.createQuery(
                    "SELECT i FROM Institucion i WHERE i.nombre = 'UTEC'", Institucion.class)
                    .getSingleResult();
            Institucion ancap = em.createQuery(
                    "SELECT i FROM Institucion i WHERE i.nombre = 'ANCAP'", Institucion.class)
                    .getSingleResult();
            Categoria categoria = em.createQuery(
                    "SELECT c FROM Categoria c", Categoria.class)
                    .getResultStream().findFirst().orElseThrow();

            Organizador organizador = new Organizador("test-it-orgreg", "Org de prueba",
                    "test-it-orgreg@mail.com", "Organiza la edicion de prueba", null,
                    "Clave1!");

            Evento evento = new Evento("Test IT Evento", "Evento de prueba",
                    LocalDate.now(), "TIE", List.of(categoria));

            EdicionEvento edicion = new EdicionEvento(EDICION, "TIE27",
                    LocalDate.of(2027, 6, 1), LocalDate.of(2027, 6, 3),
                    LocalDate.now(), "Montevideo", "Uruguay", organizador);
            evento.agregarEdicion(edicion);
            organizador.agregarEdicion(edicion);

            // Cupo 2 en "General": alcanza para llenarlo en una prueba.
            TipoRegistro general = edicion.crearTipoRegistro(
                    GENERAL, "Entrada general", 1500.0, 2);
            edicion.crearTipoRegistro(ESTUDIANTE, "Tarifa estudiantil", 800.0, 10);

            EdicionEvento ingresada = new EdicionEvento("Test IT Ingresada 2027", "TII27",
                    LocalDate.of(2027, 7, 1), LocalDate.of(2027, 7, 3),
                    LocalDate.now(), "Montevideo", "Uruguay", organizador);
            evento.agregarEdicion(ingresada);
            organizador.agregarEdicion(ingresada);
            ingresada.crearTipoRegistro(GENERAL, "Entrada general", 1500.0, 2);

            // Un solo registro gratuito: alcanza para agotarlo en una prueba.
            edicion.agregarPatrocinio(new Patrocinio(LocalDate.now(), 100000.0, 1,
                    CODIGO, NivelPatrocinio.ORO, utec, general));

            em.persist(organizador);
            em.persist(evento);
            em.persist(asistente(DE_UTEC, "Uno", utec));
            em.persist(asistente(OTRO_DE_UTEC, "Dos", utec));
            em.persist(asistente(DE_ANCAP, "Tres", ancap));
            em.persist(asistente(SIN_INSTITUCION, "Cuatro", null));
        });
        Persistencia.refrescar();
        sistema.aceptarEdicion(EDICION);
    }

    private static Asistente asistente(String nickname, String nombre, Institucion institucion) {
        Asistente a = new Asistente(nickname, nombre, nickname + "@mail.com",
                "Prueba", LocalDate.of(2000, 1, 1), "Clave1!");
        a.setInstitucion(institucion);
        return a;
    }

    @AfterEach
    void sinRastros() {
        // El orden importa: primero lo que apunta, despues lo apuntado.
        borrar("registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "' OR nombre = 'Test IT Ingresada 2027')");
        borrar("patrocinio", "codigo = '" + CODIGO + "'");
        borrar("tipo_registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "' OR nombre = 'Test IT Ingresada 2027')");
        borrar("edicion_evento", "nombre IN ('" + EDICION + "', 'Test IT Ingresada 2027')");
        // La tabla de union con las categorias apunta al evento: va antes.
        borrar("evento_categoria",
                "evento_id IN (SELECT id FROM evento WHERE nombre = 'Test IT Evento')");
        borrar("evento", "nombre = 'Test IT Evento'");
        borrar("usuario", "nickname LIKE 'test-it-%'");
    }

    private double costoDe(String nickname) throws ReglaNegocioException {
        return sistema.listarRegistrosDeAsistente(nickname, nickname).stream()
                .filter(r -> EDICION.equals(r.getNombreEdicion()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no quedo registrado"))
                .getCosto();
    }

    // ================================================== casos validos

    @Test
    @DisplayName("CV · registro general, sin codigo: cuesta lo que vale el tipo")
    void registroGeneral() throws ReglaNegocioException {
        // Datos de prueba: un asistente cualquiera y el tipo General, de $1.500
        // Ejecucion
        sistema.altaRegistro(DE_ANCAP, EDICION, GENERAL);

        // Resultado esperado
        assertEquals(1500.0, costoDe(DE_ANCAP));
    }

    @Test
    @DisplayName("CV · un asistente sin institucion se registra de forma general")
    void sinInstitucionSeRegistraGeneral() throws ReglaNegocioException {
        sistema.altaRegistro(SIN_INSTITUCION, EDICION, ESTUDIANTE);

        assertEquals(800.0, costoDe(SIN_INSTITUCION));
    }

    @Test
    @DisplayName("CV · codigo valido: el registro cuesta CERO")
    void codigoValido() throws ReglaNegocioException {
        // Datos de prueba: test-it-utec es de UTEC, y el codigo es de UTEC
        // para el tipo General de esta edicion, con 1 uso disponible.

        // Ejecucion
        sistema.altaRegistro(DE_UTEC, EDICION, GENERAL, CODIGO);

        // Resultado esperado: la letra dice «el costo es cero»
        assertEquals(0.0, costoDe(DE_UTEC));
    }

    // =============================================== los cinco rechazos

    @Test
    @DisplayName("CI 1 · codigo de otra institucion: el asistente no pertenece a UTEC")
    void codigoDeOtraInstitucion() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(DE_ANCAP, EDICION, GENERAL, CODIGO));

        assertTrue(error.getMessage().contains("UTEC"),
                "el mensaje tiene que decir de que institucion es el codigo");
    }

    @Test
    @DisplayName("CI 2 · codigo de otro tipo de registro: da General, no Estudiante")
    void codigoDeOtroTipoDeRegistro() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(DE_UTEC, EDICION, ESTUDIANTE, CODIGO));

        assertTrue(error.getMessage().contains(GENERAL));
    }

    @Test
    @DisplayName("CI 3 · codigo sin usos: el patrocinio dio uno solo y ya se gasto")
    void codigoSinUsos() throws ReglaNegocioException {
        // Datos de prueba: el patrocinio da 1 registro gratuito.
        sistema.altaRegistro(DE_UTEC, EDICION, GENERAL, CODIGO);
        assertEquals(0.0, costoDe(DE_UTEC));

        // Ejecucion: otro asistente de UTEC quiere usar el mismo codigo
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(OTRO_DE_UTEC, EDICION, GENERAL, CODIGO));

        assertTrue(error.getMessage().contains("gratuitos"));
    }

    @Test
    @DisplayName("CI 4 · cupo agotado: el tipo tiene dos lugares y ya se ocuparon")
    void cupoAgotado() throws ReglaNegocioException {
        // Datos de prueba: "General" tiene cupo 2
        sistema.altaRegistro(DE_UTEC, EDICION, GENERAL, CODIGO);
        sistema.altaRegistro(DE_ANCAP, EDICION, GENERAL);

        // Ejecucion: un tercero, aunque venga dispuesto a pagar
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(SIN_INSTITUCION, EDICION, GENERAL));

        assertTrue(error.getMessage().contains("cupo"));
    }

    @Test
    @DisplayName("CI 5 · asistente ya registrado: uno por edicion")
    void asistenteYaRegistrado() throws ReglaNegocioException {
        sistema.altaRegistro(DE_ANCAP, EDICION, GENERAL);

        // Ni siquiera eligiendo otro tipo de registro
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(DE_ANCAP, EDICION, ESTUDIANTE));

        assertTrue(error.getMessage().contains("ya esta registrado"));
    }

    // =========================================== otros casos invalidos

    @Test
    @DisplayName("CI 6 · un asistente sin institucion no puede usar ningun codigo")
    void sinInstitucionNoPuedeUsarCodigo() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(SIN_INSTITUCION, EDICION, GENERAL, CODIGO));
    }

    @Test
    @DisplayName("CI · un codigo que no existe en esa edicion")
    void codigoInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(DE_UTEC, EDICION, GENERAL, "NO-EXISTE-9999"));
    }

    @Test
    @DisplayName("CI · una edicion o un tipo de registro que no existen")
    void edicionOTipoInexistentes() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(DE_UTEC, "No Existe 2099", GENERAL));

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(DE_UTEC, EDICION, "No Existe"));
    }

    @Test
    @DisplayName("no se permite registrarse a una edicion que aun no fue aceptada")
    void edicionNoAceptada() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> sistema.altaRegistro(DE_UTEC, "Test IT Ingresada 2027", GENERAL));

        assertTrue(error.getMessage().contains("ACEPTADA"));
    }

    // ------------------------- las reglas tal como se ven en la edicion

    @Test
    @DisplayName("el cupo y el registro duplicado se ven desde la edicion")
    void loQueSabeLaEdicion() throws ReglaNegocioException {
        assertFalse(sistema.listarRegistrosDeAsistente(DE_ANCAP, DE_ANCAP).stream()
                .anyMatch(r -> EDICION.equals(r.getNombreEdicion())));

        sistema.altaRegistro(DE_ANCAP, EDICION, GENERAL);

        assertTrue(sistema.listarRegistrosDeAsistente(DE_ANCAP, DE_ANCAP).stream()
                .anyMatch(r -> EDICION.equals(r.getNombreEdicion())));
    }
}
