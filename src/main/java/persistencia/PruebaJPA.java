package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import logica.Categoria;

import java.util.List;

/**
 * PRUEBA DE JPA DE PUNTA A PUNTA CON UNA SOLA ENTIDAD (Categoria).
 *
 * No es parte de ningun caso de uso ni la usa la Estacion de Trabajo: es un
 * programa aparte para comprobar que el mapeo, la conexion y las
 * transacciones funcionan antes de tocar el resto del sistema.
 *
 * Como correrlo:
 *   IntelliJ -> boton verde al lado de main()
 *   Terminal -> .\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.PruebaJPA"
 *
 * Es idempotente: se puede correr muchas veces sin romper nada.
 */
public class PruebaJPA {

    public static void main(String[] args) {
        EntityManager em = Persistencia.getEntityManager();
        try {
            System.out.println("\n=== 1. Alta de categorias (idempotente) ===");
            guardar(em, "Ingenieria");
            guardar(em, "Charlas");
            guardar(em, "Talleres");

            System.out.println("\n=== 2. Consulta JPQL ===");
            listar(em);

            System.out.println("\n=== 3. Demo de rollback (nombre repetido) ===");
            demoRollback(em);

            System.out.println("\n=== 4. La base quedo igual que en el paso 2 ===");
            listar(em);
        } finally {
            em.close();
            Persistencia.cerrar();
            System.out.println("\nFin de la prueba.");
        }
    }

    /**
     * Da de alta una categoria si no existe todavia.
     *
     * Este es EL patron de escritura de JPA, el mismo que usa la demo
     * ProyectoJPA del profe:
     *      tx.begin();
     *      try { ...; tx.commit(); }
     *      catch (Exception e) { tx.rollback(); throw e; }
     *
     * begin/commit delimitan la transaccion: o se guarda todo, o no se guarda
     * nada. Si algo falla, rollback deshace lo que se hizo desde el begin.
     */
    private static void guardar(EntityManager em, String nombre) {
        // Verificacion por la logica (ademas de la restriccion UNIQUE del mapeo).
        if (buscar(em, nombre) != null) {
            System.out.println("  ya existia:  " + nombre);
            return;
        }

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            em.persist(new Categoria(nombre));  // persist = "guardame esto"
            tx.commit();                        // commit  = "confirmalo en la BD"
            System.out.println("  guardada:    " + nombre);
        } catch (Exception e) {
            tx.rollback();                      // rollback = "deshace todo"
            throw e;
        }
    }

    /**
     * Consulta con JPQL. Ojo con la diferencia contra SQL: JPQL se escribe
     * sobre la CLASE Java y sus atributos ("FROM Categoria c WHERE c.nombre"),
     * no sobre la tabla y sus columnas. Hibernate lo traduce a SQL solo.
     *
     * :n es un parametro. Se usa setParameter en vez de concatenar el texto
     * (concatenar seria inseguro y ademas mas lento).
     */
    private static Categoria buscar(EntityManager em, String nombre) {
        return em.createQuery(
                        "SELECT c FROM Categoria c WHERE c.nombre = :n", Categoria.class)
                .setParameter("n", nombre)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private static void listar(EntityManager em) {
        List<Categoria> todas = em.createQuery(
                        "SELECT c FROM Categoria c ORDER BY c.nombre", Categoria.class)
                .getResultList();

        System.out.println("  categorias en la base: " + todas.size());
        for (Categoria c : todas) {
            System.out.println("    id=" + c.getId() + "  nombre=" + c.getNombre());
        }
    }

    /**
     * Demuestra que la restriccion UNIQUE del mapeo realmente funciona.
     *
     * Se intenta guardar una categoria que ya existe SIN verificar antes. La
     * base la rechaza, el commit falla y el rollback deja todo como estaba.
     *
     * Esto es lo que la letra (7.1) pide comprobar: que la unicidad este
     * "reflejada en el mapeo", no solo controlada por el programa.
     */
    private static void demoRollback(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            em.persist(new Categoria("Ingenieria")); // repetida a proposito
            tx.commit();
            System.out.println("  INESPERADO: la BD acepto un nombre repetido.");
        } catch (Exception e) {
            tx.rollback();
            System.out.println("  rollback OK: la BD rechazo el nombre repetido.");
            System.out.println("  excepcion: " + e.getClass().getSimpleName());
        }
    }
}
