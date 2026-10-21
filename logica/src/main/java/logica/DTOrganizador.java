package logica;

public class DTOrganizador extends DTUsuario {

    private String descripcion;
    private String sitioWeb;

    /**
     * El tipo no es un parametro: un DTOrganizador no puede ser otra cosa.
     * La firma no cambia.
     */
    public DTOrganizador(String nickname, String nombre, String correo,
                         String descripcion, String sitioWeb) {
        super(nickname, nombre, correo, TipoUsuario.ORGANIZADOR, false);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    /** Para un organizador que ya existe y del que se sabe si tiene imagen. */
    public DTOrganizador(String nickname, String nombre, String correo,
                         String descripcion, String sitioWeb, boolean tieneImagen) {
        super(nickname, nombre, correo, TipoUsuario.ORGANIZADOR, tieneImagen);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getDescripcion() {return descripcion;}
    public String getSitioWeb() {return sitioWeb;}

}