package logica;

public class DTTipoRegistro {
    private String nombre;
    private String descripcion;
    private Double costo;
    private int cupo;

    public DTTipoRegistro(String nombre, String descripcion, Double costo, int cupo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.cupo = cupo;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Double getCosto() { return costo; }
    public int getCupo() { return cupo; }
}