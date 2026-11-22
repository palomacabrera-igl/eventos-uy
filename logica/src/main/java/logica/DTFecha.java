package logica;

import jakarta.xml.bind.annotation.XmlType;

import java.time.LocalDate;

/**
 * Una fecha (dia, mes, anio) que puede viajar por la red. Es la forma de mandar una fecha
 * dentro de un DT: JAXB, que convierte los objetos en XML, no sabe armar un
 * java.time.LocalDate. Como todos los DT, cumple las reglas de JAXB (constructor vacio, un
 * getter y un setter por campo, y @XmlType con su nombre para que el sitio genere DTFecha
 * y no DtFecha); DatatypesJaxbTest lo comprueba.
 */
@XmlType(name = "DTFecha")
public class DTFecha {

    private int dia;
    private int mes;
    private int anio;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTFecha() {
    }

    public DTFecha(int dia, int mes, int anio) {
        this.dia = dia;
        this.mes = mes;
        this.anio = anio;
    }

    public static DTFecha desde(LocalDate fecha) {
        return new DTFecha(fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());
    }

    /**
     * Igual que desde(...), pero devuelve null si la fecha es null. Sirve para
     * los datos que pueden faltar, como la fecha de alta de una edicion guardada
     * antes de que ese dato existiera.
     */
    public static DTFecha desdeOpcional(LocalDate fecha) {
        return fecha == null ? null : desde(fecha);
    }

    public LocalDate aLocalDate() {
        return LocalDate.of(anio, mes, dia);
    }

    public int getDia() {return dia;}
    public int getMes() {return mes;}
    public int getAnio() {return anio;}

    public void setDia(int dia) {this.dia = dia;}
    public void setMes(int mes) {this.mes = mes;}
    public void setAnio(int anio) {this.anio = anio;}

}
