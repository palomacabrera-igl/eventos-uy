package logica;

import persistencia.Persistencia;

import java.util.List;

/**
 * Manejador de la coleccion de Evento (patron "collection object" de GRASP).
 *
 * Singleton. Ademas de guardar/buscar/listar Evento, ofrece
 * buscarEdicion(nombre), porque las EdicionEvento no son una coleccion de
 * primer nivel: viven dentro de su Evento.
 *
 */
public class ManejadorEvento {

    private static ManejadorEvento instancia = null;

    private ManejadorEvento() {
    }

    public static ManejadorEvento getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorEvento();
        }
        return instancia;
    }

    /**
     * Da de alta un evento nuevo. Asume que Sistema ya valido la unicidad del
     * nombre.
     *
     * Al persistir el evento se persisten tambien sus ediciones, por el
     * cascade = PERSIST del @OneToMany. Las categorias NO se persisten aca:
     * ya existen, y lo unico que se escribe es la fila de la tabla intermedia
     * evento_categoria.
     */
    public void agregar(Evento evento) {
        Persistencia.enTransaccion(em -> em.persist(evento));
    }

    /**
     * Confirma en la base los cambios hechos sobre un evento que ya existe.
     *
     * No se usa merge() cuando la entidad ya esta managed: merge() devuelve una
     * copia, y si otra entidad sigue apuntando a la original, JPA inserta el
     * mismo hijo dos veces. Alcanza con abrir la transaccion: al hacer commit,
     * JPA detecta solo lo que cambio.
     */
    public void actualizar(Evento evento) {
        Persistencia.enTransaccion(em -> {
            if (!em.contains(evento)) {
                em.merge(evento);
            }
        });
    }

    public Evento buscar(String nombre) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT e FROM Evento e WHERE e.nombre = :nombre", Evento.class)
                .setParameter("nombre", nombre)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<Evento> listar() {
        return Persistencia.getEntityManager()
                .createQuery("SELECT e FROM Evento e ORDER BY e.nombre", Evento.class)
                .getResultList();
    }

    /**Busca una edicion por nombre. Devuelve null si no existe.*/
    public EdicionEvento buscarEdicion(String nombreEdicion) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT ed FROM EdicionEvento ed WHERE ed.nombre = :nombre",
                             EdicionEvento.class)
                .setParameter("nombre", nombreEdicion)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
