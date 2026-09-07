package logica;

import java.util.List;

public class DTEvento {

    private String nombre;
    private String sigla;
    private String descripcion;
    private DTFecha fechaAlta;
    private List<String> categorias;

    public DTEvento(String nombre, String sigla, String descripcion, DTFecha fechaAlta,
                    List<String> categorias) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
        this.categorias = categorias;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getDescripcion() {return descripcion;}
    public DTFecha getFechaAlta() {return fechaAlta;}
    public List<String> getCategorias() {return categorias;}
}