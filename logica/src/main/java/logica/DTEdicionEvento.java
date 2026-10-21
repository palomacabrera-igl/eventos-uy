package logica;

public class DTEdicionEvento {

    private String nombre;
    private String sigla;
    private String ciudad;
    private String pais;
    private DTFecha fechaInicio;
    private DTFecha fechaFin;
    private DTFecha fechaAlta;

    /**
     * Ingresada, Aceptada o Rechazada. El perfil del organizador muestra sus
     * ediciones en los tres estados y la pagina necesita saber cual es cual.
     */
    private EstadoEdicion estado;

    /** La pagina necesita saber si poner un <img> o el poster de color. */
    private boolean tieneImagen;

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

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}
    public DTFecha getFechaInicio() {return fechaInicio;}
    public DTFecha getFechaFin() {return fechaFin;}
    public DTFecha getFechaAlta() {return fechaAlta;}
    public EstadoEdicion getEstado() {return estado;}
    public boolean getTieneImagen() {return tieneImagen;}
}