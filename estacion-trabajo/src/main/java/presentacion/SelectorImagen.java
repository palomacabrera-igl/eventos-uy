package presentacion;

import logica.DTImagen;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Elegir una imagen del disco para un alta o una modificacion. La usan todas
 * las pantallas que tienen "Elegir imagen...", asi el codigo esta una sola vez.
 *
 * Devuelve un DTImagen y no la entidad Imagen: la presentacion solo maneja DT.
 */
public final class SelectorImagen {

    private SelectorImagen() {
    }

    /** Lo que se eligio: los datos para la logica y el nombre para mostrar. */
    public record Eleccion(DTImagen imagen, String nombreArchivo) {
    }

    /**
     * Abre el selector de archivos, filtrando solo imagenes.
     *
     * @return la imagen elegida, o null si el usuario cancelo o el archivo no
     *         se pudo leer (en ese caso ya se mostro el aviso).
     */
    public static Eleccion elegir(Component padre, String titulo) {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (selector.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File archivo = selector.getSelectedFile();
        try {
            byte[] contenido = Files.readAllBytes(archivo.toPath());
            return new Eleccion(new DTImagen(contenido, tipoDeImagen(archivo.getName())), archivo.getName());
        } catch (IOException ex) {
            Mensajes.error(padre, titulo, "No se pudo leer el archivo " + archivo.getName() + ".");
            return null;
        }
    }

    /** El tipo de contenido segun la extension, por ejemplo "image/png". */
    private static String tipoDeImagen(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();
        if (nombre.endsWith(".png")) {
            return "image/png";
        }
        if (nombre.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}
