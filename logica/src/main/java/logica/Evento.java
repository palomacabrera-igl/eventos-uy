package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

    /** Nombre unico en la plataforma. */
    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(length = 20)
    private String sigla;

    @Column(length = 500)
    private String descripcion;

    private LocalDate fechaAlta;

    /** Imagen opcional (letra, seccion 7.2). Queda null cuando no tiene. */
    @Embedded
    private Imagen imagen;

    /** Lado INVERSO: la FK evento_id vive en la tabla edicion_evento. */
    @OneToMany(mappedBy = "evento", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EdicionEvento> ediciones = new ArrayList<>();

    /**
     * Muchos a muchos: un evento tiene varias categorias y una categoria puede
     * estar en varios eventos. JPA crea una TERCERA tabla intermedia
     * (evento_categoria) con las dos claves.
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

    public Imagen getImagen() {return imagen;}

    public boolean tieneImagen() {
        return imagen != null && imagen.getContenido() != null;
    }

    /** La imagen se agrega o se reemplaza despues del alta, no en el constructor. */
    public void asignarImagen(Imagen imagen) {this.imagen = imagen;}

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
        return new DTEvento(nombre, sigla, descripcion, DTFecha.desde(fechaAlta),
                nombresCategorias, tieneImagen());
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

    // ===== Consulta de Evento (CU 7) =====

    /**
     * true si el evento esta en la categoria con ese nombre. Compara el nombre
     * exacto, igual que el filtro de la Parte 1: no incluye las subcategorias.
     */
    public boolean tieneCategoria(String nombreCategoria) {
        if (nombreCategoria == null) {
            return false;
        }
        for (Categoria c : categorias) {
            if (c.getNombre().equals(nombreCategoria)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Las ediciones que ve el publico: SOLO las Aceptada (letra, seccion 4).
     * Las Ingresada y Rechazada no salen de aca aunque existan.
     */
    /**
     * Las ediciones que todavia esperan que el administrador las acepte o las
     * rechace (Aceptar/Rechazar Edicion, Estacion de Trabajo).
     */
    public Set<DTEdicionEvento> obtenerEdicionesIngresadas() {
        Set<DTEdicionEvento> resultado = new HashSet<>();
        for (EdicionEvento ed : ediciones) {
            if (ed.getEstado() == EstadoEdicion.INGRESADA) {
                resultado.add(ed.obtenerDT());
            }
        }
        return resultado;
    }

    public Set<DTEdicionEvento> obtenerEdicionesAceptadas() {
        Set<DTEdicionEvento> resultado = new HashSet<>();
        for (EdicionEvento ed : ediciones) {
            if (ed.estaAceptada()) {
                resultado.add(ed.obtenerDT());
            }
        }
        return resultado;
    }
}