package logica;

/**
 * Entidad de dominio abstracta que representa un usuario de la plataforma.
 * No se instancia directamente: todo usuario es o bien un {@link Asistente}
 * o bien un {@link Organizador} (ver letra, seccion 4 - Vision).
 *
 * El nickname y el correo electronico son unicos en la plataforma y no se
 * pueden modificar una vez creado el usuario (ver caso de uso
 * "Modificar Datos de Usuario": el administrador puede editar el resto de
 * los datos, pero no estos dos).
 */
public abstract class Usuario {

    private final String nickname;
    private String nombre;
    private final String correoElectronico;

    protected Usuario(String nickname, String nombre, String correoElectronico) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    /**
     * Polimorfico: cada subtipo concreto arma su propio DT (DTAsistente o
     * DTOrganizador), que extiende a DTUsuario con sus campos propios.
     */
    public abstract DTUsuario obtenerDT();

    /**
     * Polimorfico: cada subtipo concreto dice quien es. Lo usa
     * listarOrganizadores() para filtrar la coleccion de Usuario.
     */
    public abstract TipoUsuario obtenerTipoUsuario();

    /**
     * Modifica el nombre (comun a todo Usuario). Las subclases sobreescriben,
     * llaman a super.modificarDatos(dt) y ademas actualizan sus campos
     * propios, casteando dt al DT concreto correspondiente. nickname y
     * correoElectronico nunca se modifican (son final).
     */
    public void modificarDatos(DTUsuario dt) {
        this.nombre = dt.getNombre();
    }
}