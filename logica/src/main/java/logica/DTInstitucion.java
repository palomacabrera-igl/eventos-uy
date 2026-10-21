package logica;

public class DTInstitucion {
    private final String nombre;
    private final String descripcion;
    private final String sitioWeb;
    /** La pagina necesita saber si poner un <img> o no. Los bytes no viajan aca. */
    private final boolean tieneImagen;

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
}
