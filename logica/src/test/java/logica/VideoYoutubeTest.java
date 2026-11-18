package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Video de YouTube de una edicion (letra, 7.4), sin base.
 *
 * <pre>
 *  CV  watch?v=, youtu.be/ y shorts/, con o sin https y www -&gt; watch?v=ID
 *  CV  null, vacio o solo espacios                          -&gt; null (sin video)
 *  CI  otro sitio, sin id, id corto o un texto cualquiera   -&gt; ReglaNegocioException
 *  CV  el video viaja en los DT de la edicion
 * </pre>
 */
@DisplayName("Video de YouTube de una edicion")
class VideoYoutubeTest {

    private static final String ESPERADA = "https://www.youtube.com/watch?v=jNQXAC9IVRw";

    @ParameterizedTest(name = "{0}")
    @DisplayName("las direcciones de YouTube quedan como watch?v=ID")
    @ValueSource(strings = {
        "https://www.youtube.com/watch?v=jNQXAC9IVRw",
        "http://youtube.com/watch?v=jNQXAC9IVRw",
        "www.youtube.com/watch?v=jNQXAC9IVRw",
        "https://m.youtube.com/watch?v=jNQXAC9IVRw",
        "https://www.youtube.com/watch?v=jNQXAC9IVRw&t=42s",
        "https://www.youtube.com/watch?feature=share&v=jNQXAC9IVRw",
        "  https://www.youtube.com/watch?v=jNQXAC9IVRw  ",
        "https://youtu.be/jNQXAC9IVRw",
        "https://youtu.be/jNQXAC9IVRw?si=abc123",
        "https://www.youtube.com/shorts/jNQXAC9IVRw",
        "HTTPS://WWW.YOUTUBE.COM/watch?v=jNQXAC9IVRw"
    })
    void direccionesValidas(String direccion) throws ReglaNegocioException {
        assertEquals(ESPERADA, VideoYoutube.normalizar(direccion));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @DisplayName("sin direccion, la edicion queda sin video")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void sinVideo(String direccion) throws ReglaNegocioException {
        assertNull(VideoYoutube.normalizar(direccion));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("lo que no es un video de YouTube se rechaza")
    @ValueSource(strings = {
        "https://vimeo.com/123456789",
        "https://www.youtube.com/",
        "https://www.youtube.com/watch?v=corto",
        "https://www.youtube.com/watch?v=jNQXAC9IVRwX",
        "https://www.youtube.com.evil.uy/watch?v=jNQXAC9IVRw",
        "https://www.youtube.com/embed/jNQXAC9IVRw",
        "javascript:alert(1)",
        "un video"
    })
    void direccionesInvalidas(String direccion) {
        assertThrows(ReglaNegocioException.class, () -> VideoYoutube.normalizar(direccion));
    }

    @Test
    @DisplayName("el video viaja en el DT del listado y en los dos DT completos")
    void viajaEnLosDT() {
        Organizador org = new Organizador("org", "Org", "org@ejemplo.uy", "Organiza", null);
        LocalDate inicio = LocalDate.of(2027, 5, 10);
        EdicionEvento edicion = new EdicionEvento("Con video", "CV", inicio, inicio,
                LocalDate.of(2026, 11, 1), "Montevideo", "Uruguay", org);

        assertNull(edicion.obtenerDT().getUrlVideo());

        edicion.asignarVideo(ESPERADA);

        assertEquals(ESPERADA, edicion.getUrlVideo());
        assertEquals(ESPERADA, edicion.obtenerDT().getUrlVideo());
        assertEquals(ESPERADA, edicion.obtenerDTCompleto().getUrlVideo());
        assertEquals(ESPERADA, edicion.obtenerDTCompletoPara(null).getUrlVideo());
    }
}
