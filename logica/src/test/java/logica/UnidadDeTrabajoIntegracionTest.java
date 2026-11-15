package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import persistencia.Persistencia;

/**
 * Un EntityManager por operacion ("unidad de trabajo").
 *
 * La Estacion de Trabajo y el sitio comparten la base, y antes compartian un
 * unico EntityManager por programa. Estas pruebas dejan escrito, con codigo, lo
 * que cambio y por que importa:
 *
 * <pre>
 *  1. Cada operacion tiene SU EntityManager y lo cierra al terminar.
 *  2. Lo que otro programa cambia en la base se ve en la operacion siguiente,
 *     sin boton "Actualizar datos" ni filtro que refresque.
 *  3. Si una operacion cambia cosas en memoria y despues falla, esos cambios
 *     NO llegan a la base y NO quedan en memoria para la operacion siguiente.
 *  4. Los datos que una llamada deja "seleccionados" (Laboratorio 1) se
 *     vuelven a conectar a la base en la llamada siguiente.
 *  5. Dos personas registrandose a la vez al ultimo lugar: entra una sola.
 * </pre>
 *
 * Necesitan PostgreSQL (ver PruebaConBase). Cada una arma lo suyo con el
 * prefijo "Test UoW" / "test-uow" y lo borra al terminar.
 */
@DisplayName("Unidad de trabajo: un EntityManager por operacion")
class UnidadDeTrabajoIntegracionTest extends PruebaConBase {

    private static final String INSTITUCION = "Test UoW Institucion";
    private static final String ORIGINAL = "descripcion original";
    private static final String CAMBIADA = "descripcion cambiada";

    private static final String EVENTO = "Test UoW Evento";
    private static final String EDICION = "Test UoW 2027";
    private static final String TIPO = "General";
    private static final String ORGANIZADOR = "test-uow-org";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    @AfterEach
    void sinRastros() {
        // El orden importa: primero lo que apunta, despues lo apuntado.
        borrar("registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "')");
        borrar("tipo_registro", "edicion_id IN (SELECT id FROM edicion_evento WHERE nombre = '"
                + EDICION + "')");
        borrar("edicion_evento", "nombre = '" + EDICION + "'");
        borrar("evento_categoria",
                "evento_id IN (SELECT id FROM evento WHERE nombre = '" + EVENTO + "')");
        borrar("evento", "nombre = '" + EVENTO + "'");
        borrar("usuario", "nickname LIKE 'test-uow-%'");
        borrar("institucion", "nombre = '" + INSTITUCION + "'");
    }

    // ================================================== 1. el EntityManager

    @Test
    @DisplayName("Sin una unidad de trabajo abierta no se entrega ningun EntityManager")
    void fueraDeUnaUnidadNoHayEntityManager() {
        // Un EntityManager "por las dudas" es uno que nadie cierra: se prefiere
        // avisar en voz alta del error de programa.
        assertThrows(IllegalStateException.class, Persistencia::getEntityManager);
    }

    @Test
    @DisplayName("Cada operacion abre SU EntityManager y lo cierra al terminar")
    void cadaUnidadTieneSuEntityManager() {
        EntityManager[] primero = new EntityManager[1];
        EntityManager[] segundo = new EntityManager[1];

        Persistencia.enTransaccion(em -> primero[0] = em);
        Persistencia.enTransaccion(em -> segundo[0] = em);

        assertFalse(primero[0].isOpen(), "al terminar la operacion, el EntityManager se cierra");
        assertNotSame(primero[0], segundo[0], "la operacion siguiente abre uno nuevo");
        assertThrows(IllegalStateException.class, Persistencia::getEntityManager,
                "y el hilo no se queda con ninguno: el proximo pedido que caiga en "
                        + "este hilo (en Tomcat los hilos se reutilizan) empieza de cero");
    }

    @Test
    @DisplayName("Una unidad anidada se une a la de afuera: es UNA sola transaccion")
    void lasUnidadesAnidadasComparten() {
        Persistencia.enTransaccion(afuera ->
                Persistencia.enTransaccion(adentro -> assertSame(afuera, adentro)));

        // Y lo que hace una anidada se deshace si la de afuera falla.
        assertThrows(IllegalStateException.class, () -> Persistencia.enTransaccion(em -> {
            Persistencia.enTransaccion(adentro -> adentro.persist(institucion()));
            throw new IllegalStateException("falla despues de que la anidada 'termino'");
        }));
        assertNull(buscarInstitucion(), "el rollback de la de afuera tambien deshace lo de adentro");
    }

    // ================================================== 2. datos frescos

    @Test
    @DisplayName("Lo que otro programa cambia en la base se ve en la operacion siguiente, "
            + "sin boton ni refresco")
    void seVeLoQueCambioOtroPrograma() throws Exception {
        sistema.altaInstitucion(INSTITUCION, ORIGINAL, "https://prueba.uy");
        assertEquals(ORIGINAL, sistema.consultarInstitucion(INSTITUCION).getDescripcion());

        // "El otro programa" (la Estacion de Trabajo, si este es el sitio) tiene
        // su propia fabrica de EntityManagers y escribe en la misma base.
        EntityManagerFactory otroPrograma = Persistence.createEntityManagerFactory("eventosuy");
        try {
            EntityManager em = otroPrograma.createEntityManager();
            try {
                em.getTransaction().begin();
                em.createQuery("UPDATE Institucion i SET i.descripcion = :d WHERE i.nombre = :n")
                        .setParameter("d", CAMBIADA)
                        .setParameter("n", INSTITUCION)
                        .executeUpdate();
                em.getTransaction().commit();
            } finally {
                em.close();
            }
        } finally {
            otroPrograma.close();
        }

        // Antes, aca se seguia viendo la descripcion vieja hasta que alguien
        // apretaba "Actualizar datos" o reiniciaba Tomcat.
        assertEquals(CAMBIADA, sistema.consultarInstitucion(INSTITUCION).getDescripcion());
    }

    // ================================================== 3. lo que falla no queda

    @Test
    @DisplayName("Lo que se cambio en memoria y despues fallo NO llega a la base")
    void elCambioEnMemoriaQueFallaNoSeGuarda() throws Exception {
        sistema.altaInstitucion(INSTITUCION, ORIGINAL, "https://prueba.uy");

        assertThrows(IllegalStateException.class, () -> Persistencia.enTransaccion(em -> {
            buscarInstitucion(em).setDescripcion(CAMBIADA);   // cambia en memoria...
            throw new IllegalStateException("...y la operacion falla a la mitad");
        }));

        assertEquals(ORIGINAL, sistema.consultarInstitucion(INSTITUCION).getDescripcion());
    }

    @Test
    @DisplayName("Un registro hecho en memoria que no llego a la base no queda dando vueltas "
            + "(el caso de altaRegistro)")
    void elRegistroQueNoSeGuardoNoQuedaEnMemoria() throws Exception {
        armarEscenario(1);

        // Lo que hace Sistema.altaRegistro, pero con una falla antes del commit:
        // el registro se agrega a la edicion EN MEMORIA y la operacion se cae.
        assertThrows(IllegalStateException.class, () -> Persistencia.enTransaccion(em -> {
            EdicionEvento edicion = ManejadorEvento.getInstancia().buscarEdicion(EDICION);
            Asistente asistente = (Asistente) ManejadorUsuario.getInstancia().buscar("test-uow-a1");
            edicion.altaRegistro(asistente, edicion.buscarTipoRegistro(TIPO), LocalDate.now());
            assertTrue(edicion.estaRegistrado("test-uow-a1"), "en memoria SI esta registrado");
            throw new IllegalStateException("el commit falla");
        }));

        // Con un EntityManager unico, el registro fantasma seguia vivo en la
        // memoria: la edicion "veia" al asistente registrado y el cupo lleno,
        // aunque en la base no hubiera nada. Ahora la operacion siguiente abre
        // un EntityManager nuevo y lee la verdad:
        assertEquals(0, registrosGuardados(), "no se guardo nada");
        sistema.altaRegistro("test-uow-a1", EDICION, TIPO);
        assertEquals(1, registrosGuardados(), "y el asistente se puede registrar de verdad");
    }

    // ================================================== 4. estado del Laboratorio 1

    @Test
    @DisplayName("Lo que una llamada del Laboratorio 1 deja seleccionado se reconecta en la siguiente")
    void laSeleccionSobreviveEntreOperaciones() throws Exception {
        armarEscenario(1);

        // Dos llamadas = dos unidades de trabajo = dos EntityManager. La edicion
        // que deja elegida la primera quedo desconectada al cerrarse su unidad;
        // la segunda la necesita conectada para agregarle un tipo de registro.
        sistema.seleccionarEvento(EVENTO);
        sistema.listarEdicionesDeEvento(EVENTO);
        sistema.seleccionarEdicionEvento(EDICION);
        sistema.ingresarDatosTipoRegistro("Estudiante", "Tarifa estudiantil", 800.0, 5);

        assertTrue(sistema.listarTiposDeRegistro(EDICION).stream()
                        .anyMatch(t -> "Estudiante".equals(t.getNombre())),
                "el tipo nuevo quedo guardado en la base");
    }

    @Test
    @DisplayName("reanclar: da la misma fila en el EntityManager nuevo, y null si ya no existe")
    void reanclar() {
        Institucion[] guardada = new Institucion[1];
        Persistencia.enTransaccion(em -> {
            guardada[0] = institucion();
            em.persist(guardada[0]);
        });
        // Ahora "guardada[0]" esta desconectada: su EntityManager se cerro.

        Persistencia.enTransaccion(em -> {
            Institucion reconectada = Persistencia.reanclar(guardada[0], Institucion.class);
            assertTrue(em.contains(reconectada), "esta conectada al EntityManager nuevo");
            assertEquals(guardada[0].getId(), reconectada.getId(), "es la misma fila");
            assertSame(reconectada, Persistencia.reanclar(reconectada, Institucion.class),
                    "si ya esta conectada, se devuelve tal cual");
        });

        borrar("institucion", "nombre = '" + INSTITUCION + "'");
        Persistencia.enTransaccion(em ->
                assertNull(Persistencia.reanclar(guardada[0], Institucion.class),
                        "si la fila ya no existe, no se inventa: null"));

        Persistencia.enTransaccion(em ->
                assertNull(Persistencia.reanclar(null, Institucion.class)));
        Persistencia.enTransaccion(em ->
                assertNull(Persistencia.reanclar(institucion(), Institucion.class),
                        "una entidad que nunca se guardo (sin id) tampoco"));
    }

    // ================================================== 5. a la vez

    @Test
    @DisplayName("Seis personas se registran a la vez al unico lugar: entra una sola")
    void elCupoNoSePasaConRegistrosSimultaneos() throws Exception {
        int personas = 6;
        armarEscenario(personas);   // cupo 1, seis asistentes

        ExecutorService hilos = Executors.newFixedThreadPool(personas);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<String>> resultados = new ArrayList<>();
        for (int i = 1; i <= personas; i++) {
            String nickname = "test-uow-a" + i;
            Callable<String> registrarse = () -> {
                largada.await();   // todos arrancan en el mismo instante
                try {
                    sistema.altaRegistro(nickname, EDICION, TIPO);
                    return "entro";
                } catch (ReglaNegocioException rechazado) {
                    return "rechazado: " + rechazado.getMessage();
                }
            };
            resultados.add(hilos.submit(registrarse));
        }
        largada.countDown();

        int entraron = 0;
        for (Future<String> resultado : resultados) {
            String r = resultado.get();
            if (r.equals("entro")) {
                entraron++;
            } else {
                assertTrue(r.contains("cupo"), "se rechaza por el cupo, no por otra cosa: " + r);
            }
        }
        hilos.shutdown();

        assertEquals(1, entraron, "el cupo es 1: tiene que entrar una sola persona");
        assertEquals(1, registrosGuardados(), "y en la base hay un solo registro");
    }

    @Test
    @DisplayName("Registrarse ESPERA si otra operacion tiene trabada la edicion "
            + "(asi el cupo se cuenta despues, no a la vez)")
    void registrarseEsperaSiLaEdicionEstaTrabada() throws Exception {
        armarEscenario(1);
        CountDownLatch trabada = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            // Una operacion que esta "en el medio" de registrar a alguien: ya
            // trajo la edicion con el bloqueo y todavia no hizo commit.
            Future<?> otra = hilos.submit(() -> Persistencia.enTransaccion(em -> {
                ManejadorEvento.getInstancia().buscarEdicionBloqueandola(EDICION);
                trabada.countDown();
                try {
                    soltar.await();
                } catch (InterruptedException interrumpida) {
                    Thread.currentThread().interrupt();
                }
            }));
            trabada.await();

            Future<?> registro = hilos.submit(() -> {
                sistema.altaRegistro("test-uow-a1", EDICION, TIPO);
                return null;
            });

            // Mientras la otra tiene la edicion trabada, el registro no avanza.
            // (Sin el bloqueo, termina en milisegundos y esto falla.)
            assertThrows(TimeoutException.class, () -> registro.get(1, TimeUnit.SECONDS),
                    "el registro tendria que estar esperando a que la otra operacion termine");
            assertEquals(0, registrosGuardados());

            // Cuando la otra termina, el registro sigue solo.
            soltar.countDown();
            registro.get(10, TimeUnit.SECONDS);
            otra.get(10, TimeUnit.SECONDS);
            assertEquals(1, registrosGuardados());
        } finally {
            soltar.countDown();
            hilos.shutdownNow();
        }
    }

    // ================================================== apoyo

    private static Institucion institucion() {
        return new Institucion(INSTITUCION, ORIGINAL, "https://prueba.uy");
    }

    private static Institucion buscarInstitucion(EntityManager em) {
        return em.createQuery("SELECT i FROM Institucion i WHERE i.nombre = :n", Institucion.class)
                .setParameter("n", INSTITUCION)
                .getSingleResult();
    }

    /** La institucion de prueba, o null si no esta en la base. */
    private static Institucion buscarInstitucion() {
        Institucion[] encontrada = new Institucion[1];
        Persistencia.enTransaccion(em -> encontrada[0] = em
                .createQuery("SELECT i FROM Institucion i WHERE i.nombre = :n", Institucion.class)
                .setParameter("n", INSTITUCION)
                .getResultStream().findFirst().orElse(null));
        return encontrada[0];
    }

    private static long registrosGuardados() {
        long[] cantidad = new long[1];
        Persistencia.enTransaccion(em -> cantidad[0] = em.createQuery(
                "SELECT COUNT(r) FROM Registro r WHERE r.edicion.nombre = :e", Long.class)
                .setParameter("e", EDICION)
                .getSingleResult());
        return cantidad[0];
    }

    /** Un evento con una edicion Aceptada, un tipo de registro con ese cupo y N asistentes. */
    private void armarEscenario(int asistentes) throws ReglaNegocioException {
        Persistencia.enTransaccion(em -> {
            Categoria categoria = em.createQuery("SELECT c FROM Categoria c", Categoria.class)
                    .getResultStream().findFirst().orElseThrow();
            Organizador organizador = new Organizador(ORGANIZADOR, "Org de prueba",
                    "test-uow-org@mail.com", "Organiza la edicion de prueba", null, "Clave1!");
            Evento evento = new Evento(EVENTO, "Evento de prueba", LocalDate.now(), "TUW",
                    List.of(categoria));
            EdicionEvento edicion = new EdicionEvento(EDICION, "TUW27",
                    LocalDate.of(2027, 6, 1), LocalDate.of(2027, 6, 3),
                    LocalDate.now(), "Montevideo", "Uruguay", organizador);
            evento.agregarEdicion(edicion);
            organizador.agregarEdicion(edicion);
            edicion.crearTipoRegistro(TIPO, "Entrada general", 1500.0, 1);

            em.persist(organizador);
            em.persist(evento);
            for (int i = 1; i <= asistentes; i++) {
                em.persist(new Asistente("test-uow-a" + i, "Asistente " + i,
                        "test-uow-a" + i + "@mail.com", "Prueba", LocalDate.of(2000, 1, 1),
                        "Clave1!"));
            }
        });
        // A una edicion Ingresada no se puede registrar nadie: se acepta por el
        // caso de uso, como hace el administrador desde la Estacion.
        sistema.aceptarEdicion(EDICION);
    }
}
