package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Consulta de Evento. Envuelve ConsultaEvento
 * (disenado con GUI Designer, ver ConsultaEvento.form) en una ventana
 * interna del escritorio.
 */
public class VentanaConsultaEvento extends JInternalFrame {

    public VentanaConsultaEvento() {
        super("Consulta de Evento", true, true, true, true);
        ConsultaEvento panel = new ConsultaEvento();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}
