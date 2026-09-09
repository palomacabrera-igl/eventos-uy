package logica;

import persistencia.Persistencia;

import java.util.List;

/**
 * Manejador de la coleccion de Categoria (patron "collection object" de GRASP).
 *
 * Singleton. Responsabilidad: guardar, buscar y listar Categoria.
 */
public class ManejadorCategoria {

    private static ManejadorCategoria instancia = null;

    private ManejadorCategoria() {}

    public static ManejadorCategoria getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorCategoria();
        }
        return instancia;
    }

    /** Agrega una categoria. Asume que Sistema ya valido la unicidad del nombre. */
    public void agregar(Categoria categoria) {
        Persistencia.enTransaccion(em -> em.persist(categoria));
    }

    /** Devuelve la categoria con ese nombre, o null si no existe. */
    public Categoria buscar(String nombre) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT c FROM Categoria c WHERE c.nombre = :nombre", Categoria.class)
                .setParameter("nombre", nombre)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /** Todas las categorias de la coleccion. */
    public List<Categoria> listar() {
        return Persistencia.getEntityManager()
                .createQuery("SELECT c FROM Categoria c ORDER BY c.nombre", Categoria.class)
                .getResultList();
    }
}
