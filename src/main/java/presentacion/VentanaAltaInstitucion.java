package presentacion;

import logica.Fabrica;
import logica.IControladorSistema;
import logica.Status;

import javax.swing.*;

public class VentanaAltaInstitucion extends JInternalFrame {

    // Variables del formulario
    private JPanel mainPanel;
    private JTextField nombre;
    private JTextField desc;
    private JTextField sitioWeb;
    private JButton aceptarButton;
    private JButton cancelarButton;

    private final IControladorSistema controlador;

    public VentanaAltaInstitucion() {
        super("Alta de Institución", true, true, true, true);

        controlador = Fabrica.getInstancia().getControladorSistema();

        // Configuración de listeners
        aceptarButton.addActionListener(e -> aceptar());
        cancelarButton.addActionListener(e -> dispose());

        setContentPane(mainPanel);
        pack();
    }

    private void aceptar() {
        String nombreInstitucion = nombre.getText().trim();
        String descripcion = desc.getText().trim();
        String web = sitioWeb.getText().trim();

        if (nombreInstitucion.isEmpty()
                || descripcion.isEmpty()
                || web.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Completá todos los campos.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Llamada con Strings, no con DTO
        Status resultado = controlador.altaInstitucion(
                nombreInstitucion,
                descripcion,
                web
        );

        if (resultado == Status.OK) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Institución dada de alta correctamente.",
                    "Alta de institución",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose(); // o accionCerrar.run();

        } else {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Ya existe una institución con el nombre \""
                            + nombreInstitucion + "\".",
                    "Nombre en uso",
                    JOptionPane.WARNING_MESSAGE
            );
            nombre.requestFocus();
            nombre.selectAll();
        }
    }
}