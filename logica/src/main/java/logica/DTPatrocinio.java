package logica;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "DTPatrocinio")
public class DTPatrocinio {

    private String codigoPatrocinio;
    private DTFecha fecha;
    private double monto;
    /** "PLATINO", "ORO", "PLATA" o "BRONCE". */
    private String nivel;
    private int cantRegistrosGratis;
    private String institucion;
    private String tipoRegistro;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTPatrocinio() {
    }

    public DTPatrocinio(String codigoPatrocinio, DTFecha fecha, double monto, String nivel,
                        int cantRegistrosGratis, String institucion, String tipoRegistro) {
        this.codigoPatrocinio = codigoPatrocinio;
        this.fecha = fecha;
        this.monto = monto;
        this.nivel = nivel;
        this.cantRegistrosGratis = cantRegistrosGratis;
        this.institucion = institucion;
        this.tipoRegistro = tipoRegistro;
    }

    public String getCodigoPatrocinio() {return codigoPatrocinio;}
    public DTFecha getFecha() {return fecha;}
    public double getMonto() {return monto;}
    public String getNivel() {return nivel;}
    public int getCantRegistrosGratis() {return cantRegistrosGratis;}
    public String getInstitucion() {return institucion;}
    public String getTipoRegistro() {return tipoRegistro;}

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setCodigoPatrocinio(String codigoPatrocinio) {this.codigoPatrocinio = codigoPatrocinio;}
    public void setFecha(DTFecha fecha) {this.fecha = fecha;}
    public void setMonto(double monto) {this.monto = monto;}
    public void setNivel(String nivel) {this.nivel = nivel;}
    public void setCantRegistrosGratis(int cantRegistrosGratis) {this.cantRegistrosGratis = cantRegistrosGratis;}
    public void setInstitucion(String institucion) {this.institucion = institucion;}
    public void setTipoRegistro(String tipoRegistro) {this.tipoRegistro = tipoRegistro;}
}
