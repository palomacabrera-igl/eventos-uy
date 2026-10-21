package logica;

/**
 * Violacion de una regla de negocio de la plataforma. Es una condicion
 * esperada y recuperable: el administrador ingreso datos que no cumplen una
 * regla y puede corregirlos. El mensaje lo arma la capa de logica, que es la
 * que conoce la regla, y la capa de presentacion solo lo muestra.
 *
 * Checked a proposito: obliga a que la GUI la maneje y no pueda fingir que
 * la operacion tuvo exito.
 */
public class ReglaNegocioException extends Exception {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}