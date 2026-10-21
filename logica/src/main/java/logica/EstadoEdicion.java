package logica;

/**
 * Estado de una edicion de evento.
 * El alta la deja INGRESADA. Solo el caso de uso Aceptar/Rechazar la cambia,
 * y una vez ACEPTADA o RECHAZADA no vuelve atras.
 */
public enum EstadoEdicion {
    INGRESADA,
    ACEPTADA,
    RECHAZADA
}
