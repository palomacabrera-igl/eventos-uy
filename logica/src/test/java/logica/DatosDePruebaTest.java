package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prueba de caja negra de las imagenes que acompanian a los datos de prueba.
 *
 * <p>Estas imagenes vivian en la carpeta del servidor web y se leian del
 * disco con una ruta relativa. Al separar los dos repositorios dejaron de
 * estar ahi, y nada avisaba: la carga seguia compilando y solo reventaba
 * cuando alguien vaciaba la base e intentaba volver a llenarla, que es
 * justo el peor momento para enterarse.
 *
 * <p>Ahora viajan dentro del jar de la logica, en src/main/resources, y esta
 * prueba lo comprueba sin necesidad de base de datos: carga las imagenes y
 * nada mas. Si alguien mueve, renombra o borra una, falla la compilacion.
 *
 * <pre>
 *  cargarImagenesAntesDePersistir()
 *  CV  estan las 18 imagenes          -&gt; devuelve 18 entradas
 *  CV  ninguna esta vacia             -&gt; todas con contenido
 *  CV  el tipo sale de la extension   -&gt; png, jpeg y webp
 *  CI  (cubierta por el metodo)       -&gt; si falta una, lanza excepcion
 * </pre>
 */
@DisplayName("Imagenes de los datos de prueba")
class DatosDePruebaTest {

    /** Las que carga DatosDePrueba. Si cambia el numero, hay que mirarlo. */
    private static final int CUANTAS = 18;

    @Test
    @DisplayName("estan todas y ninguna esta vacia")
    void estanTodas() {
        Map<String, Imagen> imagenes = DatosDePrueba.cargarImagenesAntesDePersistir();

        assertEquals(CUANTAS, imagenes.size(),
                "cambio la cantidad de imagenes de los datos de prueba");

        imagenes.forEach((ruta, imagen) -> {
            assertNotNull(imagen, "no se cargo: " + ruta);
            assertNotNull(imagen.getContenido(), "sin contenido: " + ruta);
            assertTrue(imagen.getContenido().length > 0, "vacia: " + ruta);
        });
    }

    @Test
    @DisplayName("todas viajan dentro del jar, no en una carpeta de otro repositorio")
    void vienenDelClasspath() {
        Map<String, Imagen> imagenes = DatosDePrueba.cargarImagenesAntesDePersistir();

        imagenes.keySet().forEach(ruta -> {
            assertTrue(ruta.startsWith("imagenes/"),
                    "esta imagen no se busca dentro del jar: " + ruta);
            assertFalse(ruta.contains("servidor-web"),
                    "el Servidor Central no puede depender del servidor web: " + ruta);
            assertNotNull(DatosDePrueba.class.getClassLoader().getResource(ruta),
                    "no esta en el jar: " + ruta);
        });
    }

    @Test
    @DisplayName("el tipo de cada imagen sale de su extension")
    void elTipoSaleDeLaExtension() {
        Map<String, Imagen> imagenes = DatosDePrueba.cargarImagenesAntesDePersistir();

        imagenes.forEach((ruta, imagen) -> {
            String esperado;
            if (ruta.endsWith(".png")) {
                esperado = "image/png";
            } else if (ruta.endsWith(".jpg") || ruta.endsWith(".jpeg")) {
                esperado = "image/jpeg";
            } else {
                esperado = "image/webp";
            }
            assertEquals(esperado, imagen.getTipo(), "tipo equivocado en " + ruta);
        });
    }
}
