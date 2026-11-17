package logica;

import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlType(name = "DTEvento")
public class DTEvento {

    private String nombre;
    private String sigla;
    private String descripcion;
    private DTFecha fechaAlta;
    private List<String> categorias = new ArrayList<>();
    /** La pagina necesita saber si poner un <img> o el poster de color. */
    private boolean tieneImagen;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTEvento() {
    }

    public DTEvento(String nombre, String sigla, String descripcion, DTFecha fechaAlta,
                    List<String> categorias) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
        this.categorias = categorias;
    }
    public DTEvento(String nombre, String sigla, String descripcion, DTFecha fechaAlta,
                    List<String> categorias, boolean tieneImagen) {
        this(nombre, sigla, descripcion, fechaAlta, categorias);
        this.tieneImagen = tieneImagen;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getDescripcion() {return descripcion;}
    public DTFecha getFechaAlta() {return fechaAlta;}
    public List<String> getCategorias() {return categorias;}
    public boolean getTieneImagen() {return tieneImagen;}

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(DTFecha fechaAlta) {this.fechaAlta = fechaAlta;}
    public void setCategorias(List<String> categorias) {this.categorias = categorias;}
    public void setTieneImagen(boolean tieneImagen) {this.tieneImagen = tieneImagen;}
}
