package presentacion;

import javax.swing.JInternalFrame;

public class VentanaAltaPatrocinio extends JInternalFrame {

    public VentanaAltaPatrocinio() {
        super("Alta de Patrocinio", true, true, true, true);
        AltaPatrocinioPanel panel = new AltaPatrocinioPanel();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}