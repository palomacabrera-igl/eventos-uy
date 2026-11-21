package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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

    /** Un evento finalizado no admite ediciones nuevas y no sale en los listados ni en la busqueda. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean finalizado;

    /** Organizador que dio de alta el evento; es quien puede finalizarlo. */
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Organizador creador;

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

    // ===== Rol 4: visitas =====

    /**
     * Cuantas veces se consulto el evento en el sitio (letra, 7.7). Sobrevive a
     * los reinicios porque esta en la base.
     *
     * Solo se cambia con ManejadorEvento.sumarVisita, que hace un UPDATE
     * atomico (visitas = visitas + 1). Por eso la columna es updatable = false:
     * cuando otra operacion modifica el evento (por ejemplo, lo finaliza),
     * Hibernate escribe todas las columnas con el valor que leyo, y si en el
     * medio entro una visita la pisaria. Sin esta columna en su UPDATE, no puede.
     *
     * El valor por defecto esta en la columna: con hbm2ddl.auto=update, una
     * columna NOT NULL nueva en una tabla con filas no se crea si la base no
     * sabe con que llenarla (ver README).
     */
    @Column(nullable = false, updatable = false, columnDefinition = "bigint default 0")
    private long visitas;

    protected Evento() {}

    public Evento(String nombre, String descripcion, LocalDate fechaAlta, String sigla, List<Categoria> categorias) {
        this(nombre, descripcion, fechaAlta, sigla, categorias, null);
    }

    public Evento(String nombre, String descripcion, LocalDate fechaAlta, String sigla,
                  List<Categoria> categorias, Organizador creador) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaAlta = fechaAlta;
        this.sigla = sigla;
        this.categorias.addAll(categorias);
        this.creador = creador;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getDescripcion() {return descripcion;}
    public LocalDate getFechaAlta() {return fechaAlta;}
    public Organizador getCreador() {return creador;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}

    public Imagen getImagen() {return imagen;}

    public long getVisitas() {return visitas;}

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
        DTEvento dt = new DTEvento(nombre, sigla, descripcion, DTFecha.desdeOpcional(fechaAlta),
                nombresCategorias, tieneImagen());
        dt.setVisitas(visitas);
        dt.setFinalizado(finalizado);
        return dt;
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

    // ===== Alta de Evento (CU 6) =====

    /**
     * Revisa los datos de un evento nuevo antes de crearlo: nombre,
     * descripcion y sigla son obligatorios, y la letra pide al menos una
     * categoria. Que el nombre no este repetido y que las categorias existan
     * lo controla Sistema, porque hay que buscar en la base.
     */
    public static void validarDatos(String nombre, String descripcion, String sigla,
                                    List<String> categorias) throws ReglaNegocioException {
        if (estaVacio(nombre) || estaVacio(descripcion) || estaVacio(sigla)) {
            throw new ReglaNegocioException(
                    "El nombre, la descripción y la sigla son obligatorios.");
        }
        if (categorias == null || categorias.stream().allMatch(Evento::estaVacio)) {
            throw new ReglaNegocioException(
                    "Hay que elegir al menos una categoría para el evento.");
        }
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // ===== Finalizar evento y busqueda (letra, 6.2 y 7.8) =====

    public boolean estaFinalizado() {return finalizado;}

    /** True solo para el organizador que lo dio de alta. */
    public boolean loCreo(String nickname) {
        return creador != null && creador.getNickname().equals(nickname);
    }

    /** Quien puede finalizarlo lo controla Sistema.finalizarEvento. */
    public void finalizar() {finalizado = true;}

    /** Las ediciones del evento, para que la busqueda las recorra. */
    List<EdicionEvento> getEdiciones() {return List.copyOf(ediciones);}
}
