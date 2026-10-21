package presentacion;

import javax.swing.JInternalFrame;

/**
 * Pantalla del caso de uso Alta de Categoria.
 */
public class VentanaAltaCategoria extends JInternalFrame {

    public VentanaAltaCategoria() {
        super("Alta de Categoria", true, true, true, true);
        AltaCategoria panel = new AltaCategoria();
        panel.setAccionCerrar(this::dispose);
        setContentPane(panel.getMainPanel());
        pack();
    }
}
