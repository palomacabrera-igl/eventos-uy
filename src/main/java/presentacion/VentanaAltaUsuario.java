package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Alta de Usuario ("Crear Cuenta").
 */
public class VentanaAltaUsuario extends JInternalFrame {

    public VentanaAltaUsuario() {
        super("Crear Cuenta", true, true, true, true);
        AltaUsuarioPanel panel = new AltaUsuarioPanel();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}