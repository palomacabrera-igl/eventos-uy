package presentacion;

import javax.swing.JInternalFrame;

public class VentanaAltaEdicion extends JInternalFrame {

    public VentanaAltaEdicion() {
        super("Alta de Edicion de Evento", true, true, true, true);
        AltaEdicionPanel panel = new AltaEdicionPanel();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}