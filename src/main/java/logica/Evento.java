package logica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evento {
    private String nombre;
    private String sigla;
    private String descripcion;
    private LocalDate fechaAlta;
    private final List<EdicionEvento> ediciones;
    private final List<Categoria> categorias;

    public Evento(String nombre, String descripcion, LocalDate fechaAlta, String sigla, List<Categoria> categorias) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
        this.sigla = sigla;
        this.ediciones = new ArrayList<>();
        this.categorias = new ArrayList<>(categorias);
    }


    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getDescripcion() {return descripcion;}
    public LocalDate getFechaAlta() {return fechaAlta;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}

    /** Paquete-visible: la usa Sistema para cargar datos de prueba (y, mas
     * adelante, Alta de Edicion de Evento). No es parte del contrato de la GUI. */
    void agregarEdicion(EdicionEvento edicion) {
        ediciones.add(edicion);
    }

    public EdicionEvento altaEdicion(DTEdicionEvento dt, Organizador organizador) {
        EdicionEvento nuevaEdicion = new EdicionEvento(dt.getNombre(), dt.getSigla(),
                dt.getFechaInicio().aLocalDate(), dt.getFechaFin().aLocalDate(),
                dt.getFechaAlta().aLocalDate(), dt.getCiudad(), dt.getPais(), organizador);
        agregarEdicion(nuevaEdicion);
        organizador.agregarEdicion(nuevaEdicion);
        return nuevaEdicion;
    }

    public DTEvento obtenerDT() {
        return new DTEvento(nombre, sigla, descripcion, DTFecha.desde(fechaAlta));
    }

    public Set<DTEdicionEvento> obtenerEdiciones() {
        Set<DTEdicionEvento> resultado = new HashSet<>();
        for (EdicionEvento ed : ediciones) {
            resultado.add(ed.obtenerDT());
        }
        return resultado;
    }

    public EdicionEvento buscarEdicion(String nombre) {
        for (EdicionEvento ed : ediciones) {
            if (ed.getNombre().equals(nombre)) {
                return ed;
            }
        }
        return null;
    }
}