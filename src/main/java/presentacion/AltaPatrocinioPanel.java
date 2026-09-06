package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTPatrocinio;
import logica.DTTipoRegistro;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.NivelPatrocinio;
import logica.ReglaNegocioException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;

import javax.swing.*;
import java.awt.*;

public class AltaPatrocinioPanel {
    private JPanel mainPanel;
    private JComboBox EventoCBox;
    private JComboBox EdicionCBox;
    private JComboBox TipoRegistroCBox;
    private JComboBox InstitucionCBox;
    private JTextField CodigoTxt;
    private JRadioButton bronceRadioButton;
    private JRadioButton plataRadioButton;
    private JRadioButton oroRadioButton;
    private JRadioButton platinoRadioButton;
    private JTextField AporteTxt;
    private JSpinner RegistrosSpinner;
    private JButton confirmarButton;
    private JButton cancelarButton;
    private JLabel ResumenLbl;

    private static final String TITULO = "Alta de Patrocinio";

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    private boolean cargando = false;

    public AltaPatrocinioPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        RegistrosSpinner.setModel(new SpinnerNumberModel(0, 0, 100000, 1));
        bronceRadioButton.setSelected(true);

        configurarRenderers();
        cargarEventos();
        cargarInstituciones();

        EventoCBox.addActionListener(e -> seleccionarEvento());
        EdicionCBox.addActionListener(e -> seleccionarEdicion());
        TipoRegistroCBox.addActionListener(e -> actualizarResumen());
        RegistrosSpinner.addChangeListener(e -> actualizarResumen());

        AporteTxt.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizarResumen(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarResumen(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarResumen(); }
        });

        confirmarButton.addActionListener(e -> confirmar());
        cancelarButton.addActionListener(e -> accionCerrar.run());

        actualizarResumen();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void configurarRenderers() {
        EventoCBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEvento ev) setText(ev.getNombre());
                return this;
            }
        });
        EdicionCBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEdicionEvento ed) setText(ed.getNombre());
                return this;
            }
        });
        TipoRegistroCBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTTipoRegistro tr) {
                    setText(tr.getNombre() + "  ($" + tr.getCosto() + ")");
                }
                return this;
            }
        });
    }

    private void cargarEventos() {
        cargando = true;
        EventoCBox.removeAllItems();
        for (DTEvento e : controlador.listarEventos()) {
            EventoCBox.addItem(e);
        }
        EventoCBox.setSelectedIndex(-1);
        cargando = false;
    }

    private void cargarInstituciones() {
        cargando = true;
        InstitucionCBox.removeAllItems();
        for (String nombre : controlador.listarNombresInstituciones()) {
            InstitucionCBox.addItem(nombre);
        }
        InstitucionCBox.setSelectedIndex(-1);
        cargando = false;
    }

    private void seleccionarEvento() {
        if (cargando) return;
        DTEvento evento = (DTEvento) EventoCBox.getSelectedItem();
        cargando = true;
        try {
            EdicionCBox.removeAllItems();
            TipoRegistroCBox.removeAllItems();
            if (evento != null) {
                for (DTEdicionEvento ed : controlador.listarEdicionesDeEvento(evento.getNombre())) {
                    EdicionCBox.addItem(ed);
                }
            }
            EdicionCBox.setSelectedIndex(-1);
            TipoRegistroCBox.setSelectedIndex(-1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudieron cargar las ediciones: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            cargando = false;
            actualizarResumen();
        }
    }

    private void seleccionarEdicion() {
        if (cargando) return;
        DTEdicionEvento edicion = (DTEdicionEvento) EdicionCBox.getSelectedItem();
        cargando = true;
        try {
            TipoRegistroCBox.removeAllItems();
            if (edicion != null) {
                // Ademas de listar, Sistema retiene esta edicion como edicionSeleccionada.
                for (DTTipoRegistro tr : controlador.listarTiposRegistroDeEdicion(edicion.getNombre())) {
                    TipoRegistroCBox.addItem(tr);
                }
            }
            TipoRegistroCBox.setSelectedIndex(-1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudieron cargar los tipos de registro: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            cargando = false;
            actualizarResumen();
        }
    }

    private void actualizarResumen() {
        DTTipoRegistro tipo = (DTTipoRegistro) TipoRegistroCBox.getSelectedItem();
        Double aporte = leerAporte();

        if (tipo == null || aporte == null || aporte <= 0) {
            ResumenLbl.setText("Elegi un tipo de registro y un aporte para ver el calculo.");
            ResumenLbl.setForeground(Color.DARK_GRAY);
            return;
        }

        int cantidad = (Integer) RegistrosSpinner.getValue();
        double valorGratis = tipo.getCosto() * cantidad;
        double tope = aporte * 0.20;

        ResumenLbl.setText(String.format(
                "Valor de los gratis: $%.2f (%d x $%.2f)   ·   Tope 20%%: $%.2f",
                valorGratis, cantidad, tipo.getCosto(), tope));
        ResumenLbl.setForeground(valorGratis > tope
                ? new Color(200, 40, 40)      // rojo: no va a pasar la validacion
                : new Color(30, 130, 60));    // verde: OK
    }

    /** @return el aporte, o null si el texto no es un numero valido */
    private Double leerAporte() {
        try {
            return Double.parseDouble(AporteTxt.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private NivelPatrocinio nivelSeleccionado() {
        if (platinoRadioButton.isSelected()) return NivelPatrocinio.PLATINO;
        if (oroRadioButton.isSelected()) return NivelPatrocinio.ORO;
        if (plataRadioButton.isSelected()) return NivelPatrocinio.PLATA;
        return NivelPatrocinio.BRONCE;
    }

    private void confirmar() {
        DTEvento evento = (DTEvento) EventoCBox.getSelectedItem();
        DTEdicionEvento edicion = (DTEdicionEvento) EdicionCBox.getSelectedItem();
        DTTipoRegistro tipo = (DTTipoRegistro) TipoRegistroCBox.getSelectedItem();
        String institucion = (String) InstitucionCBox.getSelectedItem();

        if (evento == null || edicion == null || tipo == null || institucion == null) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Elegi evento, edicion, tipo de registro e institucion.",
                    TITULO, JOptionPane.WARNING_MESSAGE);
            return;
        }

        int codigo;
        try {
            codigo = Integer.parseInt(CodigoTxt.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "El codigo de patrocinio tiene que ser un numero entero.",
                    TITULO, JOptionPane.WARNING_MESSAGE);
            return;
        }

        Double aporte = leerAporte();
        if (aporte == null || aporte <= 0) {
            JOptionPane.showMessageDialog(mainPanel,
                    "El aporte economico tiene que ser un numero mayor a cero.",
                    TITULO, JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad = (Integer) RegistrosSpinner.getValue();

        DTPatrocinio dt = new DTPatrocinio(codigo, DTFecha.desde(LocalDate.now()), aporte,
                nivelSeleccionado(), cantidad, institucion, tipo.getNombre());

        try {
            // Re-ancla la seleccion en Sistema por si otra ventana interna la cambio.
            controlador.listarEdicionesDeEvento(evento.getNombre());
            controlador.listarTiposRegistroDeEdicion(edicion.getNombre());

            controlador.altaPatrocinio(dt);

            JOptionPane.showMessageDialog(mainPanel,
                    "Patrocinio dado de alta correctamente.",
                    TITULO, JOptionPane.INFORMATION_MESSAGE);
            limpiar();
            accionCerrar.run();

        } catch (ReglaNegocioException ex) {
            // LOOP del caso de uso: se informa y la ventana NO se cierra,
            // el administrador puede editar los datos o cancelar.
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(),
                    "No se pudo dar de alta", JOptionPane.WARNING_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Ocurrio un error inesperado: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        CodigoTxt.setText("");
        AporteTxt.setText("");
        RegistrosSpinner.setValue(0);
        bronceRadioButton.setSelected(true);
        actualizarResumen();
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
        mainPanel.setLayout(new GridLayoutManager(11, 5, new Insets(15, 15, 15, 15), 10, 10));
        final JLabel label1 = new JLabel();
        label1.setText("Alta de Patrocinio");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Evento");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Edición");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EventoCBox = new JComboBox();
        mainPanel.add(EventoCBox, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EdicionCBox = new JComboBox();
        mainPanel.add(EdicionCBox, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Tipo de Registro");
        mainPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TipoRegistroCBox = new JComboBox();
        mainPanel.add(TipoRegistroCBox, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Institución");
        mainPanel.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Código de Patrocinio");
        mainPanel.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Nivel de Patrocinio");
        mainPanel.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CodigoTxt = new JTextField();
        mainPanel.add(CodigoTxt, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        bronceRadioButton = new JRadioButton();
        bronceRadioButton.setText("Bronce");
        mainPanel.add(bronceRadioButton, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        oroRadioButton = new JRadioButton();
        oroRadioButton.setText("Oro");
        mainPanel.add(oroRadioButton, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        plataRadioButton = new JRadioButton();
        plataRadioButton.setText("Plata");
        mainPanel.add(plataRadioButton, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Aporte Económico");
        mainPanel.add(label8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        InstitucionCBox = new JComboBox();
        mainPanel.add(InstitucionCBox, new GridConstraints(4, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Cantidad de Registros");
        mainPanel.add(label9, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        RegistrosSpinner = new JSpinner();
        mainPanel.add(RegistrosSpinner, new GridConstraints(8, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        AporteTxt = new JTextField();
        mainPanel.add(AporteTxt, new GridConstraints(7, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        platinoRadioButton = new JRadioButton();
        platinoRadioButton.setText("Platino");
        mainPanel.add(platinoRadioButton, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        confirmarButton = new JButton();
        confirmarButton.setText("Confirmar");
        mainPanel.add(confirmarButton, new GridConstraints(10, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelarButton = new JButton();
        cancelarButton.setText("Cancelar");
        mainPanel.add(cancelarButton, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ResumenLbl = new JLabel();
        ResumenLbl.setText("Elegí un tipo de registro y un aporte para ver el cálculo");
        mainPanel.add(ResumenLbl, new GridConstraints(9, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(bronceRadioButton);
        buttonGroup.add(oroRadioButton);
        buttonGroup.add(plataRadioButton);
        buttonGroup.add(platinoRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
