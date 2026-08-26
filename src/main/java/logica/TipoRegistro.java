package logica;

public class TipoRegistro {
    private String nombre;
    private String descripcion;
    private Double costo;
    private int cupo;

    public TipoRegistro(String nombre, String descripcion, Double costo, int cupo){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.cupo = cupo;
    }

    public String getNombre() {return nombre;}
    public String getDescripcion() {return descripcion;}
    public Double getCosto() {return costo;}
    public int getCupo() {return cupo;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setCosto(Double costo) {this.costo = costo;}
    public void setCupo(int cupo) {this.cupo = cupo;}
}
