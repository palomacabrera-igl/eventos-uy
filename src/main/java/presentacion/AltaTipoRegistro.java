package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.Fabrica;
import logica.IControladorSistema;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Contenido de la ventana interna "Alta de Tipo de Registro". El diseño
 * esta en AltaTipoRegistro.form (editable por arrastre); esta clase
 * contiene el comportamiento.
 */
public class AltaTipoRegistro {

    private JPanel mainPanel;
    private JComboBox EventoCBox;
    private JComboBox EdicionCBox;
    private JTextField NombreTxt;
    private JTextField DescripcionTxt;
    private JTextField CostoTxt;
    private JSpinner CupoSpinner;
    private JButton confirmarButton;
    private JButton cancelarButton;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    public AltaTipoRegistro() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        CupoSpinner.setModel(new SpinnerNumberModel(1, 1, 100000, 1));

        cargarEventos();

        EventoCBox.addActionListener(e -> cargarEdiciones());
        EdicionCBox.addActionListener(e -> seleccionarEdicion());
        confirmarButton.addActionListener(e -> confirmar());
        cancelarButton.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /** Define que hacer cuando el panel pide cerrarse (lo decide la ventana principal). */
    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void cargarEventos() {
        // listarEventos() : set<DTEvento>
        Set<DTEvento> eventos = controlador.listarEventos();
        for (DTEvento ev : eventos) {
            EventoCBox.addItem(ev.getNombre());
        }
        cargarEdiciones();
    }

    private void cargarEdiciones() {
        EdicionCBox.removeAllItems();
        String nombreEvento = (String) EventoCBox.getSelectedItem();
        if (nombreEvento == null) {
            return;
        }
        // listarEdicionesDeEvento(nombreEvento) : set<DTEdicionEvento> -- Sistema retiene eventoSeleccionado
        Set<DTEdicionEvento> ediciones = controlador.listarEdicionesDeEvento(nombreEvento);
        for (DTEdicionEvento ed : ediciones) {
            EdicionCBox.addItem(ed.getNombre());
        }
        seleccionarEdicion();
    }

    private void seleccionarEdicion() {
        String nombreEdicion = (String) EdicionCBox.getSelectedItem();
        if (nombreEdicion == null) {
            return;
        }
        // seleccionarEdicionEvento(nombreEdicion) -- Sistema retiene edicionSeleccionada
        controlador.seleccionarEdicionEvento(nombreEdicion);
    }

    private void confirmar() {
        if (EventoCBox.getSelectedItem() == null || EdicionCBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(mainPanel, "Elegí un evento y una edición.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = NombreTxt.getText().trim();
        String descripcion = DescripcionTxt.getText().trim();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Complete nombre y descripción.",
                    "Alta de Tipo de Registro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double costo;
        try {
            costo = Double.parseDouble(CostoTxt.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainPanel, "El costo tiene que ser un número (ej: 150 o 150.50).",
                    "Alta de Tipo de Registro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (costo < 0) {
            JOptionPane.showMessageDialog(mainPanel, "El costo no puede ser negativo.",
                    "Alta de Tipo de Registro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cupo = (Integer) CupoSpinner.getValue();

        // ingresarDatosTipoRegistro(nombre, descripcion, costo, cupo) : boolean
        if (controlador.ingresarDatosTipoRegistro(nombre, descripcion, costo, cupo)) {
            JOptionPane.showMessageDialog(mainPanel, "Tipo de registro creado con éxito.",
                    "Alta de Tipo de Registro", JOptionPane.INFORMATION_MESSAGE);
            limpiar();
            accionCerrar.run();
        } else {
            // [nombre ya en uso en esta edicion]: se avisa y se deja la ventana abierta
            // para reintentar (LOOP del dss), no se cierra ni se limpia.
            JOptionPane.showMessageDialog(mainPanel,
                    "Ya existe un tipo de registro con ese nombre en esta edición.",
                    "Nombre en uso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiar() {
        NombreTxt.setText("");
        DescripcionTxt.setText("");
        CostoTxt.setText("");
        CupoSpinner.setValue(1);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Alta de Tipo de Registro");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Evento");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EventoCBox = new JComboBox();
        mainPanel.add(EventoCBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Edición");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EdicionCBox = new JComboBox();
        mainPanel.add(EdicionCBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Nombre");
        mainPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        NombreTxt = new JTextField();
        mainPanel.add(NombreTxt, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Descripcion");
        mainPanel.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DescripcionTxt = new JTextField();
        mainPanel.add(DescripcionTxt, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Costo");
        mainPanel.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CostoTxt = new JTextField();
        mainPanel.add(CostoTxt, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Cupo");
        mainPanel.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CupoSpinner = new JSpinner();
        mainPanel.add(CupoSpinner, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        confirmarButton = new JButton();
        confirmarButton.setText("Confirmar");
        mainPanel.add(confirmarButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelarButton = new JButton();
        cancelarButton.setText("Cancelar");
        mainPanel.add(cancelarButton, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
