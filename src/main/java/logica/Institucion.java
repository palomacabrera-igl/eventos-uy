package logica;

public class Institucion {
    private String nombre;
    private String descripcion;
    private String sitioWeb;

    protected Institucion() {}

    public Institucion(String nombre, String descripcion, String sitioWeb){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getNombre() {return nombre;}
    public String getDescripcion() {return descripcion;}
    public String getSitioWeb() {return sitioWeb;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setSitioWeb(String sitioWeb) {this.sitioWeb = sitioWeb;}
}
