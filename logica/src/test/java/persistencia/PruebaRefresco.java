package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import logica.Institucion;

/**
 * Comprueba que Persistencia.refrescar() sirve para que una aplicacion vea un
 * cambio que hizo la otra.
 *
 * El sitio web y la Estacion de Trabajo son dos programas distintos y cada uno
 * tiene su propio EntityManager. Un EntityManager se guarda en memoria una
 * copia de lo que leyo, asi que si la otra aplicacion cambia ese dato en la
 * base, el primero sigue contestando con su copia vieja.
 *
 * La prueba simula las dos aplicaciones con dos EntityManager: el de
 * Persistencia hace de sitio web, y uno aparte hace de Estacion de Trabajo.
 *
 * Para correrla, desde la carpeta del proyecto:
 *
 *   .\mvnw.cmd -pl logica exec:java "-Dexec.mainClass=persistencia.PruebaRefresco"
 */
public class PruebaRefresco {

    /** Nombre aparte, para no tocar los datos de prueba. */
    private static final String NOMBRE = "Institucion de prueba del refresco";

    /** Tiene que coincidir con el persistence-unit name del persistence.xml. */
    private static final String UNIDAD = "eventosuy";

    private static final String ORIGINAL = "descripcion original";
    private static final String CAMBIADA = "DESCRIPCION CAMBIADA POR LA OTRA APLICACION";

    public static void main(String[] args) {
        try {
            System.out.println("\n=== 1. Se crea una institucion para la prueba ===");
            borrar();
            crear();
            System.out.println("  creada: " + NOMBRE);

            System.out.println("\n=== 2. El sitio la lee ===");
            System.out.println("  descripcion: " + leerComoElSitio());

            System.out.println("\n=== 3. La Estacion de Trabajo la modifica ===");
            modificarDesdeLaOtraAplicacion();
            System.out.println("  cambiada y guardada en la base");

            System.out.println("\n=== 4. El sitio vuelve a leer, SIN refrescar ===");
            String vieja = leerComoElSitio();
            System.out.println("  descripcion: " + vieja);
            System.out.println(vieja.equals(ORIGINAL)
                    ? "  Ve el dato VIEJO: contesta con la copia que tiene en memoria."
                    : "  INESPERADO: ya veia el dato nuevo.");

            System.out.println("\n=== 5. El sitio refresca y vuelve a leer ===");
            Persistencia.refrescar();
            String nueva = leerComoElSitio();
            System.out.println("  descripcion: " + nueva);
            System.out.println(nueva.equals(CAMBIADA)
                    ? "  refresco OK: ahora si ve lo que hizo la otra aplicacion."
                    : "  MAL: sigue viendo el dato viejo.");

        } finally {
            borrar();
            Persistencia.cerrar();
            System.out.println("\nFin de la prueba.");
        }
    }

    private static void crear() {
        Persistencia.enTransaccion(em ->
                em.persist(new Institucion(NOMBRE, ORIGINAL, "https://prueba.uy")));
    }

    /** Lee con el EntityManager de la aplicacion, como haria un servlet. */
    private static String leerComoElSitio() {
        return buscar(Persistencia.getEntityManager()).getDescripcion();
    }

    /**
     * Abre su propio EntityManager, como lo tendria el otro programa, cambia
     * la descripcion y lo cierra.
     */
    private static void modificarDesdeLaOtraAplicacion() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(UNIDAD);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            buscar(em).setDescripcion(CAMBIADA);
            em.getTransaction().commit();
        } finally {
            em.close();
            emf.close();
        }
    }

    private static Institucion buscar(EntityManager em) {
        return em.createQuery(
                        "SELECT i FROM Institucion i WHERE i.nombre = :n",
                        Institucion.class)
                .setParameter("n", NOMBRE)
                .getSingleResult();
    }

    private static void borrar() {
        Persistencia.enTransaccion(em -> em.createQuery(
                        "DELETE FROM Institucion i WHERE i.nombre = :n")
                .setParameter("n", NOMBRE)
                .executeUpdate());
    }
}
