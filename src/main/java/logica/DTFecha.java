package logica;

import java.time.LocalDate;

public class DTFecha {

    private int dia;
    private int mes;
    private int anio;

    public DTFecha(int dia, int mes, int anio) {
        this.dia = dia;
        this.mes = mes;
        this.anio = anio;
    }

    public static DTFecha desde(LocalDate fecha) {
        return new DTFecha(fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());
    }

    public LocalDate aLocalDate() {
        return LocalDate.of(anio, mes, dia);
    }

    public int getDia() {return dia;}
    public int getMes() {return mes;}
    public int getAnio() {return anio;}

}
