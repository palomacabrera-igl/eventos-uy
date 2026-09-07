package logica;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador de la coleccion de Usuario (patron "collection object" de GRASP).
 *
 * Singleton: hay una unica coleccion de usuarios en todo el sistema. Sigue el
 * mismo patron que los manejadores de la demo ProyectoSwing del curso
 * (ManejadorUsuario alli): constructor privado, getInstancia(), y una
 * estructura interna indexada por el identificador de la entidad.
 *
 * Responsabilidad: guardar, buscar y listar Usuario. Las reglas de negocio
 * (por ejemplo "no repetir nickname ni correo") las valida Sistema ANTES de
 * llamar a agregar(), usando buscar()/buscarPorCorreo().
 */
public class ManejadorUsuario {

    private static ManejadorUsuario instancia = null;

    /** Usuarios indexados por nickname (su identificador). */
    private final Map<String, Usuario> usuariosPorNickname;

    private ManejadorUsuario() {
        this.usuariosPorNickname = new LinkedHashMap<>();
    }

    public static ManejadorUsuario getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorUsuario();
        }
        return instancia;
    }

    /** Agrega un usuario. Asume que Sistema ya valido la unicidad. */
    public void agregar(Usuario usuario) {
        usuariosPorNickname.put(usuario.getNickname(), usuario);
    }

    /** Devuelve el usuario con ese nickname, o null si no existe. */
    public Usuario buscar(String nickname) {
        return usuariosPorNickname.get(nickname);
    }

    /** Devuelve el usuario con ese correo, o null si ninguno lo tiene. */
    public Usuario buscarPorCorreo(String correo) {
        for (Usuario u : usuariosPorNickname.values()) {
            if (u.getCorreoElectronico().equals(correo)) {
                return u;
            }
        }
        return null;
    }

    /** Todos los usuarios de la coleccion. */
    public List<Usuario> listar() {
        return new ArrayList<>(usuariosPorNickname.values());
    }
}
