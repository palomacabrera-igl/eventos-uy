package logica;

public class DTPatrocinio {

    private int codigoPatrocinio;
    private DTFecha fecha;
    private double monto;
    private NivelPatrocinio nivel;
    private int cantRegistrosGratis;
    private String institucion;
    private String tipoRegistro;

    public DTPatrocinio(int codigoPatrocinio, DTFecha fecha, double monto, NivelPatrocinio nivel,
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
    public NivelPatrocinio getNivel() {return nivel;}
    public int getCantRegistrosGratis() {return cantRegistrosGratis;}
    public String getInstitucion() {return institucion;}
    public String getTipoRegistro() {return tipoRegistro;}
}