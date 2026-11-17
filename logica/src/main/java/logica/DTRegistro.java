package logica;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "DTRegistro")
public class DTRegistro {
    private String nickname;
    private String nombreEdicion;
    private String tipoRegistro;
    private Double costo;
    private DTFecha fechaRegistro;
    /**
     * Si el registro se hizo con codigo de patrocinio. La letra lo pide en
     * Consulta de Registro: el organizador ve la fecha, el tipo, el costo y
     * si utilizo codigo. El codigo en si no se muestra aca.
     */
    private boolean usoCodigo;

    /**
     * Si el asistente ya registro su asistencia a la edicion. El organizador ve
     * quienes asistieron, y el asistente ve el dato en el detalle de su registro.
     */
    private boolean asistio;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTRegistro() {
    }

    public DTRegistro(String nickname, String nombreEdicion, String tipoRegistro, Double costo, DTFecha fechaRegistro) {
        this.nickname = nickname;
        this.nombreEdicion = nombreEdicion;
        this.tipoRegistro = tipoRegistro;
        this.costo = costo;
        this.fechaRegistro = fechaRegistro;
    }

    public DTRegistro(String nickname, String nombreEdicion, String tipoRegistro,
                      Double costo, DTFecha fechaRegistro, boolean usoCodigo) {
        this(nickname, nombreEdicion, tipoRegistro, costo, fechaRegistro);
        this.usoCodigo = usoCodigo;
    }

    public String getNickname() { return nickname; }
    public String getNombreEdicion() { return nombreEdicion; }
    public String getTipoRegistro() { return tipoRegistro; }
    public Double getCosto() { return costo; }
    public DTFecha getFechaRegistro() { return fechaRegistro; }
    public boolean getUsoCodigo() { return usoCodigo; }
    public boolean getAsistio() { return asistio; }

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setNombreEdicion(String nombreEdicion) { this.nombreEdicion = nombreEdicion; }
    public void setTipoRegistro(String tipoRegistro) { this.tipoRegistro = tipoRegistro; }
    public void setCosto(Double costo) { this.costo = costo; }
    public void setFechaRegistro(DTFecha fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setUsoCodigo(boolean usoCodigo) { this.usoCodigo = usoCodigo; }
    public void setAsistio(boolean asistio) { this.asistio = asistio; }
}
