package logica;

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

    /** Para que la pagina pregunte sin tener que conocer el enum. */
    public boolean esAsistente() {return tipo == TipoUsuario.ASISTENTE;}
    public boolean esOrganizador() {return tipo == TipoUsuario.ORGANIZADOR;}

}