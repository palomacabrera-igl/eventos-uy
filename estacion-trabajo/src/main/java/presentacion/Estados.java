package presentacion;

import logica.EstadoEdicion;

/**
 * Como se muestra el estado de una edicion en las pantallas: "Ingresada",
 * "Aceptada" o "Rechazada", igual que en la letra. Esta en un solo lugar para
 * que todas las consultas lo escriban igual.
 */
final class Estados {

    private Estados() {
    }

    static String texto(EstadoEdicion estado) {
        if (estado == null) {
            return "(sin estado)";
        }
        return switch (estado) {
            case INGRESADA -> "Ingresada";
            case ACEPTADA -> "Aceptada";
            case RECHAZADA -> "Rechazada";
        };
    }
}
