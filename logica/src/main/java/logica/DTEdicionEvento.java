package logica;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "DTEdicionEvento")
public class DTEdicionEvento {

    private String nombre;
    private String sigla;
    private String ciudad;
    private String pais;
    private DTFecha fechaInicio;
    private DTFecha fechaFin;
    private DTFecha fechaAlta;

    /**
     * Ingresada, Aceptada, Rechazada o Archivada. El perfil del organizador
     * muestra sus ediciones y la pagina necesita saber cual es cual.
     */
    private EstadoEdicion estado;

    /** La pagina necesita saber si poner un <img> o el poster de color. */
    private boolean tieneImagen;

    /**
     * De que evento es esta edicion.
     *
     * Hace falta para las pantallas que listan las ediciones de alguien y
     * tienen que agruparlas o filtrarlas por evento, como el Alta de
     * Patrocinio. Sin esto, "JIAP 2027" no dice a que evento pertenece.
     */
    private String nombreEvento;

    /**
     * Direccion del video de YouTube de la edicion (por ejemplo
     * https://www.youtube.com/watch?v=...), o null si no tiene. Es opcional.
     */
    private String urlVideo;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTEdicionEvento() {
    }

    public DTEdicionEvento(String nombre, String sigla, String ciudad, String pais,
                           DTFecha fechaInicio, DTFecha fechaFin, DTFecha fechaAlta) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.ciudad = ciudad;
        this.pais = pais;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
    }

    /**
     * Para una edicion que YA EXISTE: ademas de sus datos lleva el estado y
     * si tiene imagen. El constructor de arriba queda para cuando la edicion
     * todavia no existe, como en el alta, donde esos dos datos no tienen
     * ningun valor posible.
     */
    public DTEdicionEvento(String nombre, String sigla, String ciudad, String pais,
                           DTFecha fechaInicio, DTFecha fechaFin, DTFecha fechaAlta,
                           EstadoEdicion estado, boolean tieneImagen) {
        this(nombre, sigla, ciudad, pais, fechaInicio, fechaFin, fechaAlta);
        this.estado = estado;
        this.tieneImagen = tieneImagen;
    }

    /**
     * Para una edicion que ya existe, diciendo ademas de que evento es.
     * Los dos constructores de arriba siguen valiendo: este solo agrega un
     * dato mas, no reemplaza a ninguno.
     */
    public DTEdicionEvento(String nombre, String sigla, String ciudad, String pais,
                           DTFecha fechaInicio, DTFecha fechaFin, DTFecha fechaAlta,
                           EstadoEdicion estado, boolean tieneImagen, String nombreEvento) {
        this(nombre, sigla, ciudad, pais, fechaInicio, fechaFin, fechaAlta,
                estado, tieneImagen);
        this.nombreEvento = nombreEvento;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}
    public DTFecha getFechaInicio() {return fechaInicio;}
    public DTFecha getFechaFin() {return fechaFin;}
    public DTFecha getFechaAlta() {return fechaAlta;}
    public EstadoEdicion getEstado() {return estado;}
    public boolean getTieneImagen() {return tieneImagen;}
    public String getNombreEvento() {return nombreEvento;}
    public String getUrlVideo() {return urlVideo;}

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setCiudad(String ciudad) {this.ciudad = ciudad;}
    public void setPais(String pais) {this.pais = pais;}
    public void setFechaInicio(DTFecha fechaInicio) {this.fechaInicio = fechaInicio;}
    public void setFechaFin(DTFecha fechaFin) {this.fechaFin = fechaFin;}
    public void setFechaAlta(DTFecha fechaAlta) {this.fechaAlta = fechaAlta;}
    public void setEstado(EstadoEdicion estado) {this.estado = estado;}
    public void setTieneImagen(boolean tieneImagen) {this.tieneImagen = tieneImagen;}
    public void setNombreEvento(String nombreEvento) {this.nombreEvento = nombreEvento;}
    public void setUrlVideo(String urlVideo) {this.urlVideo = urlVideo;}
}
