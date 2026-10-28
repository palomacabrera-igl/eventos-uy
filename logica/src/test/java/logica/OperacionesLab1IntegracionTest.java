package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Las operaciones del Laboratorio 1, que la letra pide mantener (seccion 7.5:
 * «Hay que mantener las pruebas del Laboratorio 1 y agregar las de los
 * cambios de este laboratorio»).
 *
 * <p>Estas operaciones trabajan <b>con estado</b>: el alta de usuario son tres
 * llamadas encadenadas, y el alta de edicion o de tipo de registro necesitan
 * que antes se haya seleccionado un evento o una edicion. Por eso las pruebas
 * se recorren <b>en orden</b>, como la demo del curso: cada paso deja al
 * controlador en la situacion que el siguiente necesita.
 *
 * <pre>
 *  Recorrido
 *  ---------------------------------------------------------------
 *  Paso 1  Alta de Usuario en tres llamadas (asistente y organizador)
 *  Paso 2  Alta de Institucion
 *  Paso 3  Alta de Evento, con sus categorias
 *  Paso 4  Alta de Edicion sobre ese evento
 *  Paso 5  Alta de Tipo de Registro sobre esa edicion
 *  Paso 6  Alta de Patrocinio, la version con estado
 *  Paso 7  Las consultas sobre todo lo anterior
 *  Paso 8  Modificar Datos de Usuario, la version con estado
 *
 *  Rechazos, cada uno con una sola clase invalida
 *  ---------------------------------------------------------------
 *  CI  nickname repetido en el alta de usuario
 *  CI  correo repetido
 *  CI  institucion repetida
 *  CI  tipo de registro repetido en la misma edicion
 *  CI  usar una operacion con estado sin haber seleccionado antes
 *  CI  seleccionar algo que no existe
 * </pre>
 */
@DisplayName("Operaciones del Laboratorio 1")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OperacionesLab1IntegracionTest extends PruebaConBase {

    private static final String ASISTENTE = "test-l1-asis";
    private static final String ORGANIZADOR = "test-l1-org";
    private static final String INSTITUCION = "Test L1 Institucion";
    private static final String EVENTO = "Test L1 Evento";
    private static final String EDICION = "Test L1 Edicion 2027";
    private static final String TIPO = "Test L1 General";
    private static final String CODIGO = "TEST-L1-COD";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    @AfterEach
    void sinRastros() {
        borrar("registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "')");
        borrar("patrocinio", "codigo = '" + CODIGO + "'");
        borrar("tipo_registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "')");
        borrar("edicion_evento", "nombre = '" + EDICION + "'");
        borrar("evento_categoria",
                "evento_id IN (SELECT id FROM evento WHERE nombre = '" + EVENTO + "')");
        borrar("evento", "nombre = '" + EVENTO + "'");
        borrar("usuario", "nickname LIKE 'test-l1-%'");
        borrar("institucion", "nombre = '" + INSTITUCION + "'");
    }

    /** Deja creado el escenario que varios pasos necesitan. */
    private void crearEscenarioCompleto() throws ReglaNegocioException {
        altaAsistente();
        altaOrganizador();
        sistema.altaInstitucion(INSTITUCION, "Institucion de prueba", "https://test.uy");
        altaEvento();
        altaEdicion();
        sistema.seleccionarEdicionEvento(EDICION);
        sistema.ingresarDatosTipoRegistro(TIPO, "Entrada de prueba", 1000.0, 10);
    }

    private void altaAsistente() throws ReglaNegocioException {
        sistema.ingresarDatosUsuario(
                new DTUsuario(ASISTENTE, "Asistente L1", ASISTENTE + "@mail.com"),
                TipoUsuario.ASISTENTE);
        sistema.ingresarDatosAsistente("Prueba", new DTFecha(1, 1, 2000));
    }

    private void altaOrganizador() throws ReglaNegocioException {
        sistema.ingresarDatosUsuario(
                new DTUsuario(ORGANIZADOR, "Organizador L1", ORGANIZADOR + "@mail.com"),
                TipoUsuario.ORGANIZADOR);
        sistema.ingresarDatosOrganizador("Organiza la prueba", "https://test-org.uy");
    }

    private void altaEvento() throws ReglaNegocioException {
        String categoria = sistema.listarCategorias().iterator().next().getNombre();
        sistema.ingresarDatosEvento(EVENTO, "Evento de prueba del Lab 1",
                LocalDate.now(), "TL1", List.of(categoria));
    }

    private void altaEdicion() throws ReglaNegocioException {
        sistema.seleccionarEvento(EVENTO);
        sistema.seleccionarOrganizador(ORGANIZADOR);
        sistema.ingresarDatosEdicion(new DTEdicionEvento(EDICION, "TL127",
                "Montevideo", "Uruguay",
                new DTFecha(1, 6, 2027), new DTFecha(3, 6, 2027), DTFecha.desde(LocalDate.now())));
    }

    // ============================================ Paso 1 · Alta de Usuario

    @Test
    @Order(1)
    @DisplayName("Paso 1 · el alta de usuario son tres llamadas encadenadas")
    void altaDeUsuarioEnTresPasos() throws ReglaNegocioException {
        // Datos de prueba y ejecucion
        altaAsistente();
        sistema.altaInstitucion(INSTITUCION, "Institucion de prueba", "https://test.uy");
        sistema.seleccionarInstitucion(INSTITUCION);

        altaOrganizador();

        // Resultado esperado
        Set<String> nicknames = sistema.listarUsuarios().stream()
                .map(DTUsuario::getNickname)
                .collect(Collectors.toSet());
        assertTrue(nicknames.contains(ASISTENTE));
        assertTrue(nicknames.contains(ORGANIZADOR));

        DTAsistente creado = (DTAsistente) sistema.seleccionarUsuario(ASISTENTE);
        assertEquals("Prueba", creado.getApellido());
        assertEquals(INSTITUCION, creado.getInstitucion());
    }

    @Test
    @Order(2)
    @DisplayName("CI · no deja crear dos usuarios con el mismo nickname")
    void nicknameRepetido() throws ReglaNegocioException {
        altaAsistente();

        assertThrows(ReglaNegocioException.class, () -> sistema.ingresarDatosUsuario(
                new DTUsuario(ASISTENTE, "Otro", "otro-l1@mail.com"), TipoUsuario.ASISTENTE));
    }

    @Test
    @Order(3)
    @DisplayName("CI · no deja crear dos usuarios con el mismo correo")
    void correoRepetido() throws ReglaNegocioException {
        altaAsistente();

        assertThrows(ReglaNegocioException.class, () -> sistema.ingresarDatosUsuario(
                new DTUsuario("test-l1-otro", "Otro", ASISTENTE + "@mail.com"),
                TipoUsuario.ASISTENTE));
    }

    // ========================================= Paso 2 · Alta de Institucion

    @Test
    @Order(4)
    @DisplayName("Paso 2 · alta de institucion, y aparece en el listado")
    void altaDeInstitucion() throws ReglaNegocioException {
        sistema.altaInstitucion(INSTITUCION, "Institucion de prueba", "https://test.uy");

        assertTrue(sistema.listarNombresInstituciones().contains(INSTITUCION));
    }

    @Test
    @Order(5)
    @DisplayName("CI · el nombre de la institucion es unico en la plataforma")
    void institucionRepetida() throws ReglaNegocioException {
        sistema.altaInstitucion(INSTITUCION, "Institucion de prueba", "https://test.uy");

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaInstitucion(INSTITUCION, "Otra", "https://otra.uy"));
    }

    // ============================================== Paso 3 · Alta de Evento

    @Test
    @Order(6)
    @DisplayName("Paso 3 · alta de evento con sus categorias")
    void altaDeEvento() throws ReglaNegocioException {
        altaEvento();

        DTEvento creado = sistema.seleccionarEvento(EVENTO);
        assertEquals(EVENTO, creado.getNombre());
        assertFalse(creado.getCategorias().isEmpty());

        assertTrue(sistema.listarEventos().stream()
                .anyMatch(e -> EVENTO.equals(e.getNombre())));
    }

    @Test
    @Order(7)
    @DisplayName("CI · seleccionar un evento que no existe")
    void eventoInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.seleccionarEvento("No Existe 2099"));
    }

    // ============================================= Paso 4 · Alta de Edicion

    @Test
    @Order(8)
    @DisplayName("Paso 4 · alta de edicion sobre el evento seleccionado")
    void altaDeEdicion() throws ReglaNegocioException {
        altaOrganizador();
        altaEvento();
        altaEdicion();

        assertTrue(sistema.listarEdicionesDeEvento(EVENTO).stream()
                .anyMatch(ed -> EDICION.equals(ed.getNombre())));
    }

    @Test
    @Order(9)
    @DisplayName("CI · no se puede repetir el nombre de una edicion")
    void edicionRepetida() throws ReglaNegocioException {
        altaOrganizador();
        altaEvento();
        altaEdicion();

        // El nombre de la edicion identifica a la edicion en toda la
        // plataforma: la segunda tiene que ser rechazada.
        assertThrows(ReglaNegocioException.class, this::altaEdicion);
    }

    // ===================================== Paso 5 · Alta de Tipo de Registro

    @Test
    @Order(10)
    @DisplayName("Paso 5 · alta de tipo de registro sobre la edicion seleccionada")
    void altaDeTipoDeRegistro() throws ReglaNegocioException {
        altaOrganizador();
        altaEvento();
        altaEdicion();

        sistema.seleccionarEdicionEvento(EDICION);
        sistema.ingresarDatosTipoRegistro(TIPO, "Entrada de prueba", 1000.0, 10);

        DTTipoRegistro creado = sistema.seleccionarTipoRegistro(TIPO);
        assertEquals(1000.0, creado.getCosto());
        assertEquals(10, creado.getCupo());
    }

    @Test
    @Order(11)
    @DisplayName("CI · el nombre del tipo es unico dentro de la edicion")
    void tipoRepetidoEnLaEdicion() throws ReglaNegocioException {
        altaOrganizador();
        altaEvento();
        altaEdicion();
        sistema.seleccionarEdicionEvento(EDICION);
        sistema.ingresarDatosTipoRegistro(TIPO, "Entrada de prueba", 1000.0, 10);

        assertThrows(ReglaNegocioException.class,
                () -> sistema.ingresarDatosTipoRegistro(TIPO, "Otra", 500.0, 5));
    }

    // ========================================= Paso 6 · Alta de Patrocinio

    @Test
    @Order(12)
    @DisplayName("Paso 6 · alta de patrocinio sobre la edicion seleccionada")
    void altaDePatrocinioConEstado() throws ReglaNegocioException {
        crearEscenarioCompleto();

        // La version del Lab 1 trabaja sobre la edicion que quedo seleccionada
        // por listarTiposRegistroDeEdicion.
        sistema.seleccionarEvento(EVENTO);
        sistema.listarTiposRegistroDeEdicion(EDICION);
        sistema.altaPatrocinio(new DTPatrocinio(CODIGO, DTFecha.desde(LocalDate.now()),
                100000.0, "ORO", 1, INSTITUCION, TIPO));

        assertTrue(sistema.listarPatrociniosDeEdicion(EDICION).stream()
                .anyMatch(p -> CODIGO.equals(p.getCodigoPatrocinio())));

        assertEquals(INSTITUCION, sistema.mostrarPatrocinio(CODIGO).getInstitucion());
    }

    // ============================================== Paso 7 · Las consultas

    @Test
    @Order(13)
    @DisplayName("Paso 7 · las consultas sobre todo lo creado")
    void lasConsultas() throws ReglaNegocioException {
        crearEscenarioCompleto();

        assertFalse(sistema.listarCategorias().isEmpty());
        assertFalse(sistema.listarCategoriasArbol().isEmpty());
        assertFalse(sistema.listarOrganizadores().isEmpty());
        assertFalse(sistema.listarUsuarios().isEmpty());
        assertFalse(sistema.listarEventos().isEmpty());

        assertNotNull(sistema.seleccionarOrganizador(ORGANIZADOR));

        sistema.seleccionarEvento(EVENTO);
        assertNotNull(sistema.seleccionarEdicionEvento(EDICION));

        DTEdicionCompleto completa = sistema.seleccionarEdicionCompleta(EDICION);
        assertEquals(EDICION, completa.getNombre());
        assertFalse(completa.getTiposRegistro().isEmpty());

        DTDatosRegistro datos = sistema.listarDatosRegistro(EDICION);
        assertFalse(datos.getTipoRegistro().isEmpty());
        assertFalse(datos.getAsistentes().isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("Paso 7 · un registro se consulta por asistente y por edicion")
    void consultarRegistros() throws ReglaNegocioException {
        crearEscenarioCompleto();
        sistema.altaRegistro(ASISTENTE, EDICION, TIPO);

        assertFalse(sistema.listarRegistroUsuario(ASISTENTE).isEmpty());

        DTRegistro registro = sistema.obtenerRegistro(ASISTENTE, EDICION);
        assertEquals(1000.0, registro.getCosto());
        assertEquals(TIPO, registro.getTipoRegistro());
    }

    // ================================= Paso 8 · Modificar Datos de Usuario

    @Test
    @Order(15)
    @DisplayName("Paso 8 · modificar los datos del usuario seleccionado")
    void modificarDatosConEstado() throws ReglaNegocioException {
        altaAsistente();

        sistema.seleccionarUsuario(ASISTENTE);
        sistema.modificarDatosUsuario(new DTAsistente(ASISTENTE, "Nombre Nuevo",
                ASISTENTE + "@mail.com", "Apellido Nuevo",
                new DTFecha(2, 2, 1999), null));

        DTAsistente despues = (DTAsistente) sistema.seleccionarUsuario(ASISTENTE);
        assertEquals("Nombre Nuevo", despues.getNombre());
        assertEquals("Apellido Nuevo", despues.getApellido());
    }

    // ============================= las operaciones con estado, sin estado

    @ParameterizedTest(name = "seleccionar \"{0}\"")
    @DisplayName("CI · seleccionar algo que no existe siempre es un error")
    @ValueSource(strings = {"No Existe 2099", "", "   "})
    void seleccionarInexistente(String nombre) {
        assertThrows(ReglaNegocioException.class, () -> sistema.seleccionarUsuario(nombre));
        assertThrows(ReglaNegocioException.class, () -> sistema.seleccionarEvento(nombre));
        assertThrows(ReglaNegocioException.class, () -> sistema.seleccionarOrganizador(nombre));
    }
}
