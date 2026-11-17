package logica;

import jakarta.persistence.LockModeType;
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

    // No hay un actualizar(evento): un evento que se LEYO con buscar() o listar()
    // queda conectado a la unidad de trabajo, y JPA guarda solo lo que se le
    // cambio cuando la operacion termina (ver persistencia.Persistencia).
    // Llamar a merge() de mas seria un error: devuelve una copia, y si otra
    // entidad sigue apuntando a la original, JPA inserta el mismo hijo dos veces.

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
    /**
     * El patrocinio que tiene ese codigo, en cualquier edicion, o null.
     *
     * El codigo es unico en toda la plataforma (letra, seccion 4), asi que no
     * alcanza con mirar la edicion que se esta patrocinando: hay que mirar
     * todas. Se hace con una consulta y no recorriendo los eventos en memoria
     * porque la base lo resuelve en un paso.
     */
    public Patrocinio buscarPatrocinioPorCodigo(String codigo) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT p FROM Patrocinio p WHERE p.codigo = :codigo",
                             Patrocinio.class)
                .setParameter("codigo", codigo)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public EdicionEvento buscarEdicion(String nombreEdicion) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT ed FROM EdicionEvento ed WHERE ed.nombre = :nombre",
                             EdicionEvento.class)
                .setParameter("nombre", nombreEdicion)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Igual que buscarEdicion(), pero TRABA la fila de la edicion en la base
     * hasta que termine la unidad de trabajo (el commit o el rollback).
     *
     * Es un bloqueo "pesimista": SELECT ... FOR UPDATE. Si otra operacion
     * quiere traer la misma edicion con este metodo mientras la primera no
     * termino, la base la hace ESPERAR. Cuando la primera hace commit, la
     * segunda sigue y lee los datos ya actualizados.
     *
     * Se usa al registrar un asistente. Antes, el candado global hacia que el
     * sitio atendiera las paginas de a una y eso evitaba que dos registros
     * simultaneos se pasaran del cupo. Sin ese candado, el cupo y los registros
     * gratuitos se protegen aca, solo para la edicion que se esta tocando, y
     * solo mientras dura la operacion.
     */
    public EdicionEvento buscarEdicionBloqueandola(String nombreEdicion) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT ed FROM EdicionEvento ed WHERE ed.nombre = :nombre",
                             EdicionEvento.class)
                .setParameter("nombre", nombreEdicion)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    // ===== Rol 4: visitas =====

    /**
     * Suma una visita al evento con UN solo UPDATE en la base:
     * visitas = visitas + 1. Devuelve cuantas filas cambio, 0 si no existe.
     *
     * No se lee el numero, se le suma uno y se guarda de nuevo: con dos visitas
     * a la vez las dos leerian el mismo valor y una se perderia. Con el UPDATE,
     * la base traba la fila: la segunda espera a que termine la primera y recien
     * ahi suma, sobre el valor ya actualizado.
     */
    public int sumarVisita(String nombreEvento) {
        return Persistencia.getEntityManager()
                .createQuery("UPDATE Evento e SET e.visitas = e.visitas + 1 WHERE e.nombre = :nombre")
                .setParameter("nombre", nombreEvento)
                .executeUpdate();
    }

    /**
     * Los eventos con mas visitas, del que mas al que menos y, si empatan, por
     * nombre. Hasta "cantidad". Un evento que nadie consulto no es de los mas
     * visitados: no entra.
     */
    public List<Evento> masVisitados(int cantidad) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT e FROM Evento e WHERE e.visitas > 0 "
                        + "ORDER BY e.visitas DESC, e.nombre ASC", Evento.class)
                .setMaxResults(cantidad)
                .getResultList();
    }
}
