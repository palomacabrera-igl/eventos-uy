package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "evento")
public class Evento extends EntidadBase {

    /** Nombre unico en la plataforma (letra). Es la clave de ManejadorEvento. */
    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(length = 20)
    private String sigla;

    @Column(length = 500)
    private String descripcion;

    private LocalDate fechaAlta;

    /** Lado INVERSO: la FK evento_id vive en la tabla edicion_evento. */
    @OneToMany(mappedBy = "evento", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EdicionEvento> ediciones = new ArrayList<>();

    /**
     * Muchos a muchos: un evento tiene varias categorias y una categoria puede
     * estar en varios eventos. JPA crea una TERCERA tabla intermedia
     * (evento_categoria) con las dos claves. Mismo patron que Libro N-N Autor
     * en la demo del profe.
     */
    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(name = "evento_categoria",
               joinColumns        = @JoinColumn(name = "evento_id"),
               inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private List<Categoria> categorias = new ArrayList<>();

    protected Evento() {}

    public Evento(String nombre, String descripcion, LocalDate fechaAlta, String sigla, List<Categoria> categorias) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
        this.sigla = sigla;
        this.categorias.addAll(categorias);
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getDescripcion() {return descripcion;}
    public LocalDate getFechaAlta() {return fechaAlta;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}

    /**
     * Agrega una edicion a este evento.
     */
    void agregarEdicion(EdicionEvento edicion) {
        ediciones.add(edicion);
        edicion.setEvento(this);
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
        List<String> nombresCategorias = new ArrayList<>();
        for (Categoria c : categorias) {
            nombresCategorias.add(c.getNombre());
        }
        return new DTEvento(nombre, sigla, descripcion, DTFecha.desde(fechaAlta), nombresCategorias);
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