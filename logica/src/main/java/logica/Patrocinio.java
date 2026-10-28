package logica;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Patrocinio de una Institucion a una EdicionEvento.
 *
 * La restriccion (edicion_id, institucion_id) refleja en la base la regla de
 * la letra "una institucion no puede patrocinar dos veces la misma edicion",
 * que Sistema.altaPatrocinio() ya valida con tienePatrocinioDe().
 */
@Entity
@Table(name = "patrocinio",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_patrocinio_codigo",
                             columnNames = "codigo"),
           @UniqueConstraint(name = "uk_patrocinio_edicion_institucion",
                             columnNames = {"edicion_id", "institucion_id"})
       })
public class Patrocinio extends EntidadBase {

    private LocalDate fechaIni;
    private Double monto;
    private int cantRegistrosGratis;

    /** Codigo de patrocinio, unico en la plataforma. */
    @Column(nullable = false, length = 50)
    private String codigo;

    /**
     * EnumType.STRING guarda "ORO" / "PLATA" / "BRONCE" como texto.
     * Con el valor por defecto (ORDINAL) se guardaria 0, 1, 2.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", length = 20)
    private NivelPatrocinio nivelPatro;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "institucion_id",
                foreignKey = @ForeignKey(name = "fk_patrocinio_institucion"))
    private Institucion institucion;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "tipo_registro_id",
                foreignKey = @ForeignKey(name = "fk_patrocinio_tipo_registro"))
    private TipoRegistro tipoRegistro;

    /**
     * La EdicionEvento patrocinada (referencia inversa). La setea
     * EdicionEvento.agregarPatrocinio().
     */
    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "edicion_id",
                foreignKey = @ForeignKey(name = "fk_patrocinio_edicion"))
    private EdicionEvento edicion;

    protected Patrocinio() {}

    /**
     * El tope que pone la letra: el costo de los registros gratuitos no puede
     * pasar del 20% del aporte economico (seccion 4).
     *
     * Esta aca, y como metodo aparte, por dos motivos: es una regla del
     * patrocinio y no del caso de uso, y asi se puede probar sola, sin tener
     * que armar una edicion entera ni levantar la base.
     */
    public static final double PORCENTAJE_TOPE = 0.20;

    /** Lo maximo que puede valer lo que se regala, dado el aporte. */
    public static double topeDeGratis(double aporte) {
        return aporte * PORCENTAJE_TOPE;
    }

    /** Lo que vale lo que se regala: el costo del tipo por la cantidad. */
    public static double valorDeGratis(double costoDelTipo, int cantidad) {
        return costoDelTipo * cantidad;
    }

    /** true cuando lo regalado pasa el tope y el patrocinio hay que rechazarlo. */
    public static boolean superaElTope(double aporte, double costoDelTipo, int cantidad) {
        return valorDeGratis(costoDelTipo, cantidad) > topeDeGratis(aporte);
    }

    public Patrocinio(LocalDate fechaIni, Double monto, int cantRegistrosGratis,
                      String codigo, NivelPatrocinio nivelPatro,
                      Institucion institucion, TipoRegistro tipoRegistro){
        this.fechaIni = fechaIni;
        this.monto = monto;
        this.cantRegistrosGratis = cantRegistrosGratis;
        this.codigo = codigo;
        this.nivelPatro = nivelPatro;
        this.institucion = institucion;
        this.tipoRegistro = tipoRegistro;
    }

    public LocalDate getFechaIni() {return fechaIni;}
    public Double getMonto() {return monto;}
    public int getCantRegistrosGratis() {return cantRegistrosGratis;}
    public String getCodigo() {return codigo;}
    public NivelPatrocinio getNivelPatro() {return nivelPatro;}
    public Institucion getInstitucion() {return institucion;}
    public TipoRegistro getTipoRegistro() {return tipoRegistro;}
    public EdicionEvento getEdicion() {return edicion;}

    void setEdicion(EdicionEvento edicion) {this.edicion = edicion;}

    public void setFechaIni(LocalDate fechaIni) {this.fechaIni = fechaIni;}
    public void setMonto(Double monto) {this.monto = monto;}
    public void setCantRegistrosGratis(int cantRegistrosGratis) {this.cantRegistrosGratis = cantRegistrosGratis;}
    public void setCodigo(String codigo) {this.codigo = codigo;}
    public void setNivelPatro(NivelPatrocinio nivelPatro) {this.nivelPatro = nivelPatro;}

    /**
     * Arma el DTPatrocinio navegando hacia Institucion y TipoRegistro
     * asociados para obtener sus nombres.
     */
    public DTPatrocinio obtenerDT() {
        return new DTPatrocinio(codigo, DTFecha.desde(fechaIni), monto, nivelPatro.name(),
                cantRegistrosGratis, institucion.getNombre(), tipoRegistro.getNombre());
    }
}