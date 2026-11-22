package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

/**
 * Capa de persistencia: el unico lugar que conoce al EntityManagerFactory. Ni la
 * interfaz grafica ni Sistema saben que existe Hibernate.
 *
 * Un EntityManager por operacion ("unidad de trabajo"): cada operacion abre el
 * suyo, lo usa y lo cierra, con commit si sale bien y rollback si falla. Asi
 * cada operacion lee la base de verdad (lo que cambia el sitio lo ve la Estacion),
 * no hace falta un candado entre hilos y un fallo no deja cambios a medias.
 *
 * Todos los Manejadores de una operacion comparten ese mismo EntityManager: si
 * cada uno abriera el suyo, un objeto leido por uno quedaria desconectado para
 * el otro. Abrir y cerrar la unidad lo hace un unico intermediario
 * (ControladorConUnidadDeTrabajo), asi el try / commit / rollback esta una sola vez.
 */
public final class Persistencia {

    /** Tiene que coincidir con el persistence-unit name del persistence.xml. */
    private static final String UNIDAD = "eventosuy";

    private static EntityManagerFactory emf;

    /**
     * El EntityManager de la unidad de trabajo abierta en este hilo (ThreadLocal:
     * cada hilo ve el suyo). Vale null fuera de una unidad.
     */
    private static final ThreadLocal<EntityManager> UNIDAD_ACTUAL = new ThreadLocal<>();

    private Persistencia() {
    }

    /**
     * Una tarea para correr dentro de una unidad de trabajo. Recibe el
     * EntityManager de la unidad y puede lanzar cualquier excepcion (por
     * ejemplo ReglaNegocioException), que sale tal cual despues del rollback.
     */
    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar(EntityManager em) throws Exception;
    }

    /**
     * La fabrica de EntityManagers. Se crea una sola vez por programa porque es
     * cara (lee persistence.xml, se conecta y valida el mapeo) y es lo unico que
     * se comparte entre hilos. Es synchronized porque Tomcat la llama desde varios
     * hilos a la vez y sin eso se construirian varias fabricas.
     */
    private static synchronized EntityManagerFactory fabrica() {
        if (emf == null) {
            // La direccion, el usuario y la contrasena de la base NO estan en
            // persistence.xml: salen de ~/.eventosUy/central.properties (letra:
            // nada de credenciales en el codigo). Lo demas sigue en persistence.xml.
            emf = Persistence.createEntityManagerFactory(UNIDAD, Configuracion.cargar().conexionJpa());
        }
        return emf;
    }

    /**
     * El EntityManager de la unidad abierta en este hilo. Lo usan los Manejadores y
     * no hay que cerrarlo: lo cierra quien abrio la unidad. Fuera de una unidad
     * lanza un error en vez de abrir uno que nadie cierre; en un test hay que
     * envolver el codigo en enUnidadDeTrabajo(...).
     */
    public static EntityManager getEntityManager() {
        EntityManager em = UNIDAD_ACTUAL.get();
        if (em == null) {
            throw new IllegalStateException(
                    "No hay una unidad de trabajo abierta en este hilo. Las operaciones "
                            + "tienen que pasar por el controlador (Fabrica.getControladorSistema()) "
                            + "o correr dentro de Persistencia.enUnidadDeTrabajo(...).");
        }
        return em;
    }

    /**
     * Corre la tarea en una unidad de trabajo: EntityManager nuevo y transaccion
     * que se confirma si sale bien y se deshace si falla (begin / commit /
     * rollback / close, escrito una sola vez). La excepcion se vuelve a lanzar para
     * que llegue a la pantalla, y el finally cierra el EntityManager y lo saca del
     * hilo (los hilos de Tomcat se reutilizan).
     *
     * Si el hilo ya tiene una unidad abierta, la tarea se une a ella y no hace
     * commit ni rollback propios: toda la operacion es una sola transaccion.
     */
    public static <T> T enUnidadDeTrabajo(Trabajo<T> trabajo) throws Exception {
        EntityManager existente = UNIDAD_ACTUAL.get();
        if (existente != null) {
            return trabajo.ejecutar(existente);
        }

        EntityManager em = fabrica().createEntityManager();
        UNIDAD_ACTUAL.set(em);
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultado = trabajo.ejecutar(em);
            tx.commit();
            return resultado;
        } catch (Throwable fallo) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (RuntimeException alDeshacer) {
                    // Lo importante es la excepcion original; si ademas fallo
                    // el rollback (conexion caida), se anota y se sigue.
                    fallo.addSuppressed(alDeshacer);
                }
            }
            throw fallo;
        } finally {
            UNIDAD_ACTUAL.remove();
            em.close();
        }
    }

    /**
     * Ejecuta una escritura dentro de la unidad actual (o abre una propia si no
     * hay). Dentro de una operacion se une a la transaccion abierta y el commit
     * llega al final, asi una operacion que falla a la mitad no deja nada guardado.
     * Uso: Persistencia.enTransaccion(em -> em.persist(x)).
     */
    public static void enTransaccion(Consumer<EntityManager> accion) {
        try {
            enUnidadDeTrabajo(em -> {
                accion.accept(em);
                return null;
            });
        } catch (RuntimeException | Error sinCambios) {
            throw sinCambios;
        } catch (Exception inesperada) {
            // El Consumer no puede lanzar excepciones chequeadas, asi que no
            // deberia llegar nunca aca. Se envuelve para no perderla.
            throw new IllegalStateException(inesperada);
        }
    }

    /**
     * La version de esta entidad que pertenece al EntityManager de la unidad
     * actual, o null si ya no existe en la base. Hace falta para las operaciones
     * "con estado" del Laboratorio 1: la entidad que dejo una llamada quedo
     * desconectada al cerrarse su unidad, y esto la vuelve a buscar por id.
     */
    public static <T> T reanclar(T entidad, Class<T> tipo) {
        if (entidad == null) {
            return null;
        }
        EntityManager em = getEntityManager();
        if (em.contains(entidad)) {
            return entidad;
        }
        Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entidad);
        return id == null ? null : em.find(tipo, id);
    }

    /** Cierra todo. Se llama una sola vez, al terminar la aplicacion. */
    public static synchronized void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
