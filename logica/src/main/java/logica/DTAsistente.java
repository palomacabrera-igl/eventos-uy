package logica;

public class DTAsistente extends DTUsuario {

    private String apellido;
    private DTFecha fechaNacimiento;

    /**
     * Nombre de la institucion a la que pertenece, o null si no pertenece a
     * ninguna: la asociacion es opcional.
     *
     * Viaja el NOMBRE y no el objeto Institucion, porque se pide
     * que los objetos del dominio no crucen a la interfaz grafica.
     */
    private String institucion;

    /**
     * El tipo no es un parametro: un DTAsistente no puede ser otra cosa que
     * un asistente, asi que se lo declara el solo. La firma no cambia.
     */
    public DTAsistente(String nickname, String nombre, String correo,
                       String apellido, DTFecha fechaNacimiento, String institucion) {
        super(nickname, nombre, correo, TipoUsuario.ASISTENTE, false);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.institucion = institucion;
    }

    /** Para un asistente que ya existe y del que se sabe si tiene imagen. */
    public DTAsistente(String nickname, String nombre, String correo,
                       String apellido, DTFecha fechaNacimiento, String institucion,
                       boolean tieneImagen) {
        super(nickname, nombre, correo, TipoUsuario.ASISTENTE, tieneImagen);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.institucion = institucion;
    }

    public String getApellido() {return apellido;}
    public DTFecha getFechaNacimiento() {return fechaNacimiento;}

    /** Nombre de la institucion, o null si el asistente no pertenece a ninguna. */
    public String getInstitucion() {return institucion;}

}