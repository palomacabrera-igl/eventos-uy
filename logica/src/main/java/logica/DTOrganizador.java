package logica;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "DTOrganizador")
public class DTOrganizador extends DTUsuario {

    private String descripcion;
    private String sitioWeb;

    /**
     * Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su
     * setter. Ya queda con el tipo de organizador, igual que los otros constructores.
     */
    public DTOrganizador() {
        super(null, null, null, TipoUsuario.ORGANIZADOR, false);
    }

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

    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}

}
