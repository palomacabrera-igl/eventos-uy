package logica;

import jakarta.xml.bind.annotation.XmlType;

/**
 * Una imagen cruzando la frontera de la logica: los bytes y su tipo de
 * contenido, sin nada del dominio adentro.
 *
 * Existe porque Imagen es una clase del dominio y no puede salir de la
 * logica (letra, seccion 7.4). Viaja sola, nunca dentro de un listado: la
 * cartelera pide seis DTEvento y cada uno dice solo si tiene imagen; los
 * bytes los pide despues el navegador, uno por uno, al servlet de imagenes.
 */
@XmlType(name = "DTImagen")
public class DTImagen {

    private byte[] contenido;

    /** Tipo de contenido, por ejemplo "image/png". Lo necesita el navegador. */
    private String tipo;

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTImagen() {
    }

    public DTImagen(byte[] contenido, String tipo) {
        this.contenido = contenido;
        this.tipo = tipo;
    }

    public byte[] getContenido() {return contenido;}
    public String getTipo() {return tipo;}

    // Setters: los usa JAXB para reconstruir el objeto cuando llega por un servicio web.
    public void setContenido(byte[] contenido) {this.contenido = contenido;}
    public void setTipo(String tipo) {this.tipo = tipo;}
}
