package logica;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "DTInstitucion")
public class DTInstitucion {
    private String nombre;
    private String descripcion;
    private String sitioWeb;
    /** La pagina necesita saber si poner un <img> o no. Los bytes no viajan aca. */
    private boolean tieneImagen;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTInstitucion() {
    }

    /**
     * El constructor para quien no tiene imagen que informar.
     * Le pasa false al de abajo: la firma no cambia, asi que quien ya lo
     * llamaba con tres argumentos lo sigue llamando igual.
     */
    public DTInstitucion(String nombre, String descripcion, String sitioWeb) {
        this(nombre, descripcion, sitioWeb, false);
    }

    public DTInstitucion(String nombre, String descripcion, String sitioWeb, boolean tieneImagen) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
        this.tieneImagen = tieneImagen;
    }

    public String getNombre() {return nombre;}
    public String getDescripcion() {return descripcion;}
    public String getSitioWeb() {return sitioWeb;}
    public boolean getTieneImagen() {return tieneImagen;}

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
    public void setTieneImagen(boolean tieneImagen) {this.tieneImagen = tieneImagen;}
}
