package presentacion;

import javax.swing.JInternalFrame;

public class VentanaModificarUsuario extends JInternalFrame {
    public VentanaModificarUsuario() {
        super("Modificar Datos de Usuario", true, true, true, true);
        ModificarUsuarioPanel panel = new ModificarUsuarioPanel();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}