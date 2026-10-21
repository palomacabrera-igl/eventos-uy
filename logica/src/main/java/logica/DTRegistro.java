package logica;

import java.time.LocalDate;

public class DTRegistro {
    private String nickname;
    private String nombreEdicion;
    private String tipoRegistro;
    private Double costo;
    private LocalDate fechaRegistro;
    /**
     * Si el registro se hizo con codigo de patrocinio. La letra lo pide en
     * Consulta de Registro: el organizador ve la fecha, el tipo, el costo y
     * si utilizo codigo. El codigo en si no se muestra aca.
     */
    private boolean usoCodigo;

    public DTRegistro(String nickname, String nombreEdicion, String tipoRegistro, Double costo, LocalDate fechaRegistro) {
        this.nickname = nickname;
        this.nombreEdicion = nombreEdicion;
        this.tipoRegistro = tipoRegistro;
        this.costo = costo;
        this.fechaRegistro = fechaRegistro;
    }

    public DTRegistro(String nickname, String nombreEdicion, String tipoRegistro,
                      Double costo, LocalDate fechaRegistro, boolean usoCodigo) {
        this(nickname, nombreEdicion, tipoRegistro, costo, fechaRegistro);
        this.usoCodigo = usoCodigo;
    }

    public String getNickname() { return nickname; }
    public String getNombreEdicion() { return nombreEdicion; }
    public String getTipoRegistro() { return tipoRegistro; }
    public Double getCosto() { return costo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public boolean getUsoCodigo() { return usoCodigo; }
}