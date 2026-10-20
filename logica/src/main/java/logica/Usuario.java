package logica;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
/*
 * Herencia SINGLE_TABLE: Usuario, Asistente y Organizador comparten la tabla
 * 'usuario', y la columna 'tipo_usuario' distingue cada fila.
 *
 * Ventaja: no hay que unir tablas para leer un usuario.
 * Costo: las columnas propias de un subtipo aceptan NULL, porque en las
 * filas del otro subtipo van vacias.
 */
@Entity
@Table(name = "usuario",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_usuario_nickname", columnNames = "nickname"),
           @UniqueConstraint(name = "uk_usuario_correo",   columnNames = "correoElectronico")
       })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario",
                     discriminatorType = DiscriminatorType.STRING,
                     length = 20)
public abstract class Usuario extends EntidadBase {

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String correoElectronico;

    /**
     * Aca NO va la contrasena, va su huella con sal, en el formato que arma
     * la clase Contrasena. Son 76 caracteres. Sin nullable = false: las bases
     * que ya tienen filas no pueden recibir una columna NOT NULL sin valor
     * por defecto.
     */
    @Column(length = 100)
    private String password;

    /** Imagen opcional (letra, seccion 7.2). Queda null cuando no tiene. */
    @Embedded
    private Imagen imagen;

    protected Usuario() {}

    protected Usuario(String nickname, String nombre, String correoElectronico) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    protected Usuario(String nickname, String nombre, String correoElectronico, String password) {
        this(nickname, nombre, correoElectronico);
        cambiarPassword(password);
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

    public Imagen getImagen() {
        return imagen;
    }

    public boolean tieneImagen() {
        return imagen != null && imagen.getContenido() != null;
    }

    /**
     * La imagen se agrega o se reemplaza despues del alta, no en el
     * constructor: en los altas es opcional y en Modificar Perfil se cambia.
     */
    public void asignarImagen(Imagen imagen) {
        this.imagen = imagen;
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

    /**
     * Unico lugar donde se verifica la contrasena. Como lo guardado es una
     * huella y no un texto, comparar textos no alcanza: hay que recalcular la
     * huella de lo que se intento y comparar las dos.
     */
    public boolean verificarPassword(String intento) {
        return Contrasena.coincide(intento, password);
    }

    /**
     * Guarda la huella, nunca el texto. Al estar el calculo aca adentro, y al
     * no haber setter ni getter del campo, no hay forma de que alguien guarde
     * una contrasena sin hashear ni de leerla despues.
     */
    public void cambiarPassword(String nueva) {
        this.password = (nueva == null) ? null : Contrasena.hashear(nueva);
    }
}
