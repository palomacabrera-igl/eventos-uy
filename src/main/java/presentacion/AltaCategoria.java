package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import logica.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Contenido de la ventana interna "Alta de Categoria". El diseño esta en
 * AltaCategoria.form (editable por arrastre); esta clase contiene el
 * comportamiento.
 * <p>
 * Flujo (DSS Categoria):
 * 1) listarCategoriasArbol() -> arma el arbol: raices con sus hijas anidadas
 *    (recursivo, refleja la jerarquia Categoria -> 0..* hijas).
 * 2) altaCategoria(nombre, nombrePadre) -> OK: se agrega al arbol (como hija de
 *    la categoria seleccionada, o como raiz si no hay nada seleccionado);
 *    ERROR (ya existe): se avisa y la ventana queda abierta (LOOP del DSS).
 */
public class AltaCategoria {

    /** Titulo de todos los dialogos de este caso de uso. */
    private static final String TITULO = "Alta de Categoría";

    private JTextField NombreTxt;
    private JTree CategoriasTree;
    private JPanel mainPanel;
    private JButton cancelarButton;
    private JButton aceptarButton;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    public AltaCategoria() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        recargarArbol();

        aceptarButton.addActionListener(e -> aceptar());
        cancelarButton.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /** Define que hacer cuando el panel pide cerrarse (lo decide la ventana principal). */
    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    /**
     * Arma el arbol de categorias: un nodo raiz "Categorias" y una hoja por
     * cada categoria existente. Se llama al inicio y despues de cada alta OK,
     * para que el arbol refleje la nueva categoria.
     */
    private void recargarArbol() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Categorias");
        try {
            // listarCategoriasArbol() : set<DTCategoria> (raices con hijas anidadas)
            for (DTCategoria c : controlador.listarCategoriasArbol()) {
                raiz.add(construirNodo(c));
            }
        } catch (Exception ex) {
            // El arbol queda solo con la raiz, pero la ventana abre igual.
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
        CategoriasTree.setModel(new DefaultTreeModel(raiz));
        // Expandir para que se vean las categorias colgando de la raiz.
        for (int i = 0; i < CategoriasTree.getRowCount(); i++) {
            CategoriasTree.expandRow(i);
        }
    }

    /**
     * Convierte un DTCategoria (y todo su subarbol) en nodos del JTree, de forma
     * RECURSIVA: por cada hija del DT crea un nodo hijo y baja por las suyas.
     */
    private DefaultMutableTreeNode construirNodo(DTCategoria categoria) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(categoria.getNombre());
        for (DTCategoria hija : categoria.getHijas()) {
            nodo.add(construirNodo(hija));   // recursion
        }
        return nodo;
    }

    private void aceptar() {
        // (a) Validacion del FORMULARIO: fuera del try, no toca la logica.
        String nombre = NombreTxt.getText().trim();
        if (nombre.isEmpty()) {
            Mensajes.aviso(mainPanel, TITULO, "Ingresá el nombre de la categoría.");
            return;
        }
        // Si hay una categoria seleccionada en el arbol, la nueva se crea como su
        // hija; si no hay nada seleccionado, se crea como categoria raiz.
        String nombrePadre = padreSeleccionado();

        // (b) Llamada a la LOGICA: siempre dentro del try.
        try {
            controlador.altaCategoria(nombre, nombrePadre);
            recargarArbol();      // el arbol refleja la nueva categoria
            NombreTxt.setText("");
            Mensajes.exito(mainPanel, TITULO, "Categoría creada con éxito.");
        } catch (ReglaNegocioException ex) {
            // LOOP del CU: se avisa y la ventana NO se cierra
            Mensajes.error(mainPanel, TITULO, ex.getMessage());
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
    }

    /**
     * Nombre de la categoria seleccionada en el arbol (sera el padre de la nueva),
     * o null si no hay seleccion o si esta seleccionada la raiz ficticia "Categorias".
     */
    private String padreSeleccionado() {
        TreePath seleccion = CategoriasTree.getSelectionPath();
        if (seleccion == null) {
            return null;
        }
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) seleccion.getLastPathComponent();
        if (nodo.getParent() == null) {
            return null;   // es la raiz ficticia "Categorias", no una categoria real
        }
        return nodo.getUserObject().toString();
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
        mainPanel.setLayout(new GridLayoutManager(5, 2, new Insets(15, 15, 15, 15), 10, 10));
        final JLabel label1 = new JLabel();
        label1.setText("Nombre de la nueva categoria");
        mainPanel.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Categorias Existentes");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        NombreTxt = new JTextField();
        mainPanel.add(NombreTxt, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 120), null, null, 0, false));
        CategoriasTree = new JTree();
        scrollPane1.setViewportView(CategoriasTree);
        final JLabel label3 = new JLabel();
        label3.setText("Alta Categoria");
        mainPanel.add(label3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelarButton = new JButton();
        cancelarButton.setText("Cancelar");
        mainPanel.add(cancelarButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aceptarButton = new JButton();
        aceptarButton.setText("Aceptar");
        mainPanel.add(aceptarButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
