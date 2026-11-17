package logica;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "DTTipoRegistro")
public class DTTipoRegistro {
    private String nombre;
    private String descripcion;
    private Double costo;
    private int cupo;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTTipoRegistro() {
    }

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

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCosto(Double costo) { this.costo = costo; }
    public void setCupo(int cupo) { this.cupo = cupo; }
}
