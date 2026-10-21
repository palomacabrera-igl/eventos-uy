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

    /** Ingresada, Aceptada o Rechazada: la pagina decide con esto quien puede verla. */
    private EstadoEdicion estado;

    /** Si hay que dibujar un <img> o el poster de color. Los bytes no viajan aca. */
    private boolean tieneImagen;

    public DTEdicionCompleto(String nombre, String sigla, LocalDate fechaAlta,
                             LocalDate fechaIni, LocalDate fechaFin, String pais, String ciudad,
                             DTOrganizador organizador, Set<DTTipoRegistro> tiposRegistro,
                             Set<DTRegistro> registros, Set<DTPatrocinio> patrocinios,
                             EstadoEdicion estado, boolean tieneImagen) {
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
        this.estado = estado;
        this.tieneImagen = tieneImagen;
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

    public EstadoEdicion getEstado() {
        return estado;
    }

    public boolean getTieneImagen() {
        return tieneImagen;
    }
}