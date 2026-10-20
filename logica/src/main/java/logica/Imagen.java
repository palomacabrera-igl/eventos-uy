package logica;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Imagen de una entidad (ver letra, seccion 7.2). El equipo decidio guardarla
 * en la base y no en el disco del servidor: la Estacion de Trabajo y el sitio
 * son dos procesos distintos y lo unico que comparten es la base, asi que una
 * imagen cargada desde el sitio se ve en Swing sin configurar rutas.
 *
 * &#64;Embeddable: NO genera una tabla propia. Sus dos campos se guardan como
 * columnas de la tabla de la entidad que la contiene, igual que si estuvieran
 * declarados ahi. Se usa cuando dos datos van siempre juntos y los necesitan
 * varias entidades, para no repetirlos en cada una.
 */
@Embeddable
public class Imagen {

    /**
     * Sin @Lob a proposito: en PostgreSQL, @Lob sobre un byte[] hace que
     * Hibernate use un "large object" (tipo oid), que se guarda aparte de la
     * fila y complica el borrado y el acceso fuera de una transaccion. Un
     * byte[] comun queda como bytea, que es una columna normal.
     */
    @Column(name = "imagen")
    private byte[] contenido;

    /**
     * Tipo de contenido, por ejemplo "image/png". Hace falta para que el
     * servlet que entregue la imagen pueda decirle al navegador que le esta
     * mandando (la cabecera Content-Type). Sin eso, los bytes llegan pero el
     * navegador no sabe como dibujarlos.
     */
    @Column(name = "imagen_tipo", length = 100)
    private String tipo;

    protected Imagen() {}

    public Imagen(byte[] contenido, String tipo) {
        this.contenido = contenido;
        this.tipo = tipo;
    }

    public byte[] getContenido() {return contenido;}
    public String getTipo() {return tipo;}
}
