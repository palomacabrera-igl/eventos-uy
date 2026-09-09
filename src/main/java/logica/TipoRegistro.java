package logica;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Tipo de registro de una EdicionEvento (entrada general, VIP, etc.).
 *
 * El nombre es unico DENTRO de su edicion, no en toda la plataforma: dos
 * ediciones distintas pueden tener cada una su "General". Por eso la
 * restriccion es sobre el par (edicion_id, nombre).
 */
@Entity
@Table(name = "tipo_registro",
       uniqueConstraints = @UniqueConstraint(name = "uk_tipo_registro_edicion_nombre",
                                             columnNames = {"edicion_id", "nombre"}))
public class TipoRegistro extends EntidadBase {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    private Double costo;
    private int cupo;

    /**
     * La EdicionEvento a la que pertenece este tipo de registro (referencia
     * inversa). La setea EdicionEvento.agregarTipoRegistro().
     */
    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "edicion_id",
                foreignKey = @ForeignKey(name = "fk_tipo_registro_edicion"))
    private EdicionEvento edicion;

    protected TipoRegistro() {}

    public TipoRegistro(String nombre, String descripcion, Double costo, int cupo){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.cupo = cupo;
    }

    public String getNombre() {return nombre;}
    public String getDescripcion() {return descripcion;}
    public Double getCosto() {return costo;}
    public int getCupo() {return cupo;}
    public EdicionEvento getEdicion() {return edicion;}

    /** Sin 'public': solo lo usa EdicionEvento, dentro del paquete logica. */
    void setEdicion(EdicionEvento edicion) {this.edicion = edicion;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setCosto(Double costo) {this.costo = costo;}
    public void setCupo(int cupo) {this.cupo = cupo;}

    public DTTipoRegistro obtenerDT() {
        return new DTTipoRegistro(nombre, descripcion, costo, cupo);
    }
}
