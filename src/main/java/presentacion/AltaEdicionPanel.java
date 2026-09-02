package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTOrganizador;
import logica.Fabrica;
import logica.IControladorSistema;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AltaEdicionPanel {

    // ===== Atados al .form =====
    private JPanel mainPanel;
    private JComboBox comboEventos;
    private JComboBox comboOrganizadores;
    private JTextField campoNombre;
    private JTextField campoSigla;
    private JTextField campoCiudad;
    private JTextField campoPais;
    private JSpinner spinnerDiaInicio;
    private JSpinner spinnerMesInicio;
    private JSpinner spinnerAnioInicio;
    private JSpinner spinnerDiaFin;
    private JSpinner spinnerMesFin;
    private JSpinner spinnerAnioFin;
    private JSpinner spinnerDiaAlta;
    private JSpinner spinnerMesAlta;
    private JSpinner spinnerAnioAlta;
    private JButton botonAceptar;
    private JButton botonCancelar;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    public AltaEdicionPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        rango(spinnerDiaInicio, 1, 1, 31);
        rango(spinnerMesInicio, 1, 1, 12);
        rango(spinnerAnioInicio, 2026, 2020, 2035);
        rango(spinnerDiaFin, 1, 1, 31);
        rango(spinnerMesFin, 1, 1, 12);
        rango(spinnerAnioFin, 2026, 2020, 2035);
        rango(spinnerDiaAlta, 1, 1, 31);
        rango(spinnerMesAlta, 1, 1, 12);
        rango(spinnerAnioAlta, 2026, 2020, 2035);

        configurarRenderers();
        cargarEventosYOrganizadores();

        comboEventos.addActionListener(e -> seleccionarEvento());
        comboOrganizadores.addActionListener(e -> seleccionarOrganizador());
        botonAceptar.addActionListener(e -> aceptar());
        botonCancelar.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private static void rango(JSpinner spinner, int valor, int min, int max) {
        spinner.setModel(new SpinnerNumberModel(valor, min, max, 1));
    }

    private void configurarRenderers() {
        comboEventos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEvento ev) setText(ev.getNombre());
                return this;
            }
        });
        comboOrganizadores.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTOrganizador o) {
                    setText(o.getNombre() + " (" + o.getNickname() + ")");
                }
                return this;
            }
        });
    }

    private void cargarEventosYOrganizadores() {
        try {
            for (DTEvento e : controlador.listarEventos()) {
                comboEventos.addItem(e);
            }
            for (DTOrganizador o : controlador.listarOrganizadores()) {
                comboOrganizadores.addItem(o);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudieron cargar eventos u organizadores: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        comboEventos.setSelectedIndex(-1);
        comboOrganizadores.setSelectedIndex(-1);
    }

    private void seleccionarEvento() {
        DTEvento evento = (DTEvento) comboEventos.getSelectedItem();
        if (evento == null) return;
        try {
            // el Sistema retiene el evento seleccionado (precondicion de ingresarDatosEdicion)
            controlador.seleccionarEvento(evento.getNombre());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudo seleccionar el evento: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarOrganizador() {
        DTOrganizador organizador = (DTOrganizador) comboOrganizadores.getSelectedItem();
        if (organizador == null) return;
        try {
            controlador.seleccionarOrganizador(organizador.getNickname());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudo seleccionar el organizador: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aceptar() {
        DTEvento evento = (DTEvento) comboEventos.getSelectedItem();
        DTOrganizador organizador = (DTOrganizador) comboOrganizadores.getSelectedItem();
        if (evento == null || organizador == null) {
            JOptionPane.showMessageDialog(mainPanel, "Elegí un evento y un organizador.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = campoNombre.getText().trim();
        String sigla = campoSigla.getText().trim();
        String ciudad = campoCiudad.getText().trim();
        String pais = campoPais.getText().trim();
        if (nombre.isEmpty() || sigla.isEmpty() || ciudad.isEmpty() || pais.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Completá nombre, sigla, ciudad y país.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DTFecha fechaInicio = fechaDe(spinnerDiaInicio, spinnerMesInicio, spinnerAnioInicio);
            DTFecha fechaFin = fechaDe(spinnerDiaFin, spinnerMesFin, spinnerAnioFin);
            DTFecha fechaAlta = fechaDe(spinnerDiaAlta, spinnerMesAlta, spinnerAnioAlta);

            DTEdicionEvento dt = new DTEdicionEvento(nombre, sigla, ciudad, pais,
                    fechaInicio, fechaFin, fechaAlta);

            if (controlador.ingresarDatosEdicion(dt)) {
                JOptionPane.showMessageDialog(mainPanel,
                        "Edición de evento dada de alta correctamente.",
                        "Alta de Edición de Evento", JOptionPane.INFORMATION_MESSAGE);
                accionCerrar.run();
            } else {
                // LOOP del DSS: el nombre ya existe -> se avisa y la ventana NO se cierra
                JOptionPane.showMessageDialog(mainPanel,
                        "Ya existe una edición con el nombre \"" + nombre + "\". Elegí otro.",
                        "Nombre en uso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Ocurrió un error al dar de alta la edición: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static DTFecha fechaDe(JSpinner dia, JSpinner mes, JSpinner anio) {
        return new DTFecha((int) dia.getValue(), (int) mes.getValue(), (int) anio.getValue());
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
        mainPanel.setLayout(new GridLayoutManager(12, 3, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Evento:");
        mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Organizador:");
        mainPanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Nombre de la edicion:");
        mainPanel.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Sigla:");
        mainPanel.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Ciudad:");
        mainPanel.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Pais:");
        mainPanel.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Fecha de inicio:");
        mainPanel.add(label7, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Fecha de fin:");
        mainPanel.add(label8, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoNombre = new JTextField();
        mainPanel.add(campoNombre, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Fecha de alta:");
        mainPanel.add(label9, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboEventos = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        comboEventos.setModel(defaultComboBoxModel1);
        mainPanel.add(comboEventos, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboOrganizadores = new JComboBox();
        mainPanel.add(comboOrganizadores, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoSigla = new JTextField();
        mainPanel.add(campoSigla, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoCiudad = new JTextField();
        mainPanel.add(campoCiudad, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoPais = new JTextField();
        mainPanel.add(campoPais, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        spinnerDiaInicio = new JSpinner();
        panel1.add(spinnerDiaInicio, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerMesInicio = new JSpinner();
        panel1.add(spinnerMesInicio, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerAnioInicio = new JSpinner();
        panel1.add(spinnerAnioInicio, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        spinnerDiaFin = new JSpinner();
        panel2.add(spinnerDiaFin, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerMesFin = new JSpinner();
        panel2.add(spinnerMesFin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerAnioFin = new JSpinner();
        panel2.add(spinnerAnioFin, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new GridConstraints(9, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        spinnerDiaAlta = new JSpinner();
        panel3.add(spinnerDiaAlta, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerMesAlta = new JSpinner();
        panel3.add(spinnerMesAlta, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerAnioAlta = new JSpinner();
        panel3.add(spinnerAnioAlta, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        botonAceptar = new JButton();
        botonAceptar.setText("Aceptar");
        mainPanel.add(botonAceptar, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        botonCancelar = new JButton();
        botonCancelar.setText("Cancelar");
        mainPanel.add(botonCancelar, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(11, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Alta Edicion de Evento");
        mainPanel.add(label10, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
