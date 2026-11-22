package logica;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Imagen de una entidad (letra, seccion 7.2). Se guarda en la base y no en el disco: la
 * Estacion de Trabajo y el sitio son dos procesos que solo comparten la base, asi que una
 * imagen cargada desde el sitio se ve en Swing sin configurar rutas. Es @Embeddable: no
 * genera una tabla propia, sus dos campos son columnas de la entidad que la contiene.
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
