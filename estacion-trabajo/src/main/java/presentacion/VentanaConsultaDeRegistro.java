package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Consulta de Registro.
 */
public class VentanaConsultaDeRegistro extends JInternalFrame {

    public VentanaConsultaDeRegistro() {
        super("Consulta de Registro", true, true, true, true);
        ConsultaDeRegistro panel = new ConsultaDeRegistro();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}
