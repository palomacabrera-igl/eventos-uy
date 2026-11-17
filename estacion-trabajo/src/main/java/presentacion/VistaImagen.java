package presentacion;

import logica.DTImagen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Recuadro que muestra la imagen de un usuario, evento, edicion o institucion
 * en las consultas. La usan todas las consultas, asi el codigo esta una sola vez.
 *
 * Recibe un DTImagen (los bytes y el tipo), nunca la entidad. Si no hay imagen,
 * muestra "Sin imagen", porque la letra dice que la imagen es opcional.
 *
 * En los .form se agrega como componente de clase presentacion.VistaImagen.
 */
public class VistaImagen extends JLabel {

    /** Lado del recuadro, en pixeles. La imagen se achica para entrar sin deformarse. */
    private static final int LADO = 120;

    public VistaImagen() {
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        Dimension tamanio = new Dimension(LADO, LADO);
        setPreferredSize(tamanio);
        setMinimumSize(tamanio);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setForeground(Color.GRAY);
        limpiar();
    }

    /** Muestra la imagen, o "Sin imagen" si es null. */
    public void mostrar(DTImagen imagen) {
        if (imagen == null || imagen.getContenido() == null || imagen.getContenido().length == 0) {
            limpiar();
            return;
        }
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imagen.getContenido()));
            if (original == null) {
                // Un formato que Java no sabe leer (por ejemplo webp): se avisa en el recuadro.
                mostrarTexto("Formato no soportado");
                return;
            }
            setIcon(new ImageIcon(achicar(original)));
            setText(null);
        } catch (IOException ex) {
            mostrarTexto("No se pudo leer");
        }
    }

    /** Deja el recuadro vacio, por ejemplo cuando no hay nada seleccionado. */
    public void limpiar() {
        mostrarTexto("Sin imagen");
    }

    private void mostrarTexto(String texto) {
        setIcon(null);
        setText(texto);
    }

    /** Achica la imagen para que entre en el recuadro, manteniendo la proporcion. */
    private static Image achicar(BufferedImage original) {
        int espacio = LADO - 4; // lo que deja el borde
        double escala = Math.min(1.0, Math.min(
                (double) espacio / original.getWidth(), (double) espacio / original.getHeight()));
        int ancho = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int alto = Math.max(1, (int) Math.round(original.getHeight() * escala));
        return original.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
    }
}
