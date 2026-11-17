package logica;

import persistencia.Persistencia;

import java.util.List;

/**
 * Manejador de la coleccion de Usuario (patron "collection object" de GRASP).
 *
 * Singleton. Responsabilidad: guardar, buscar y listar Usuario.
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


    public void agregar(Usuario usuario) {
        Persistencia.enTransaccion(em -> em.persist(usuario));
    }

    // No hay un actualizar(usuario): un usuario que se LEYO con buscar() o
    // listar() queda conectado a la unidad de trabajo, y JPA guarda solo lo que
    // se le cambio cuando la operacion termina (ver persistencia.Persistencia).

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
     * Es una consulta polimorfica: al consultar Usuario vienen asistentes y
     * organizadores, y Hibernate arma la subclase de cada fila segun la
     * columna tipo_usuario.
     */
    public List<Usuario> listar() {
        return Persistencia.getEntityManager()
                .createQuery("SELECT u FROM Usuario u ORDER BY u.nickname", Usuario.class)
                .getResultList();
    }
}
