package logica;

import java.time.LocalDate;

public class EdicionEvento {
    private String nombre;
    private String sigla;
    private LocalDate fechaIni;
    private LocalDate fechaFin;
    private LocalDate fechaAlta;
    private String ciudad;
    private String pais;

    public EdicionEvento(String nombre, String sigla, LocalDate fechaIni,
                         LocalDate fechaFin, LocalDate fechaAlta, String ciudad,
                         String pais) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.fechaIni = fechaIni;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
        this.ciudad = ciudad;
        this.pais = pais;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public LocalDate getfechaIni() {return fechaIni;}
    public LocalDate getfechaFin() {return fechaFin;}
    public LocalDate getfechaAlta() {return fechaAlta;}
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setfechaIni(LocalDate fechaIni) {fechaIni = fechaIni;}
    public void setfechaFin(LocalDate fechaFin) {fechaFin = fechaFin;}
    public void setfechaAlta(LocalDate fechaAlta) {fechaAlta = fechaAlta;}
    public void setCiudad(String ciudad) {this.ciudad = ciudad;}
    public void setPais(String pais) {this.pais = pais;}
}
