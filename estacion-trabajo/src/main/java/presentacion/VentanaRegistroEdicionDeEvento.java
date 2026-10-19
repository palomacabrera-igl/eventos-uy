package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Registro a Edicion de Evento.
 */
public class VentanaRegistroEdicionDeEvento extends JInternalFrame {

    public VentanaRegistroEdicionDeEvento() {
        super("Registro a Edicion de Evento", true, true, true, true);
        RegistroEdicionDeEvento panel = new RegistroEdicionDeEvento();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}
