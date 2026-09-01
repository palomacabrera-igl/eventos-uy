package logica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdicionEvento {
    private String nombre;
    private String sigla;
    private LocalDate fechaIni;
    private LocalDate fechaFin;
    private LocalDate fechaAlta;
    private String ciudad;
    private String pais;
    private Organizador organizador;
    private final List<Patrocinio> patrocinios;
    private final List<TipoRegistro> tipoRegistros;
    private final List<Registro> registros;

    public EdicionEvento(String nombre, String sigla, LocalDate fechaIni,
                         LocalDate fechaFin, LocalDate fechaAlta, String ciudad,
                         String pais, Organizador organizador) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.fechaIni = fechaIni;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
        this.ciudad = ciudad;
        this.pais = pais;
        this.organizador = organizador;
        this.patrocinios = new ArrayList<>();
        this.tipoRegistros = new ArrayList<>();
        this.registros = new ArrayList<>();
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public LocalDate getfechaIni() {return fechaIni;}
    public LocalDate getfechaFin() {return fechaFin;}
    public LocalDate getfechaAlta() {return fechaAlta;}
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}
    public Organizador getOrganizador() {return organizador;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setfechaIni(LocalDate fechaIni) {this.fechaIni = fechaIni;}
    public void setfechaFin(LocalDate fechaFin) {this.fechaFin = fechaFin;}
    public void setfechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
    public void setCiudad(String ciudad) {this.ciudad = ciudad;}
    public void setPais(String pais) {this.pais = pais;}

    /** Paquete-visible: la usa Sistema para cargar datos de prueba (y, mas
     * adelante, Alta de Patrocinio). No es parte del contrato de la GUI. */
    void agregarPatrocinio(Patrocinio patrocinio) {
        patrocinios.add(patrocinio);
    }

    public DTEdicionEvento obtenerDT() {
        return new DTEdicionEvento(nombre, sigla, ciudad, pais,
                DTFecha.desde(fechaIni), DTFecha.desde(fechaFin), DTFecha.desde(fechaAlta));
    }

    public Set<DTPatrocinio> obtenerPatrocinios() {
        // 2.1*[foreach]: p := next()  /  2.2*: dt := obtenerDT()
        Set<DTPatrocinio> resultado = new HashSet<>();
        for (Patrocinio p : patrocinios) {
            resultado.add(p.obtenerDT());
        }
        return resultado;
    }

    public Patrocinio buscarPatrocinio(int codigo) {
        for (Patrocinio p : patrocinios) {
            if (p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    public DTEdicionCompleto obtenerDTCompleto() {
        Set<DTPatrocinio> dtPatrocinios = new HashSet<>();
        for (Patrocinio p : patrocinios) {
            dtPatrocinios.add(p.obtenerDT());
        }

        Set<DTTipoRegistro> dtTipos = new HashSet<>();
        for (TipoRegistro t : tipoRegistros) {
            dtTipos.add(t.obtenerDT());
        }

        Set<DTRegistro> dtRegistros = new HashSet<>();
        for (Registro r : registros) {
            dtRegistros.add(r.obtenerDT());
        }

        DTOrganizador dtOrg = organizador.obtenerDT();

        return new DTEdicionCompleto(nombre, sigla, fechaAlta, fechaIni, fechaFin,
                pais, ciudad, dtOrg, dtTipos, dtRegistros, dtPatrocinios);
    }

}