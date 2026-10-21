package logica;

/**
 * Una imagen cruzando la frontera de la logica: los bytes y su tipo de
 * contenido, sin nada del dominio adentro.
 *
 * Existe porque Imagen es una clase del dominio y no puede salir de la
 * logica (letra, seccion 7.4). Viaja sola, nunca dentro de un listado: la
 * cartelera pide seis DTEvento y cada uno dice solo si tiene imagen; los
 * bytes los pide despues el navegador, uno por uno, al servlet de imagenes.
 */
public class DTImagen {

    private final byte[] contenido;

    /** Tipo de contenido, por ejemplo "image/png". Lo necesita el navegador. */
    private final String tipo;

    public DTImagen(byte[] contenido, String tipo) {
        this.contenido = contenido;
        this.tipo = tipo;
    }

    public byte[] getContenido() {return contenido;}
    public String getTipo() {return tipo;}
}
