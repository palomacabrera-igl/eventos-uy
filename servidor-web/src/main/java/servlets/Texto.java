package servlets;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import logica.DTFecha;

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

    /**
     * Prepara un valor para ir dentro de una URL (?nombre=...). Los nombres
     * tienen espacios y tildes ("Maratón de Montevideo"), que en una direccion
     * no pueden ir tal cual. Es la decision D5: claves naturales codificadas.
     */
    public static String paraUrl(String valor) {
        return valor == null ? "" : URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    /** Una fecha del DT como la mostraba la Parte 1: 01/10/2026. */
    public static String fecha(DTFecha fecha) {
        if (fecha == null) {
            return "";
        }
        return String.format("%02d/%02d/%04d", fecha.getDia(), fecha.getMes(), fecha.getAnio());
    }
}
