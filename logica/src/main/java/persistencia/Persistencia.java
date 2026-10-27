package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.concurrent.locks.ReentrantLock;

import java.util.function.Consumer;

/**
 * Capa de persistencia: punto unico de acceso a JPA.
 *
 * La letra (seccion 7.3) pide separar interfaz grafica, logica y persistencia.
 * Este paquete es esa tercera capa: es el UNICO lugar del proyecto que conoce
 * al EntityManagerFactory. Ni la GUI ni el Sistema saben que existe Hibernate.
 *
 * Hay UN SOLO EntityManager para toda la aplicacion. Si cada Manejador
 * abriera el suyo, un objeto leido por uno quedaria desconectado (detached)
 * para el otro y al guardarlo fallaria con "detached entity passed to
 * persist": pasa, por ejemplo, en Alta de Usuario, donde se busca la
 * Institucion con un Manejador y se guarda el Asistente con otro.
 */
public final class Persistencia {

    /** Tiene que coincidir con el persistence-unit name del persistence.xml. */
    private static final String UNIDAD = "eventosuy";

    /**
     * Candado de la unidad de trabajo.
     *
     * Hay UN SOLO EntityManager para toda la aplicacion, y un EntityManager
     * no se puede usar desde dos hilos a la vez (lo dice la especificacion de
     * JPA). Tomcat atiende cada peticion en un hilo distinto, asi que sin esto
     * dos paginas pedidas al mismo tiempo se pisan: una llama a clear()
     * mientras la otra esta leyendo, y saltan ConcurrentModificationException
     * y errores de JDBC.
     *
     * Es la misma idea que usa la demo del curso, donde los metodos del
     * Manejador son synchronized. La diferencia es que aca la zona protegida
     * no es un metodo sino TODA la peticion: desde que se refresca hasta que
     * se termino de dibujar la pagina. Por eso es un ReentrantLock y no un
     * bloque synchronized, que no puede abrirse en un metodo y cerrarse en
     * otro.
     */
    private static final ReentrantLock CANDADO = new ReentrantLock();

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private Persistencia() {
    }

    /**
     * Devuelve EL EntityManager de la aplicacion (siempre el mismo).
     *
     * La primera llamada crea la fabrica, que es cara: lee el persistence.xml,
     * se conecta a PostgreSQL y valida el mapeo de las 10 entidades. Las
     * siguientes llamadas reusan todo.
     *
     * No hay que cerrarlo: lo cierra cerrar() al terminar la aplicacion.
     *
     * synchronized porque en Tomcat esto lo llaman varios hilos a la vez: al
     * abrir una pagina el navegador pide en paralelo el HTML, el CSS, las
     * tipografias y cada imagen. Sin esto, diez hilos entran juntos al
     * if (emf == null), construyen diez fabricas y se pisan actualizando el
     * esquema. En la Estacion de Trabajo no se nota: hay un solo hilo.
     */
    public static synchronized EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(UNIDAD);
        }
        if (em == null || !em.isOpen()) {
            em = emf.createEntityManager();
        }
        return em;
    }

    /**
     * Ejecuta una operacion de escritura dentro de una transaccion.
     *
     * Es el patron begin / commit / rollback, escrito una sola vez en lugar de
     * repetirlo en cada Manejador:
     *
     *     tx.begin();
     *     try { ...; tx.commit(); }
     *     catch (Exception e) { tx.rollback(); throw e; }
     *
     * El throw es importante: la excepcion tiene que seguir subiendo hasta el
     * catch de la pantalla, que la muestra con Mensajes.errorInesperado(). Si
     * la tapamos aca, el usuario creeria que se guardo y no se guardo.
     *
     * Uso desde un Manejador:
     *
     *     public void agregar(Institucion institucion) {
     *         Persistencia.enTransaccion(em -> em.persist(institucion));
     *     }
     */
    public static void enTransaccion(Consumer<EntityManager> accion) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            accion.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Vuelve a leer de la base en lugar de usar lo que el EntityManager tiene
     * guardado en memoria.
     *
     * Un EntityManager recuerda todo lo que leyo mientras vive. Como el sitio
     * web y la Estacion de Trabajo son dos procesos distintos sobre la misma
     * base, cada uno con el suyo, sin esto una edicion aceptada desde la
     * Estacion de Trabajo se seguiria viendo Ingresada en el sitio hasta
     * reiniciar Tomcat. La letra (seccion 3) pide lo contrario: un cambio
     * hecho en una aplicacion tiene que estar disponible en la otra.
     *
     * clear() desconecta todo lo que tenia cargado, asi que la proxima
     * consulta va a la base de verdad.
     *
     * Quien la llama:
     *   - la Estacion de Trabajo, desde el boton "Actualizar datos";
     *   - el sitio, al empezar cada pagina, desde el filtro del modulo web.
     *
     * No llamarla en medio de una operacion: desconecta las entidades que se
     * esten usando en ese momento.
     */
    public static void refrescar() {
        EntityManager em = getEntityManager();
        if (em.isOpen()) {
            em.clear();
        }
    }

    /**
     * Empieza una unidad de trabajo: espera el turno y vuelve a leer de la base.
     *
     * El refresco va ADENTRO del candado a proposito. Si estuviera afuera,
     * entre que un hilo refresca y lee, otro podria refrescar y borrarle lo
     * que acababa de cargar: el error volveria igual.
     *
     * Quien la llama tiene que cerrar la unidad en un finally, SIEMPRE. Si una
     * pagina falla y el candado no se suelta, el sitio queda colgado para
     * todos hasta reiniciar Tomcat.
     */
    public static void abrirUnidadDeTrabajo() {
        CANDADO.lock();
        refrescar();
    }

    /** Termina la unidad de trabajo y le deja el turno al siguiente. */
    public static void cerrarUnidadDeTrabajo() {
        if (CANDADO.isHeldByCurrentThread()) {
            CANDADO.unlock();
        }
    }

    /** Cierra todo. Se llama una sola vez, al terminar la aplicacion. */
    public static void cerrar() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        em = null;
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
