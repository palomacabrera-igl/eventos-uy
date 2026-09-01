package logica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Usuario de tipo asistente. Ademas de los datos de {@link Usuario}, tiene
 * apellido y fecha de nacimiento (ver letra, seccion 4). Opcionalmente puede
 * estar asociado a una Institucion (ver caso de uso "Alta de Usuario":
 * la asociacion se hace despues de crear el Asistente, mediante
 * seleccionarInstitucion(), no en el alta).
 */
public class Asistente extends Usuario {

    private String apellido;
    private LocalDate fechaNacimiento;
    private Institucion institucion;
    private final List<Registro> registros;

    public Asistente(String nickname, String nombre, String correoElectronico,
                     String apellido, LocalDate fechaNacimiento) {
        super(nickname, nombre, correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.registros = new ArrayList<>();
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Institucion getInstitucion() {
        return institucion;
    }

    public void setInstitucion(Institucion institucion) {
        this.institucion = institucion;
    }

    @Override
    public DTUsuario obtenerDT() {
        return new DTAsistente(getNickname(), getNombre(), getCorreoElectronico(),
                apellido, DTFecha.desde(fechaNacimiento));
    }

    @Override
    public TipoUsuario obtenerTipoUsuario() {
        return TipoUsuario.ASISTENTE;
    }

    @Override
    public void modificarDatos(DTUsuario dt) {
        super.modificarDatos(dt);
        DTAsistente da = (DTAsistente) dt;
        this.apellido = da.getApellido();
        this.fechaNacimiento = da.getFechaNacimiento().aLocalDate();
    }

    public void agregarRegistro(Registro registro) {
        registros.add(registro);
    }

    public List<Registro> getRegistros() {
        return registros;
    }

    public DTRegistro darRegistro(String nombreEdicion) {
        for (Registro reg : registros) {
            if (reg.getEdicion().getNombre().equals(nombreEdicion)) {
                return reg.obtenerDT();
            }
        }
        return null;
    }

}