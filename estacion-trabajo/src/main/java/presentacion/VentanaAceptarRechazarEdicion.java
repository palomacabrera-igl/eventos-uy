package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Aceptar/Rechazar Edicion de Evento. Envuelve
 * AceptarRechazarEdicionPanel (ver AceptarRechazarEdicionPanel.form) en una
 * ventana interna del escritorio.
 */
public class VentanaAceptarRechazarEdicion extends JInternalFrame {

    public VentanaAceptarRechazarEdicion() {
        super("Aceptar/Rechazar Edición de Evento", true, true, true, true);
        AceptarRechazarEdicionPanel panel = new AceptarRechazarEdicionPanel();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}
