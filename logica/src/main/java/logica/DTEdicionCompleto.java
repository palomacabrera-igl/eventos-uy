package logica;

import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * Todo lo que se sabe de una edicion. Las fechas son DTFecha y las colecciones
 * son List porque JAXB no sabe armar LocalDate ni trabaja bien con Set.
 */
@XmlType(name = "DTEdicionCompleto")
public class DTEdicionCompleto {
    private String nombre;
    private String sigla;
    private DTFecha fechaAlta;
    private DTFecha fechaIni;
    private DTFecha fechaFin;
    private String pais;
    private String ciudad;
    private DTOrganizador organizador;
    private List<DTTipoRegistro> tiposRegistro = new ArrayList<>();
    private List<DTRegistro> registros = new ArrayList<>();
    private List<DTPatrocinio> patrocinios = new ArrayList<>();

    /** Ingresada, Aceptada, Rechazada o Archivada: la pagina decide con esto quien puede verla. */
    private EstadoEdicion estado;

    /** Si hay que dibujar un <img> o el poster de color. Los bytes no viajan aca. */
    private boolean tieneImagen;

    /** Nombre del evento de la edicion, para volver a el desde la pagina. */
    private String nombreEvento;

    /** Direccion del video de YouTube de la edicion, o null si no tiene. Es opcional. */
    private String urlVideo;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTEdicionCompleto() {
    }

    public DTEdicionCompleto(String nombre, String sigla, DTFecha fechaAlta,
                             DTFecha fechaIni, DTFecha fechaFin, String pais, String ciudad,
                             DTOrganizador organizador, List<DTTipoRegistro> tiposRegistro,
                             List<DTRegistro> registros, List<DTPatrocinio> patrocinios,
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

    /**
     * Igual al de arriba, mas el nombre del evento al que pertenece la edicion:
     * la pagina de Consulta de Edicion (CU 9) lo usa para volver al evento. El
     * constructor anterior queda como estaba, asi la Estacion de Trabajo no cambia.
     */
    public DTEdicionCompleto(String nombre, String sigla, DTFecha fechaAlta,
                             DTFecha fechaIni, DTFecha fechaFin, String pais, String ciudad,
                             DTOrganizador organizador, List<DTTipoRegistro> tiposRegistro,
                             List<DTRegistro> registros, List<DTPatrocinio> patrocinios,
                             EstadoEdicion estado, boolean tieneImagen, String nombreEvento) {
        this(nombre, sigla, fechaAlta, fechaIni, fechaFin, pais, ciudad, organizador,
                tiposRegistro, registros, patrocinios, estado, tieneImagen);
        this.nombreEvento = nombreEvento;
    }

    /** El evento al que pertenece. Puede ser null si se armo con el constructor viejo. */
    public String getNombreEvento() {
        return nombreEvento;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getSigla() {
        return sigla;
    }

    public DTFecha getFechaAlta() {
        return fechaAlta;
    }

    public DTFecha getFechaIni() {
        return fechaIni;
    }

    public DTFecha getFechaFin() {
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

    public List<DTTipoRegistro> getTiposRegistro() {
        return tiposRegistro;
    }

    public List<DTRegistro> getRegistros() {
        return registros;
    }

    public List<DTPatrocinio> getPatrocinios() {
        return patrocinios;
    }

    public EstadoEdicion getEstado() {
        return estado;
    }

    public boolean getTieneImagen() {
        return tieneImagen;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setNombreEvento(String nombreEvento) {
        this.nombreEvento = nombreEvento;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public void setFechaAlta(DTFecha fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public void setFechaIni(DTFecha fechaIni) {
        this.fechaIni = fechaIni;
    }

    public void setFechaFin(DTFecha fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setOrganizador(DTOrganizador organizador) {
        this.organizador = organizador;
    }

    public void setTiposRegistro(List<DTTipoRegistro> tiposRegistro) {
        this.tiposRegistro = tiposRegistro;
    }

    public void setRegistros(List<DTRegistro> registros) {
        this.registros = registros;
    }

    public void setPatrocinios(List<DTPatrocinio> patrocinios) {
        this.patrocinios = patrocinios;
    }

    public void setEstado(EstadoEdicion estado) {
        this.estado = estado;
    }

    public void setTieneImagen(boolean tieneImagen) {
        this.tieneImagen = tieneImagen;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }
}
