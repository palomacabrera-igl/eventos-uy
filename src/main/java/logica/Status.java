package logica;

/**
 * <<Enumeration>> Status del DSS de Registro a Edicion de Evento.
 * Resultado de altaRegistro(): OK si se creo el registro, ERROR si el
 * asistente ya estaba registrado en la edicion o el tipo de registro no
 * tenia cupo.
 */
public enum Status {
    OK, ERROR
}
