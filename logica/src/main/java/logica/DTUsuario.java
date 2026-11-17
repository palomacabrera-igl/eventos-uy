package logica;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "DTUsuario")
public class DTUsuario {

    private String nickname;
    private String nombre;
    private String correo;

    /**
     * Asistente u organizador. Hasta ahora esto se averiguaba preguntando de
     * que clase era el objeto (instanceof DTAsistente). En la web no sirve:
     * el cabezal recibe un DTUsuario desde la sesion y tiene que decidir que
     * menu mostrar sin saber como fue construido.
     */
    private TipoUsuario tipo;

    /** Si hay que dibujar un <img> o el circulo con la inicial. */
    private boolean tieneImagen;

    /** null si el usuario se guardo antes de que existiera la fecha de alta. */
    private DTFecha fechaAlta;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTUsuario() {
    }

    public DTUsuario(String nickname, String nombre, String correo) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.correo = correo;
    }

    /**
     * protected: lo usan DTAsistente y DTOrganizador, que son los unicos que
     * saben su propio tipo. Nadie de afuera arma un DTUsuario "suelto" con un
     * tipo elegido a mano.
     */
    protected DTUsuario(String nickname, String nombre, String correo,
                        TipoUsuario tipo, boolean tieneImagen) {
        this(nickname, nombre, correo);
        this.tipo = tipo;
        this.tieneImagen = tieneImagen;
    }

    public String getNickname() {return nickname;}
    public String getNombre() {return nombre;}
    public String getCorreo() {return correo;}
    public TipoUsuario getTipo() {return tipo;}
    public boolean getTieneImagen() {return tieneImagen;}
    public DTFecha getFechaAlta() {return fechaAlta;}

    /*
     * Los setters existen para JAXB: es como reconstruye el objeto cuando llega
     * por un servicio web. No hace falta llamarlos a mano.
     */
    public void setNickname(String nickname) {this.nickname = nickname;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setCorreo(String correo) {this.correo = correo;}
    public void setTipo(TipoUsuario tipo) {this.tipo = tipo;}
    public void setTieneImagen(boolean tieneImagen) {this.tieneImagen = tieneImagen;}

    /**
     * Antes era de paquete: solo la completaba la entidad al armar su DT. Ahora
     * es publico porque JAXB lo necesita. Los que llegan desde un formulario de
     * alta siguen sin traerla, porque la pone el sistema.
     */
    public void setFechaAlta(DTFecha fechaAlta) {this.fechaAlta = fechaAlta;}

    /** Para que la pagina pregunte sin tener que conocer el enum. */
    public boolean esAsistente() {return tipo == TipoUsuario.ASISTENTE;}
    public boolean esOrganizador() {return tipo == TipoUsuario.ORGANIZADOR;}

}
