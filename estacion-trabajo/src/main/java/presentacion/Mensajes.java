package presentacion;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Diálogos de la aplicación con iconos propios, dibujados con Java2D.
 * Evita los iconos predeterminados del Look and Feel.
 */
public final class Mensajes {

    private static final Color VERDE = new Color(34, 168, 96);
    private static final Color ROJO = new Color(214, 69, 65);
    private static final Color AMBAR = new Color(230, 162, 60);

    private Mensajes() {
    }

    public static void exito(Component padre, String titulo, String mensaje) {
        mostrar(padre, titulo, mensaje, new IconoEstado(VERDE, Simbolo.CHECK));
    }

    public static void error(Component padre, String titulo, String mensaje) {
        mostrar(padre, titulo, mensaje, new IconoEstado(ROJO, Simbolo.CRUZ));
    }

    public static void aviso(Component padre, String titulo, String mensaje) {
        mostrar(padre, titulo, mensaje, new IconoEstado(AMBAR, Simbolo.EXCLAMACION));
    }

    /** Para el catch: algo que no previmos (NPE hoy, fallo de BD con JPA). */
    public static void errorInesperado(Component padre, String titulo, Throwable ex) {
        String detalle = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? ex.getClass().getSimpleName()
                : ex.getMessage();
        error(padre, titulo, "Ocurrió un error inesperado:\n" + detalle);
        ex.printStackTrace();   // además queda en consola para depurar
    }

    private static void mostrar(Component padre, String titulo, String mensaje, Icon icono) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.PLAIN_MESSAGE, icono);
    }

    private enum Simbolo {
        CHECK, CRUZ, EXCLAMACION
    }

    /** Círculo de color con un símbolo blanco encima. */
    private static final class IconoEstado implements Icon {

        private static final int TAMANIO = 44;

        private final Color color;
        private final Simbolo simbolo;

        private IconoEstado(Color color, Simbolo simbolo) {
            this.color = color;
            this.simbolo = simbolo;
        }

        @Override
        public int getIconWidth() {
            return TAMANIO;
        }

        @Override
        public int getIconHeight() {
            return TAMANIO;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                g2.setColor(color);
                g2.fillOval(x, y, TAMANIO, TAMANIO);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(TAMANIO * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(crearTrazo(x, y));

                if (simbolo == Simbolo.EXCLAMACION) {
                    int diametro = Math.round(TAMANIO * 0.10f);
                    g2.fillOval(
                            Math.round(x + TAMANIO * 0.50f - diametro / 2f),
                            Math.round(y + TAMANIO * 0.68f),
                            diametro,
                            diametro);
                }
            } finally {
                g2.dispose();
            }
        }

        private Path2D crearTrazo(int x, int y) {
            Path2D trazo = new Path2D.Float();
            switch (simbolo) {
                case CHECK -> {
                    trazo.moveTo(punto(x, 0.28f), punto(y, 0.52f));
                    trazo.lineTo(punto(x, 0.43f), punto(y, 0.67f));
                    trazo.lineTo(punto(x, 0.72f), punto(y, 0.34f));
                }
                case CRUZ -> {
                    trazo.moveTo(punto(x, 0.33f), punto(y, 0.33f));
                    trazo.lineTo(punto(x, 0.67f), punto(y, 0.67f));
                    trazo.moveTo(punto(x, 0.67f), punto(y, 0.33f));
                    trazo.lineTo(punto(x, 0.33f), punto(y, 0.67f));
                }
                case EXCLAMACION -> {
                    trazo.moveTo(punto(x, 0.50f), punto(y, 0.26f));
                    trazo.lineTo(punto(x, 0.50f), punto(y, 0.56f));
                }
            }
            return trazo;
        }

        private static float punto(int origen, float proporcion) {
            return origen + TAMANIO * proporcion;
        }
    }
}
