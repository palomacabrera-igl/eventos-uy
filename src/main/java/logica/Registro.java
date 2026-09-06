package logica;

import java.time.LocalDate;

public class Registro {
    private Asistente asistente;
    private EdicionEvento edicion;
    private TipoRegistro tipoRegistro;
    private Double costo;
    private LocalDate fechaRegistro;

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
