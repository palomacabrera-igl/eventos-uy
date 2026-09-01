package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Alta de Tipo de Registro. 
 */
public class VentanaAltaTipoRegistro extends JInternalFrame {

    public VentanaAltaTipoRegistro() {
        super("Alta de Tipo de Registro", true, true, true, true);
        AltaTipoRegistro panel = new AltaTipoRegistro();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}
