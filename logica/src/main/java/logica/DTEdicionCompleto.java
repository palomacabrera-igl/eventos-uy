package logica;

import java.util.Set;
import java.time.LocalDate;

public class DTEdicionCompleto {
    private String nombre;
    private String sigla;
    private LocalDate fechaAlta;
    private LocalDate fechaIni;
    private LocalDate fechaFin;
    private String pais;
    private String ciudad;
    private DTOrganizador organizador;
    private Set<DTTipoRegistro> tiposRegistro;
    private Set<DTRegistro> registros;
    private Set<DTPatrocinio> patrocinios;

    public DTEdicionCompleto(String nombre, String sigla, LocalDate fechaAlta,
                             LocalDate fechaIni, LocalDate fechaFin, String pais, String ciudad,
                             DTOrganizador organizador, Set<DTTipoRegistro> tiposRegistro,
                             Set<DTRegistro> registros, Set<DTPatrocinio> patrocinios) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.fechaAlta = fechaAlta;
        this.fechaIni = fechaIni;
        this.fechaFin = fechaFin;
        this.pais = pais;
        this.ciudad = ciudad;
        this.organizador = organizador;
        this.tiposRegistro = tiposRegistro;
        this.registros = registros;
        this.patrocinios = patrocinios;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getSigla() {
        return sigla;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public LocalDate getFechaIni() {
        return fechaIni;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getPais() {
        return pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public DTOrganizador getOrganizador() {
        return organizador;
    }

    public Set<DTTipoRegistro> getTiposRegistro() {
        return tiposRegistro;
    }

    public Set<DTRegistro> getRegistros() {
        return registros;
    }

    public Set<DTPatrocinio> getPatrocinios() {
        return patrocinios;
    }
}