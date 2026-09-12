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
 * Hay UN SOLO EntityManager para toda la aplicacion. Si cada Manejador
 * abriera el suyo, un objeto leido por uno quedaria desconectado (detached)
 * para el otro y al guardarlo fallaria con "detached entity passed to
 * persist": pasa, por ejemplo, en Alta de Usuario, donde se busca la
 * Institucion con un Manejador y se guarda el Asistente con otro.
 */
public final class Persistencia {

    /** Tiene que coincidir con el persistence-unit name del persistence.xml. */
    private static final String UNIDAD = "eventosuy";

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
     */
    public static EntityManager getEntityManager() {
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
