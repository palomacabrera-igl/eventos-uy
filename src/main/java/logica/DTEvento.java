package logica;

public class DTEvento {

    private String nombre;
    private String sigla;
    private String descripcion;
    private DTFecha fechaAlta;

    public DTEvento(String nombre, String sigla, String descripcion, DTFecha fechaAlta) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getDescripcion() {return descripcion;}
    public DTFecha getFechaAlta() {return fechaAlta;}
}