package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import logica.Fabrica;
import logica.IControladorSistema;
import logica.Status;
import logica.DTCategoria;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.time.DateTimeException;

public class VentanaAltaEvento extends JInternalFrame {

    /**
     * Titulo de todos los dialogos de este caso de uso (criterio del equipo).
     */
    private static final String TITULO = "Alta de Evento";

    private JPanel mainPanel;
    private JTextField nombretxt;
    private JTextField desctxt;
    private JTextField siglatxt;
    private JTree catTree;
    private JButton aceptarButton;
    private JButton cancelarButton;
    private JSpinner spinnerDia;
    private JSpinner spinnerMes;
    private JSpinner spinnerAnio;

    private final IControladorSistema controlador;

    public VentanaAltaEvento() {
        super("Alta de Evento", true, true, true, true);
        controlador = Fabrica.getInstancia().getControladorSistema();

        int anioActual = LocalDate.now().getYear();
        spinnerDia.setModel(new SpinnerNumberModel(1, 1, 31, 1));
        spinnerMes.setModel(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerAnio.setModel(new SpinnerNumberModel(anioActual, 1800, anioActual, 1));

        cargarCategorias();

        aceptarButton.addActionListener(e -> aceptar());
        cancelarButton.addActionListener(e -> dispose());

        setContentPane(mainPanel);
        pack();
    }

    /**
     * Carga las categorias en el JList. Estaba suelto en el constructor: si
     * listarCategorias() fallaba, la ventana ni siquiera abria.
     */
    private void cargarCategorias() {
        try {
            Set<DTCategoria> categorias = controlador.listarCategoriasArbol();

            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Categorías");
            for (DTCategoria c : categorias) {
                root.add(buildTreeNode(c));
            }

            // Reutiliza el JTree ya creado por el diseñador
            catTree.setModel(new DefaultTreeModel(root));
            catTree.setRootVisible(false);
            catTree.setShowsRootHandles(true);
            catTree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

            // No agregues scrollPane ni mainPanel.add(...)
            // El diseñador ya lo hizo en $$$setupUI$$$
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
    }

    private DefaultMutableTreeNode buildTreeNode(DTCategoria categoria) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(categoria.getNombre());
        for (DTCategoria hija : categoria.getHijas()) {
            node.add(buildTreeNode(hija));
        }
        return node;
    }

    private void aceptar() {
        String nombre = nombretxt.getText().trim();
        String descripcion = desctxt.getText().trim();
        String sigla = siglatxt.getText().trim();

        // (a) Validacion del FORMULARIO: fuera del try de la logica.
        if (nombre.isEmpty() || descripcion.isEmpty() || sigla.isEmpty()) {
            Mensajes.aviso(mainPanel, TITULO, "Completá todos los campos.");
            return;
        }

        LocalDate fechaAlta;
        try {
            // Validacion de formato del formulario, no un fallo del sistema.
            fechaAlta = LocalDate.of((int) spinnerAnio.getValue(),
                    (int) spinnerMes.getValue(), (int) spinnerDia.getValue());
        } catch (DateTimeException ex) {
            Mensajes.aviso(mainPanel, TITULO,
                    "La fecha de alta no existe (revisá el día para ese mes).");
            return;
        }
        if (fechaAlta.isAfter(LocalDate.now())) {
            Mensajes.aviso(mainPanel, TITULO,
                    "La fecha de alta no puede ser posterior a la fecha actual.");
            return;
        }

        List<String> nombresCategorias = new ArrayList<>();
        TreePath[] paths = catTree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                nombresCategorias.add(node.toString());
            }
        }
        if (nombresCategorias.isEmpty()) {
            Mensajes.aviso(mainPanel, TITULO, "Debe seleccionar al menos una categoría.");
            return;
        }

        // (b) Llamada a la LOGICA: siempre dentro del try.
        try {
            Status resultado = controlador.ingresarDatosEvento(
                    nombre, descripcion, fechaAlta, sigla, nombresCategorias);

            if (resultado == Status.OK) {
                Mensajes.exito(mainPanel, TITULO, "Evento dado de alta correctamente.");
                dispose();
            } else {
                Mensajes.error(mainPanel, TITULO,
                        "Ya existe un evento con el nombre \"" + nombre + "\".");
                nombretxt.requestFocus();
                nombretxt.selectAll();
            }
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
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
        mainPanel.setLayout(new GridLayoutManager(7, 2, new Insets(15, 15, 15, 15), 10, 10));
        final JLabel label1 = new JLabel();
        label1.setText("Nombre del evento");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        nombretxt = new JTextField();
        nombretxt.setText("");
        mainPanel.add(nombretxt, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Descripción");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Fecha de Alta");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Sigla");
        mainPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Categorías\n\n(Ctrl+clic para varias)");
        mainPanel.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        desctxt = new JTextField();
        desctxt.setText("");
        mainPanel.add(desctxt, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        siglatxt = new JTextField();
        siglatxt.setText("");
        mainPanel.add(siglatxt, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        aceptarButton = new JButton();
        aceptarButton.setText("Aceptar");
        mainPanel.add(aceptarButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        spinnerDia = new JSpinner();
        panel1.add(spinnerDia, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerMes = new JSpinner();
        panel1.add(spinnerMes, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerAnio = new JSpinner();
        panel1.add(spinnerAnio, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        catTree = new JTree();
        scrollPane1.setViewportView(catTree);
        cancelarButton = new JButton();
        cancelarButton.setText("Cancelar");
        mainPanel.add(cancelarButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
