package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Consulta de Edicion de Evento. Envuelve
 * ConsultaEdicionPanel (diseñado con GUI Designer, ver ConsultaEdicionPanel.form)
 * en una ventana interna del escritorio.
 */
public class VentanaConsultaEdicion extends JInternalFrame {

    public VentanaConsultaEdicion() {
        super("Consulta de Edicion de Evento", true, true, true, true);
        ConsultaEdicionPanel panel = new ConsultaEdicionPanel();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}