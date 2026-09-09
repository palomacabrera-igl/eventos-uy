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
 * MIGRADO A JPA. Antes tenia un Map en memoria; ahora la "coleccion" es la
 * tabla evento de PostgreSQL. Los metodos son los mismos de antes, asi que ni
 * Sistema ni las pantallas cambian.
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
     * Confirma en la base los cambios hechos sobre un evento que YA existe.
     *
     * Hace falta porque modificar un objeto no alcanza: JPA sincroniza con la
     * base recien al hacer commit de una transaccion. Sin esto, el cambio se
     * pierde en silencio.
     *
     * OJO con merge(): NO se usa cuando la entidad ya esta managed. merge()
     * devuelve una COPIA administrada, y si otra entidad seguia apuntando al
     * objeto original, JPA termina insertando el mismo hijo dos veces y la
     * base lo rechaza por nombre repetido. (Nos paso exactamente eso con una
     * edicion nueva, que cuelga a la vez del Evento y del Organizador.)
     *
     * Como la entidad vino de una consulta de nuestro unico EntityManager, ya
     * esta managed: alcanza con abrir y cerrar la transaccion, y al hacer
     * commit JPA detecta solo lo que cambio. El merge queda como red de
     * seguridad por si alguna vez llega desconectada.
     */
    public void actualizar(Evento evento) {
        Persistencia.enTransaccion(em -> {
            if (!em.contains(evento)) {
                em.merge(evento);
            }
        });
    }

    /** Devuelve el evento con ese nombre, o null si no existe. */
    public Evento buscar(String nombre) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT e FROM Evento e WHERE e.nombre = :nombre", Evento.class)
                .setParameter("nombre", nombre)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /** Todos los eventos, ordenados por nombre. */
    public List<Evento> listar() {
        return Persistencia.getEntityManager()
                .createQuery("SELECT e FROM Evento e ORDER BY e.nombre", Evento.class)
                .getResultList();
    }

    /**
     * Busca una edicion por nombre. Devuelve null si no existe.
     *
     * Antes este metodo recorria TODOS los eventos preguntandole a cada uno si
     * tenia una edicion con ese nombre. Ahora es una sola consulta directa
     * sobre la tabla edicion_evento, porque EdicionEvento paso a ser una
     * entidad con nombre unico en la plataforma.
     *
     * El metodo se queda en ManejadorEvento (y no se crea un manejador de
     * ediciones) porque una edicion sigue perteneciendo a su evento: no es una
     * coleccion de primer nivel del dominio.
     */
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
