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

    public DTAsistente(String nickname, String nombre, String correo,
                       String apellido, DTFecha fechaNacimiento, String institucion) {
        super(nickname, nombre, correo);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.institucion = institucion;
    }

    public String getApellido() {return apellido;}
    public DTFecha getFechaNacimiento() {return fechaNacimiento;}

    /** Nombre de la institucion, o null si el asistente no pertenece a ninguna. */
    public String getInstitucion() {return institucion;}

}