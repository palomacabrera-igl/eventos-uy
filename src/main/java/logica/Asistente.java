package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Usuario de tipo asistente. Ademas de los datos de {@link Usuario}, tiene
 * apellido y fecha de nacimiento. Opcionalmente puede
 * estar asociado a una Institucion (ver caso de uso "Alta de Usuario":
 * la asociacion se hace despues de crear el Asistente, mediante
 * seleccionarInstitucion(), no en el alta).
 */
@Entity
@DiscriminatorValue("ASISTENTE")
public class Asistente extends Usuario {

    /**
     * Sin nullable = false A PROPOSITO: con SINGLE_TABLE esta columna vive en
     * la tabla 'usuario' compartida, y en las filas de Organizador va vacia.
     * Que el apellido no quede vacio lo valida la GUI, no la base.
     */
    @Column(length = 100)
    private String apellido;

    private LocalDate fechaNacimiento;

    /**
     * EAGER (el valor por defecto de @ManyToOne, explicito para que se lea).
     * Con LAZY, armar el DT fuera de la transaccion daria
     * LazyInitializationException.
     */
    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "institucion_id",
                foreignKey = @ForeignKey(name = "fk_asistente_institucion"))
    private Institucion institucion;

    /** Lado INVERSO: la FK asistente_id vive en la tabla registro. */
    @OneToMany(mappedBy = "asistente", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Registro> registros = new  ArrayList<>();

    protected Asistente() {}

    public Asistente(String nickname, String nombre, String correoElectronico,
                     String apellido, LocalDate fechaNacimiento) {
        super(nickname, nombre, correoElectronico);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
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
                apellido, DTFecha.desde(fechaNacimiento),
                institucion == null ? null : institucion.getNombre());
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