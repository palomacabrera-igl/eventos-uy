package logica;

/**
 * Datos de un patrocinio que cruzan entre la logica y la interfaz grafica.
 *
 * El nivel viaja como String y no como el enum NivelPatrocinio: la letra
 * (seccion 7.3) pide que "los objetos del dominio no deben viajar a la
 * interfaz grafica", y el enum es una clase del paquete logica. La conversion
 * entre el texto y el enum la hace Sistema, del lado de la logica.
 */
public class DTPatrocinio {

    private int codigoPatrocinio;
    private DTFecha fecha;
    private double monto;
    /** "PLATINO", "ORO", "PLATA" o "BRONCE". */
    private String nivel;
    private int cantRegistrosGratis;
    private String institucion;
    private String tipoRegistro;

    public DTPatrocinio(int codigoPatrocinio, DTFecha fecha, double monto, String nivel,
                        int cantRegistrosGratis, String institucion, String tipoRegistro) {
        this.codigoPatrocinio = codigoPatrocinio;
        this.fecha = fecha;
        this.monto = monto;
        this.nivel = nivel;
        this.cantRegistrosGratis = cantRegistrosGratis;
        this.institucion = institucion;
        this.tipoRegistro = tipoRegistro;
    }

    public int getCodigoPatrocinio() {return codigoPatrocinio;}
    public DTFecha getFecha() {return fecha;}
    public double getMonto() {return monto;}
    public String getNivel() {return nivel;}
    public int getCantRegistrosGratis() {return cantRegistrosGratis;}
    public String getInstitucion() {return institucion;}
    public String getTipoRegistro() {return tipoRegistro;}
}