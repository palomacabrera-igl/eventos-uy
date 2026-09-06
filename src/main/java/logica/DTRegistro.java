package logica;

import java.time.LocalDate;

public class DTRegistro {
    private String nickname;
    private String nombreEdicion;
    private String tipoRegistro;
    private Double costo;
    private LocalDate fechaRegistro;

    public DTRegistro(String nickname, String nombreEdicion, String tipoRegistro, Double costo, LocalDate fechaRegistro) {
        this.nickname = nickname;
        this.nombreEdicion = nombreEdicion;
        this.tipoRegistro = tipoRegistro;
        this.costo = costo;
        this.fechaRegistro = fechaRegistro;
    }

    public String getNickname() { return nickname; }
    public String getNombreEdicion() { return nombreEdicion; }
    public String getTipoRegistro() { return tipoRegistro; }
    public Double getCosto() { return costo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
}