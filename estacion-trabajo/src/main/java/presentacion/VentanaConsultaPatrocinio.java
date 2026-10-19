package presentacion;

import javax.swing.JInternalFrame;

public class VentanaConsultaPatrocinio extends JInternalFrame {

    public VentanaConsultaPatrocinio() {
        super("Consulta de Patrocinio", true, true, true, true);
        ConsultaPatrocinioPanel panel = new ConsultaPatrocinioPanel();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}