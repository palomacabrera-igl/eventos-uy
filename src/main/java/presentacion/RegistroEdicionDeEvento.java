package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import logica.*;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Contenido de la ventana interna "Registro a Edicion de Evento". El diseño
 * esta en RegistroEdicionDeEvento.form (editable por arrastre); esta clase
 * contiene el comportamiento.
 * <p>
 * Flujo (DSS RegistroAEdicion):
 * 1) listarEventos()                       -> llena EventoCBox
 * 2) listarEdicionesDeEvento(nombreEvento) -> llena EdicionCBox (recuerda el evento)
 * 3) listarDatosRegistro(nombreEdicion)    -> llena AsistenteCBox y TipoRegistroCBox
 * (recuerda la edicion)
 * 4) altaRegistro(nickname, nombreEdicion, nombreTipo) -> OK / ERROR
 */
public class RegistroEdicionDeEvento {

    /** Titulo de todos los dialogos de este caso de uso. */
    private static final String TITULO = "Registro a Edición de Evento";

    private JPanel mainPanel;
    private JComboBox EventoCBox;
    private JComboBox EdicionCBox;
    private JComboBox AsistenteCBox;
    private JComboBox TipoRegistroCBox;
    private JLabel costoLbl;
    private JButton cancelarButton;
    private JButton confirmarButton;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };
    /**
     * Datos de la edicion seleccionada, para buscar el costo del tipo elegido.
     */
    private transient DTDatosRegistro datosRegistro;

    public RegistroEdicionDeEvento() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        cargarEventos();

        EventoCBox.addActionListener(e -> cargarEdiciones());
        EdicionCBox.addActionListener(e -> cargarDatosRegistro());
        TipoRegistroCBox.addActionListener(e -> mostrarCosto());
        confirmarButton.addActionListener(e -> confirmar());
        cancelarButton.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Define que hacer cuando el panel pide cerrarse (lo decide la ventana principal).
     */
    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void cargarEventos() {
        try {
            // listarEventos() : set<DTEvento>
            Set<DTEvento> eventos = controlador.listarEventos();
            for (DTEvento ev : eventos) {
                EventoCBox.addItem(ev.getNombre());
            }
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
        // Fuera del try: cargarEdiciones() ya maneja sus propios errores.
        cargarEdiciones();
    }

    private void cargarEdiciones() {
        EdicionCBox.removeAllItems();
        String nombreEvento = (String) EventoCBox.getSelectedItem();
        if (nombreEvento == null) {
            return;
        }
        try {
            // listarEdicionesDeEvento(nombreEvento) : set<DTEdicionEvento> -- Sistema retiene eventoSeleccionado
            Set<DTEdicionEvento> ediciones = controlador.listarEdicionesDeEvento(nombreEvento);
            for (DTEdicionEvento ed : ediciones) {
                EdicionCBox.addItem(ed.getNombre());
            }
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
            return; // no seguimos: evitamos encadenar un segundo dialogo
        }
        cargarDatosRegistro();
    }

    private void cargarDatosRegistro() {
        AsistenteCBox.removeAllItems();
        TipoRegistroCBox.removeAllItems();
        costoLbl.setText("");
        datosRegistro = null;

        String nombreEdicion = (String) EdicionCBox.getSelectedItem();
        if (nombreEdicion == null) {
            return;
        }
        try {
            // listarDatosRegistro(nombreEdicion) : DTDatosRegistro -- Sistema retiene edicionSeleccionada
            datosRegistro = controlador.listarDatosRegistro(nombreEdicion);
            for (DTAsistente a : datosRegistro.getAsistentes()) {
                AsistenteCBox.addItem(a.getNickname());
            }
            for (DTTipoRegistro t : datosRegistro.getTipoRegistro()) {
                TipoRegistroCBox.addItem(t.getNombre());
            }
        } catch (Exception ex) {
            // Estado consistente: sin datos cargados no se puede confirmar nada.
            datosRegistro = null;
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
            return;
        }
        mostrarCosto();
    }

    private void mostrarCosto() {
        String nombreTipo = (String) TipoRegistroCBox.getSelectedItem();
        if (nombreTipo == null || datosRegistro == null) {
            costoLbl.setText("");
            return;
        }
        for (DTTipoRegistro t : datosRegistro.getTipoRegistro()) {
            if (t.getNombre().equals(nombreTipo)) {
                costoLbl.setText("Costo: $" + t.getCosto());
                return;
            }
        }
    }

    private void confirmar() {
        String nickname = (String) AsistenteCBox.getSelectedItem();
        String nombreEdicion = (String) EdicionCBox.getSelectedItem();
        String nombreTipo = (String) TipoRegistroCBox.getSelectedItem();

        // (a) Validacion del FORMULARIO: fuera del try, no toca la logica.
        if (nickname == null || nombreEdicion == null || nombreTipo == null) {
            Mensajes.aviso(mainPanel, TITULO,
                    "Elegí evento, edición, asistente y tipo de registro.");
            return;
        }

        // (b) Llamada a la LOGICA: siempre dentro del try.
        try {
            controlador.altaRegistro(nickname, nombreEdicion, nombreTipo);
            Mensajes.exito(mainPanel, TITULO, "Registro creado con éxito.");
            accionCerrar.run();
        } catch (ReglaNegocioException ex) {
            Mensajes.error(mainPanel, TITULO, ex.getMessage());
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
        mainPanel.setLayout(new GridLayoutManager(8, 3, new Insets(15, 15, 15, 15), 10, 10));
        final JLabel label1 = new JLabel();
        label1.setText("Registro a Edicion de Evento");
        mainPanel.add(label1, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(1, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        EventoCBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        EventoCBox.setModel(defaultComboBoxModel1);
        mainPanel.add(EventoCBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EdicionCBox = new JComboBox();
        mainPanel.add(EdicionCBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Eventos");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Ediciones de Eventos");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        AsistenteCBox = new JComboBox();
        mainPanel.add(AsistenteCBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Asistentes");
        mainPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TipoRegistroCBox = new JComboBox();
        mainPanel.add(TipoRegistroCBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Tipos de Registro");
        mainPanel.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        costoLbl = new JLabel();
        costoLbl.setText("");
        mainPanel.add(costoLbl, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelarButton = new JButton();
        cancelarButton.setText("Cancelar");
        mainPanel.add(cancelarButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 30), null, 0, false));
        confirmarButton = new JButton();
        confirmarButton.setText("Aceptar");
        mainPanel.add(confirmarButton, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 30), null, 0, false));
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        mainPanel.add(toolBar$Separator1, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
