package logica;

import java.time.LocalDate;

public enum NivelPatrocinio {PLATINO, ORO, PLATA, BRONCE}

public class Patrocinio {
    private LocalDate fechaIni;
    private Double monto;
    private int cantRegistrosGratis;
    private int codigo;
    private NivelPatrocinio nivelPatro;

    public Patrocinio(LocalDate fechaIni, Double monto, int cantRegistrosGratis,
                      int codigo, NivelPatrocinio nivelPatro){
        this.fechaIni = fechaIni;
        this.monto = monto;
        this.cantRegistrosGratis = cantRegistrosGratis;
        this.codigo = codigo;
        this.nivelPatro = nivelPatro;
    }

    public LocalDate getFechaIni() {return fechaIni;}
    public Double getMonto() {return monto;}
    public int getCantRegistrosGratis() {return cantRegistrosGratis;}
    public int getCodigo() {return codigo;}
    public NivelPatrocinio getNivelPatro() {return nivelPatro;}

    public void setFechaIni(LocalDate fechaIni) {this.fechaIni = fechaIni;}
    public void setMonto(Double monto) {this.monto = monto;}
    public void setCantRegistrosGratis(int cantRegistrosGratis) {this.cantRegistrosGratis = cantRegistrosGratis;}
    public void setCodigo(int codigo) {this.codigo = codigo;}
    public void setNivelPatro(NivelPatrocinio nivelPatro) {this.nivelPatro = nivelPatro;}
}
