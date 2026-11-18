package logica;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Revisa la direccion del video de YouTube de una edicion (letra, 7.4) y la
 * deja siempre con la misma forma: https://www.youtube.com/watch?v=ID. El sitio
 * la pasa a https://www.youtube.com/embed/ID para el reproductor.
 */
final class VideoYoutube {

    /** El id de un video: 11 caracteres entre letras, numeros, "-" y "_". */
    private static final String ID = "([A-Za-z0-9_-]{11})";

    private static final String PROTOCOLO = "(?:https?://)?";
    private static final String DOMINIO = "(?:www\\.|m\\.)?youtube\\.com";

    /** Las direcciones que da YouTube al compartir: watch?v=, youtu.be/ y shorts/. */
    private static final Pattern[] FORMAS = {
        Pattern.compile("^" + PROTOCOLO + DOMINIO + "/watch\\?(?:[^#]*&)?v=" + ID + "(?:[&#].*)?$",
                Pattern.CASE_INSENSITIVE),
        Pattern.compile("^" + PROTOCOLO + "youtu\\.be/" + ID + "(?:[?#].*)?$",
                Pattern.CASE_INSENSITIVE),
        Pattern.compile("^" + PROTOCOLO + DOMINIO + "/shorts/" + ID + "(?:[/?#].*)?$",
                Pattern.CASE_INSENSITIVE),
    };

    private VideoYoutube() {}

    /**
     * La direccion en la forma watch?v=ID, o null si no se indico video (es
     * opcional). Si se indico algo que no es un video de YouTube, es un error.
     */
    static String normalizar(String direccion) throws ReglaNegocioException {
        if (direccion == null || direccion.isBlank()) {
            return null;
        }
        String limpia = direccion.trim();
        for (Pattern forma : FORMAS) {
            Matcher m = forma.matcher(limpia);
            if (m.matches()) {
                return "https://www.youtube.com/watch?v=" + m.group(1);
            }
        }
        throw new ReglaNegocioException("La dirección del video tiene que ser de YouTube, "
                + "por ejemplo https://www.youtube.com/watch?v=...");
    }
}
