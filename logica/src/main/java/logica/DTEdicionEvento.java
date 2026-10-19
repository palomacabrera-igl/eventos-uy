package logica;

public class DTEdicionEvento {

    private String nombre;
    private String sigla;
    private String ciudad;
    private String pais;
    private DTFecha fechaInicio;
    private DTFecha fechaFin;
    private DTFecha fechaAlta;

    public DTEdicionEvento(String nombre, String sigla, String ciudad, String pais,
                           DTFecha fechaInicio, DTFecha fechaFin, DTFecha fechaAlta) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.ciudad = ciudad;
        this.pais = pais;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}
    public DTFecha getFechaInicio() {return fechaInicio;}
    public DTFecha getFechaFin() {return fechaFin;}
    public DTFecha getFechaAlta() {return fechaAlta;}
}