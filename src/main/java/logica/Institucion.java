package logica;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Institucion a la que puede pertenecer un Asistente y que puede patrocinar
 * ediciones de eventos.
 *
 * Sin asociaciones propias: las dos relaciones en las que participa las
 * poseen del otro lado (Asistente.institucion y Patrocinio.institucion).
 */
@Entity
@Table(name = "institucion")
public class Institucion extends EntidadBase {

    /** Nombre unico: lo usa ManejadorInstitucion como clave. */
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 300)
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
