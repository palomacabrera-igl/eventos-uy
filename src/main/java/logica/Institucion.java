package logica;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "institucion")
public class Institucion extends EntidadBase {

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "sitio_web", nullable = false, length = 255)
    private String sitioWeb;

    protected Institucion() {}

    public Institucion(String nombre, String descripcion, String sitioWeb) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getSitioWeb() { return sitioWeb; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }
}
