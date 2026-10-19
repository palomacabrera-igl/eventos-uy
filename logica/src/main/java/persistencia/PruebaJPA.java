package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import logica.Categoria;
import logica.Institucion;

import java.util.List;

public class PruebaJPA {

    public static void main(String[] args) {
        EntityManager em = Persistencia.getEntityManager();

        try {
            System.out.println("\n=== 1. Alta de categorias ===");
            guardarCategoria(em, "Ingenieria");
            guardarCategoria(em, "Charlas");
            guardarCategoria(em, "Talleres");

            System.out.println("\n=== 2. Consulta de categorias ===");
            listarCategorias(em);

            System.out.println("\n=== 3. Alta de instituciones ===");
            guardarInstitucion(
                    em,
                    "UTEC",
                    "Universidad Tecnológica",
                    "https://utec.edu.uy"
            );
            

            System.out.println("\n=== 4. Consulta de instituciones ===");
            listarInstituciones(em);

            System.out.println("\n=== 5. Demo de rollback: institución repetida ===");
            demoRollbackInstitucion(em);

        } finally {
            em.close();
            Persistencia.cerrar();
            System.out.println("\nFin de la prueba.");
        }
    }

    // ===== Categoría =====

    private static void guardarCategoria(EntityManager em, String nombre) {
        if (buscarCategoria(em, nombre) != null) {
            System.out.println("  ya existía: " + nombre);
            return;
        }

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            em.persist(new Categoria(nombre));
            tx.commit();
            System.out.println("  guardada: " + nombre);
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    private static Categoria buscarCategoria(EntityManager em, String nombre) {
        return em.createQuery(
                        "SELECT c FROM Categoria c WHERE c.nombre = :n",
                        Categoria.class)
                .setParameter("n", nombre)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private static void listarCategorias(EntityManager em) {
        List<Categoria> categorias = em.createQuery(
                        "SELECT c FROM Categoria c ORDER BY c.nombre",
                        Categoria.class)
                .getResultList();

        for (Categoria c : categorias) {
            System.out.println(
                    "  id=" + c.getId()
                            + " nombre=" + c.getNombre()
            );
        }
    }

    // ===== Institución =====

    private static void guardarInstitucion(
            EntityManager em,
            String nombre,
            String descripcion,
            String sitioWeb) {

        if (buscarInstitucion(em, nombre) != null) {
            System.out.println("  ya existía: " + nombre);
            return;
        }

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            em.persist(new Institucion(nombre, descripcion, sitioWeb));
            tx.commit();
            System.out.println("  guardada: " + nombre);
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    private static Institucion buscarInstitucion(EntityManager em, String nombre) {
        return em.createQuery(
                        "SELECT i FROM Institucion i WHERE i.nombre = :n",
                        Institucion.class)
                .setParameter("n", nombre)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private static void listarInstituciones(EntityManager em) {
        List<Institucion> instituciones = em.createQuery(
                        "SELECT i FROM Institucion i ORDER BY i.nombre",
                        Institucion.class)
                .getResultList();

        for (Institucion i : instituciones) {
            System.out.println(
                    "  id=" + i.getId()
                            + " nombre=" + i.getNombre()
                            + " sitio web=" + i.getSitioWeb()
            );
        }
    }

    private static void demoRollbackInstitucion(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            em.persist(new Institucion(
                    "UTEC",
                    "Institución repetida",
                    "https://utec.edu.uy"
            ));

            tx.commit();
            System.out.println("  INESPERADO: la BD aceptó una institución repetida.");

        } catch (Exception e) {
            tx.rollback();
            System.out.println("  rollback OK: la BD rechazó el nombre repetido.");
            System.out.println("  excepción: " + e.getClass().getSimpleName());
        }
    }
}
