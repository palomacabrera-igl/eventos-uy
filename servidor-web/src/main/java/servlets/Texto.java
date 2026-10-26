package servlets;

/**
 * Escapa el texto que escribio una persona antes de meterlo en el HTML.
 *
 * Sin esto, alguien que se registre con el nombre "&lt;script&gt;..." logra que ese
 * script se ejecute en el navegador de cualquiera que mire la pagina. Es el
 * mismo motivo por el que en la Parte 1 se usaba textContent y no innerHTML.
 */
public final class Texto {

    private Texto() {
    }

    public static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }
}
