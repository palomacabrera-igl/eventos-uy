package logica;

public class DTAsistente extends DTUsuario {

    private String apellido;
    private DTFecha fechaNacimiento;

    public DTAsistente(String nickname, String nombre, String correo,
                       String apellido, DTFecha fechaNacimiento) {
        super(nickname, nombre, correo);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getApellido() {return apellido;}
    public DTFecha getFechaNacimiento() {return fechaNacimiento;}

}