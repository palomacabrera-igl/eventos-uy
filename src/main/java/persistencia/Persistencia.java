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
 * UN SOLO EntityManager PARA TODA LA APLICACION
 *
 * Es lo mismo que hace la demo ProyectoJPA del curso: su App.java crea UN
 * EntityManager al principio, lo usa en todos los metodos y lo cierra al final.
 *
 * Por que nos importa: si cada Manejador abriera y cerrara el suyo, un objeto
 * leido por uno quedaria "detached" (desconectado) para el otro, y al intentar
 * guardarlo saltaria "detached entity passed to persist". Por ejemplo, en Alta
 * de Usuario se busca la Institucion con un Manejador y se guarda el Asistente
 * con otro: con dos EntityManager distintos, eso falla.
 *
 * Con uno solo, todas las entidades viven en el mismo contexto de persistencia
 * y el problema no existe. Para el volumen de datos del laboratorio no tiene
 * ninguna contra.
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
     * Es el patron begin / commit / rollback del teorico y de la demo del
     * profe, escrito UNA sola vez en lugar de repetirlo en cada Manejador:
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
