package logica;

import persistencia.Persistencia;

import java.util.List;

/**
 * Manejador de la coleccion de Institucion (patron "collection object" de GRASP).
 *
 * Singleton. Responsabilidad: guardar, buscar y listar Institucion.
 */
public class ManejadorInstitucion {

    private static ManejadorInstitucion instancia = null;

    private ManejadorInstitucion() {}

    public static ManejadorInstitucion getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorInstitucion();
        }
        return instancia;
    }

    public void agregar(Institucion institucion) {
        Persistencia.enTransaccion(em -> em.persist(institucion));
    }

    public Institucion buscar(String nombre) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT i FROM Institucion i WHERE i.nombre = :nombre", Institucion.class)
                .setParameter("nombre", nombre)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<Institucion> listar() {
        return Persistencia.getEntityManager()
                .createQuery("SELECT i FROM Institucion i ORDER BY i.nombre", Institucion.class)
                .getResultList();
    }
}
