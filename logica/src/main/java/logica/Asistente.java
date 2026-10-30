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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public Asistente(String nickname, String nombre, String correoElectronico,
                     String apellido, LocalDate fechaNacimiento, String password) {
        super(nickname, nombre, correoElectronico, password);
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Una persona no puede haber nacido despues de hoy. Una fecha de
     * nacimiento futura no describe a nadie, y hasta ahora el unico
     * control era que no viniera vacia.
     *
     * Esta aca, y como metodo aparte, por los mismos motivos que el tope
     * del patrocinio: es una regla del asistente y no del caso de uso, y
     * asi se puede probar sola, pasandole el "hoy" que uno quiera, sin
     * depender de la fecha de la maquina ni de la base.
     *
     * Nacer hoy si vale: el limite es el dia de hoy, incluido.
     */
    public static boolean fechaDeNacimientoEnElFuturo(LocalDate fechaNacimiento,
                                                      LocalDate hoy) {
        return fechaNacimiento != null && hoy != null && fechaNacimiento.isAfter(hoy);
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
                institucion == null ? null : institucion.getNombre(),
                tieneImagen());
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

    /**
     * Los registros de este asistente, segun quien los este mirando (CU 4).
     *
     * A diferencia de las ediciones de un organizador, aca no se muestra nada
     * a nadie mas: a que eventos se anoto alguien es asunto suyo. Se devuelve
     * el conjunto vacio y no un error, porque el perfil SI se puede mirar; lo
     * unico que no se ve es esta parte.
     */
    public Set<DTRegistro> obtenerRegistrosPara(String solicitante) {
        Set<DTRegistro> resultado = new LinkedHashSet<>();
        if (!esSuPropioPerfil(solicitante)) {
            return resultado;
        }
        for (Registro reg : registros) {
            resultado.add(reg.obtenerDT());
        }
        return resultado;
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