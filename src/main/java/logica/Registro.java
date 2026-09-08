package logica;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Registro de un Asistente a una EdicionEvento.
 *
 * La restriccion (edicion_id, asistente_id) refleja en la base la regla de la
 * letra "un asistente no se puede registrar dos veces en la misma edicion",
 * que la logica ya valida con estaRegistrado().
 */
@Entity
@Table(name = "registro",
       uniqueConstraints = @UniqueConstraint(name = "uk_registro_edicion_asistente",
                                             columnNames = {"edicion_id", "asistente_id"}))
public class Registro extends EntidadBase {

    @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "asistente_id",
                foreignKey = @ForeignKey(name = "fk_registro_asistente"))
    private Asistente asistente;

    @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "edicion_id",
                foreignKey = @ForeignKey(name = "fk_registro_edicion"))
    private EdicionEvento edicion;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "tipo_registro_id",
                foreignKey = @ForeignKey(name = "fk_registro_tipo"))
    private TipoRegistro tipoRegistro;

    private Double costo;
    private LocalDate fechaRegistro;

    protected Registro() {}

    public Registro(Asistente asistente, EdicionEvento edicion, TipoRegistro tipoRegistro, Double costo, LocalDate fechaRegistro) {
        this.asistente = asistente;
        this.edicion = edicion;
        this.tipoRegistro = tipoRegistro;
        this.costo = costo;
        this.fechaRegistro = fechaRegistro;
    }

    public Asistente getAsistente() { return asistente; }
    public EdicionEvento getEdicion() { return edicion; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public Double getCosto() { return costo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }

    public void setTipoRegistro(TipoRegistro tipoRegistro) { this.tipoRegistro = tipoRegistro; }
    public void setCosto(Double costo) { this.costo = costo; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public DTRegistro obtenerDT() {
        return new DTRegistro(asistente.getNickname(), edicion.getNombre(), tipoRegistro.getNombre(), costo, fechaRegistro);
    }
}
