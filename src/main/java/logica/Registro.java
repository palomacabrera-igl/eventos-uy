package logica;

import java.time.LocalDate;

public class Registro {
    private Double costo;
    private LocalDate fechaRegistro;

    public Registro(Double costo, LocalDate fechaRegistro){
        this.costo = costo;
        this.LocalDate = fechaRegistro;
    }

    public Double getCosto() {return costo;}
    public LocalDate getFechaRegistro() {return fechaRegistro;}

    public void setCosto(Double costo) {this.costo = costo;}
    public void setFechaRegistro(LocalDate fechaRegistro) {this.fechaRegistro = fechaRegistro;}
}
