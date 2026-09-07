package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Capa de persistencia: punto unico de acceso a JPA.
 *
 * La letra (seccion 7.3) pide separar interfaz grafica, logica y persistencia.
 * Este paquete es esa tercera capa: es el UNICO lugar del proyecto que conoce
 * al EntityManagerFactory. Ni la GUI ni el Sistema saben que existe Hibernate.
 *
 * Por que un solo EntityManagerFactory para toda la aplicacion:
 *  - Crearlo es CARO (lee el persistence.xml, se conecta, valida el mapeo).
 *    Se hace una vez y se reusa.
 *  - En cambio el EntityManager es BARATO y no se comparte: se pide uno para
 *    cada operacion y se cierra al terminar.
 */
public final class Persistencia {

    /** Tiene que coincidir con el persistence-unit name del persistence.xml. */
    private static final String UNIDAD = "eventosuy";

    private static EntityManagerFactory emf;

    private Persistencia() {
    }

    /**
     * Devuelve un EntityManager nuevo. La primera llamada crea el
     * EntityManagerFactory; las siguientes reusan el mismo.
     *
     * Quien lo pide es responsable de cerrarlo (idealmente en un finally).
     */
    public static EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(UNIDAD);
        }
        return emf.createEntityManager();
    }

    /** Cierra la fabrica. Se llama una sola vez, al terminar la aplicacion. */
    public static void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
