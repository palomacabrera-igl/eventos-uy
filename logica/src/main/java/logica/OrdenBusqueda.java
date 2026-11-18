package logica;

/**
 * Como se ordenan los resultados de una busqueda (buscarEventos y buscarEdiciones).
 * Es un enumerado y no un texto para que no se pueda mandar un orden que no existe.
 */
public enum OrdenBusqueda {
    /** Por fecha de alta, de la mas reciente a la mas antigua. Es el orden por defecto. */
    FECHA_ALTA,
    /** Alfabetico por nombre, de la A a la Z. */
    ALFABETICO_ASC,
    /** Alfabetico por nombre, de la Z a la A. */
    ALFABETICO_DESC
}
