package logica;

import persistencia.Persistencia;

import java.util.List;

/**
 * Manejador de la coleccion de Usuario (patron "collection object" de GRASP).
 *
 * Singleton. Responsabilidad: guardar, buscar y listar Usuario.
 *
 * MIGRADO A JPA. Antes tenia un Map en memoria; ahora la "coleccion" es la
 * tabla usuario de PostgreSQL y este manejador es el unico lugar de la logica
 * que sabe como consultarla. Los metodos son los mismos de antes, asi que ni
 * Sistema ni las pantallas cambian: esa fue toda la razon de separar los
 * Manejadores antes de meter JPA.
 *
 * Nota de capas: logica usa persistencia, nunca al reves. La letra (7.1) lo
 * permite explicitamente: "el acceso a la base se realiza desde la capa de
 * logica o de persistencia, nunca desde la interfaz grafica".
 */
public class ManejadorUsuario {

    private static ManejadorUsuario instancia = null;

    private ManejadorUsuario() {
    }

    public static ManejadorUsuario getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorUsuario();
        }
        return instancia;
    }

    /**
     * Da de alta un usuario nuevo. Asume que Sistema ya valido la unicidad de
     * nickname y correo (ademas de las restricciones UNIQUE de la tabla).
     */
    public void agregar(Usuario usuario) {
        Persistencia.enTransaccion(em -> em.persist(usuario));
    }

    /**
     * Confirma en la base los cambios hechos sobre un usuario que YA existe.
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
    public void actualizar(Usuario usuario) {
        Persistencia.enTransaccion(em -> {
            if (!em.contains(usuario)) {
                em.merge(usuario);
            }
        });
    }

    /**
     * Devuelve el usuario con ese nickname, o null si no existe.
     *
     * Se usa JPQL y no em.find() porque find() busca por CLAVE PRIMARIA, y
     * nuestra clave primaria es el id numerico que genera la base, no el
     * nickname. El nickname es unico, pero no es la primary key.
     */
    public Usuario buscar(String nickname) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT u FROM Usuario u WHERE u.nickname = :nickname", Usuario.class)
                .setParameter("nickname", nickname)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /** Devuelve el usuario con ese correo, o null si ninguno lo tiene. */
    public Usuario buscarPorCorreo(String correo) {
        return Persistencia.getEntityManager()
                .createQuery("SELECT u FROM Usuario u WHERE u.correoElectronico = :correo", Usuario.class)
                .setParameter("correo", correo)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Todos los usuarios, ordenados por nickname.
     *
     * Consultar Usuario (la clase abstracta) trae asistentes Y organizadores
     * mezclados: es una consulta polimorfica. Hibernate mira la columna
     * tipo_usuario de cada fila y arma la subclase que corresponde, asi que el
     * instanceof de Sistema y de las pantallas sigue funcionando igual.
     */
    public List<Usuario> listar() {
        return Persistencia.getEntityManager()
                .createQuery("SELECT u FROM Usuario u ORDER BY u.nickname", Usuario.class)
                .getResultList();
    }
}
