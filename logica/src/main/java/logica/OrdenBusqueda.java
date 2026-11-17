package logica;

/**
 * Como se ordenan los resultados de una busqueda (buscarEventos y buscarEdiciones).
 * Es un enumerado y no un texto para que no se pueda mandar un orden que no existe.
 */
public enum OrdenBusqueda {
    /** Por fecha de alta. */
    FECHA_ALTA,
    /** Alfabetico por nombre. */
    ALFABETICO
}
