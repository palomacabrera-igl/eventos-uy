package logica;

public class DTInstitucion {
    private final String nombre;
    private final String descripcion;
    private final String sitioWeb;

    public DTInstitucion(String nombre, String descripcion, String sitioWeb) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getNombre() {return nombre;}
    public String getDescripcion() {return descripcion;}
    public String getSitioWeb() {return sitioWeb;}
}
