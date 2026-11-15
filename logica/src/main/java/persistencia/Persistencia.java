package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

/**
 * Capa de persistencia: punto unico de acceso a JPA.
 *
 * La letra (seccion 7.3) pide separar interfaz grafica, logica y persistencia.
 * Este paquete es esa tercera capa: es el UNICO lugar del proyecto que conoce
 * al EntityManagerFactory. Ni la GUI ni el Sistema saben que existe Hibernate.
 *
 * <h2>Un EntityManager por operacion (la "unidad de trabajo")</h2>
 *
 * Antes habia UN SOLO EntityManager para toda la aplicacion, vivo desde que
 * arrancaba hasta que se cerraba. Eso traia tres problemas, que parecian
 * distintos pero tenian la misma causa:
 *
 *   1. Un EntityManager se acuerda de todo lo que leyo. Si otro programa
 *      cambiaba la base (el sitio y la Estacion de Trabajo son dos programas
 *      sobre la misma base), este seguia viendo lo viejo. Por eso existian el
 *      boton "Actualizar datos" y el filtro del sitio, que lo vaciaban a mano.
 *
 *   2. No se puede usar desde dos hilos a la vez. Por eso existia un candado
 *      que hacia que Tomcat atendiera las paginas de a una.
 *
 *   3. Si una operacion cambiaba objetos en memoria y despues fallaba, esos
 *      cambios se quedaban en el EntityManager, vivos, aunque nunca llegaran a
 *      la base. La siguiente operacion los veia (y hasta los guardaba).
 *
 * Ahora cada operacion abre SU EntityManager, lo usa, y lo cierra. Se llama
 * "unidad de trabajo" y funciona asi:
 *
 *     abre un EntityManager nuevo y empieza una transaccion
 *     ejecuta la operacion
 *     si salio bien  -> commit: lo que se cambio en memoria se guarda
 *     si algo fallo  -> rollback: no se guarda nada
 *     cierra el EntityManager (descarta TODO lo que habia en memoria)
 *
 * Con eso los tres problemas desaparecen juntos:
 *   1. Cada operacion empieza con memoria vacia, asi que lee la base de verdad.
 *   2. Cada hilo tiene el suyo: no se comparten, no hace falta candado.
 *   3. Si algo falla, el EntityManager se tira entero con todo lo que tenia.
 *
 * Importante: TODOS los Manejadores de una misma operacion usan el MISMO
 * EntityManager (el de la unidad abierta en ese hilo). Si cada Manejador
 * abriera el suyo, un objeto leido por uno quedaria desconectado (detached)
 * para el otro y al guardarlo fallaria con "detached entity passed to
 * persist". Por eso "uno por operacion, compartido", y no "uno por Manejador".
 *
 * Quien abre y cierra la unidad NO son los Manejadores ni el Sistema: es un
 * unico intermediario (ver logica.ControladorConUnidadDeTrabajo) que envuelve
 * cada llamada al controlador. Asi el try / commit / rollback esta escrito una
 * sola vez en todo el proyecto.
 */
public final class Persistencia {

    /** Tiene que coincidir con el persistence-unit name del persistence.xml. */
    private static final String UNIDAD = "eventosuy";

    private static EntityManagerFactory emf;

    /**
     * El EntityManager de la unidad de trabajo abierta en ESTE hilo.
     *
     * ThreadLocal es "una variable con un valor distinto para cada hilo": el
     * hilo A ve el suyo y el hilo B el suyo, sin pisarse. Es lo que permite
     * que dos peticiones simultaneas de Tomcat (dos hilos) tengan cada una su
     * EntityManager sin que los Manejadores tengan que recibirlo por parametro.
     *
     * Vale null cuando este hilo no esta dentro de ninguna unidad.
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
     * La fabrica de EntityManagers. Se crea UNA vez por programa.
     *
     * Es cara: lee el persistence.xml, se conecta a PostgreSQL y valida el
     * mapeo de las 10 entidades. Por eso no se crea una por operacion: la
     * fabrica es una sola y es lo unico que se comparte entre hilos (esta
     * hecha para eso). Lo barato es crear un EntityManager a partir de ella.
     *
     * synchronized porque en Tomcat la llaman varios hilos a la vez: al abrir
     * una pagina el navegador pide en paralelo el HTML, el CSS, las tipografias
     * y cada imagen. Sin esto, diez hilos entran juntos al if (emf == null),
     * construyen diez fabricas y se pisan actualizando el esquema.
     */
    private static synchronized EntityManagerFactory fabrica() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(UNIDAD);
        }
        return emf;
    }

    /**
     * Devuelve el EntityManager de la unidad de trabajo abierta en este hilo.
     *
     * Lo usan los Manejadores para consultar. No hay que cerrarlo: lo cierra
     * quien abrio la unidad, al terminarla.
     *
     * Si se llama fuera de una unidad de trabajo es un error de programa, y se
     * avisa en voz alta en lugar de abrir un EntityManager "por las dudas": uno
     * abierto por las dudas es uno que nadie cierra, y es justo el tipo de
     * filtracion de memoria y de datos viejos que esta clase viene a evitar.
     * Para usarlo desde un test o un programa suelto, envolver el codigo en
     * enUnidadDeTrabajo(...) o en enTransaccion(...).
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
     * Corre la tarea dentro de una unidad de trabajo: un EntityManager nuevo y
     * una transaccion que se confirma si todo sale bien y se deshace si no.
     *
     * Es el patron begin / commit / rollback / close, escrito una sola vez:
     *
     *     em = nuevo;  tx.begin();
     *     try { tarea; tx.commit(); }
     *     catch (...) { tx.rollback(); throw ...; }
     *     finally { em.close(); }
     *
     * El throw es importante: la excepcion tiene que seguir subiendo hasta el
     * catch de la pantalla, que la muestra. Si la tapamos aca, el usuario
     * creeria que se guardo y no se guardo.
     *
     * El finally tambien: pase lo que pase, el EntityManager se cierra y se
     * saca del hilo. Los hilos de Tomcat se reutilizan; si quedara uno
     * colgado, la peticion siguiente que caiga en ese hilo lo heredaria.
     *
     * <b>Anidada:</b> si este hilo ya tiene una unidad abierta, la tarea se une
     * a ella y NO hace commit ni rollback propios: eso lo decide la unidad de
     * afuera. Es lo que pasa cuando un Manejador llama a enTransaccion(...)
     * adentro de una operacion del controlador: todo es UNA sola transaccion, y
     * se guarda todo o no se guarda nada.
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
     * Ejecuta una escritura dentro de la unidad de trabajo actual (o abre una
     * propia si no hay ninguna).
     *
     * Es lo que usan los Manejadores:
     *
     *     public void agregar(Institucion institucion) {
     *         Persistencia.enTransaccion(em -> em.persist(institucion));
     *     }
     *
     * Dentro de una operacion del controlador NO abre ni cierra transaccion:
     * se une a la que ya esta abierta y el commit llega al final de la
     * operacion. Eso es lo que hace que una operacion que falla a la mitad no
     * deje nada guardado. Fuera de una operacion (un test, o DatosDePrueba si
     * no la envolvio) se comporta como antes: abre, guarda y cierra.
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
     * Devuelve la version de esta entidad que pertenece al EntityManager de la
     * unidad actual, o null si ya no existe en la base.
     *
     * Hace falta porque algunas operaciones del Laboratorio 1 son "con estado":
     * una llamada deja guardada una entidad (por ejemplo la edicion elegida) y
     * la siguiente la usa. Pero la unidad de trabajo de la primera llamada ya
     * se cerro, y con ella el EntityManager: esa entidad quedo desconectada
     * (detached), con los datos de ese momento y sin poder leer sus colecciones.
     * Esto la vuelve a buscar por su id en el EntityManager nuevo, asi cada
     * operacion trabaja con datos frescos y conectados.
     *
     * Si la entidad nunca llego a la base (no tiene id) o alguien la borro,
     * devuelve null: es como si no se hubiera elegido nada.
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
