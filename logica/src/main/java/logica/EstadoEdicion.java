package logica;

/**
 * Estado de una edicion de evento.
 * El alta la deja INGRESADA. El caso de uso Aceptar/Rechazar la pasa a ACEPTADA
 * o RECHAZADA, y de ahi no vuelve atras. Solo una ACEPTADA que ya termino se
 * puede pasar a ARCHIVADA, y lo hace su organizador (archivarEdicion).
 */
public enum EstadoEdicion {
    INGRESADA,
    ACEPTADA,
    RECHAZADA,
    ARCHIVADA
}
