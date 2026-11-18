package logica;

import jakarta.persistence.Column;
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

    /**
     * Patrocinio que pago este registro, o null si el asistente lo pago.
     * Guardarlo permite contar cuantos registros gratuitos lleva usados cada
     * patrocinio y explicar por que este registro costo cero.
     */
    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "patrocinio_id",
                foreignKey = @ForeignKey(name = "fk_registro_patrocinio"))
    private Patrocinio patrocinio;

    private Double costo;
    private LocalDate fechaRegistro;

    /**
     * Si el asistente indico que asistio a la edicion (letra, seccion 6.3,
     * Registro de Asistencia). Arranca en false y pasa a true una sola vez.
     *
     * El valor por defecto esta en la columna y no solo en Java: con
     * hbm2ddl.auto=update, una columna NOT NULL nueva en una tabla que ya tiene
     * filas no se crea si la base no sabe con que llenarla (ver README).
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean asistio;

    protected Registro() {}

    public Registro(Asistente asistente, EdicionEvento edicion, TipoRegistro tipoRegistro, Double costo, LocalDate fechaRegistro) {
        this(asistente, edicion, tipoRegistro, costo, fechaRegistro, null);
    }

    public Registro(Asistente asistente, EdicionEvento edicion, TipoRegistro tipoRegistro,
                    Double costo, LocalDate fechaRegistro, Patrocinio patrocinio) {
        this.asistente = asistente;
        this.edicion = edicion;
        this.tipoRegistro = tipoRegistro;
        this.costo = costo;
        this.fechaRegistro = fechaRegistro;
        this.patrocinio = patrocinio;
    }

    public Asistente getAsistente() { return asistente; }
    public EdicionEvento getEdicion() { return edicion; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public Double getCosto() { return costo; }

    public Patrocinio getPatrocinio() { return patrocinio; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }

    public void setTipoRegistro(TipoRegistro tipoRegistro) { this.tipoRegistro = tipoRegistro; }
    public void setCosto(Double costo) { this.costo = costo; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean getAsistio() { return asistio; }

    /**
     * Anota que el asistente asistio. Solo se puede una vez: la letra pide que
     * el sistema indique que ya estaba registrada y no ofrezca registrarla otra vez.
     */
    public void registrarAsistencia() throws ReglaNegocioException {
        if (asistio) {
            throw new ReglaNegocioException("Ya registraste tu asistencia a la edición \""
                    + edicion.getNombre() + "\".");
        }
        asistio = true;
    }

    public DTRegistro obtenerDT() {
        DTRegistro dt = new DTRegistro(asistente.getNickname(), edicion.getNombre(),
                tipoRegistro.getNombre(), costo, DTFecha.desdeOpcional(fechaRegistro),
                patrocinio != null);
        dt.setAsistio(asistio);
        return dt;
    }
}
