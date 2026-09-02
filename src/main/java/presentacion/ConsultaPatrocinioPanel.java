package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTPatrocinio;
import logica.Fabrica;
import logica.IControladorSistema;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class ConsultaPatrocinioPanel {

    // ===== Atados al .form =====
    private JPanel mainPanel;
    private JComboBox comboEventos;
    private JComboBox comboEdiciones;
    private JComboBox comboPatrocinios;
    private JPanel panelDetalle;
    private JTextField campoCodigo;
    private JTextField campoInstitucion;
    private JTextField campoTipoRegistro;
    private JTextField campoNivel;
    private JTextField campoMonto;
    private JTextField campoCantRegistrosGratis;
    private JTextField campoFecha;
    private JButton botonCerrar;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    /**
     * Bandera para ignorar los eventos que disparamos NOSOTROS al recargar los combos.
     */
    private boolean cargando = false;

    public ConsultaPatrocinioPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        for (JTextField f : new JTextField[]{campoCodigo, campoInstitucion, campoTipoRegistro,
                campoNivel, campoMonto, campoCantRegistrosGratis, campoFecha}) {
            f.setEditable(false);
            f.setBackground(Color.LIGHT_GRAY);
        }

        configurarRenderers();
        cargarEventos();

        comboEventos.addActionListener(e -> seleccionarEvento());
        comboEdiciones.addActionListener(e -> seleccionarEdicion());
        comboPatrocinios.addActionListener(e -> mostrarPatrocinio());
        botonCerrar.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
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
        comboEdiciones.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEdicionEvento ed) setText(ed.getNombre());
                return this;
            }
        });
        comboPatrocinios.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTPatrocinio p) {
                    setText(p.getCodigoPatrocinio() + " - " + p.getInstitucion());
                }
                return this;
            }
        });
    }

    private void cargarEventos() {
        cargando = true;
        comboEventos.removeAllItems();
        for (DTEvento e : controlador.listarEventos()) {
            comboEventos.addItem(e);
        }
        comboEventos.setSelectedIndex(-1);   // arranca sin elegir
        cargando = false;
    }

    private void seleccionarEvento() {
        if (cargando) return;
        DTEvento evento = (DTEvento) comboEventos.getSelectedItem();
        cargando = true;
        try {
            comboEdiciones.removeAllItems();
            comboPatrocinios.removeAllItems();
            limpiarDetalle();
            if (evento != null) {
                for (DTEdicionEvento ed : controlador.listarEdicionesDeEvento(evento.getNombre())) {
                    comboEdiciones.addItem(ed);
                }
            }
            comboEdiciones.setSelectedIndex(-1);
            comboPatrocinios.setSelectedIndex(-1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudieron cargar las ediciones: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            cargando = false;
        }
    }

    private void seleccionarEdicion() {
        if (cargando) return;
        DTEdicionEvento edicion = (DTEdicionEvento) comboEdiciones.getSelectedItem();
        cargando = true;
        try {
            comboPatrocinios.removeAllItems();
            limpiarDetalle();
            if (edicion != null) {
                for (DTPatrocinio p : controlador.listarPatrociniosDeEdicion(edicion.getNombre())) {
                    comboPatrocinios.addItem(p);
                }
            }
            comboPatrocinios.setSelectedIndex(-1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudieron cargar los patrocinios: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            cargando = false;
        }
    }

    private void mostrarPatrocinio() {
        if (cargando) return;
        DTPatrocinio seleccionado = (DTPatrocinio) comboPatrocinios.getSelectedItem();
        if (seleccionado == null) {
            limpiarDetalle();
            return;
        }
        try {
            DTPatrocinio detalle = controlador.mostrarPatrocinio(seleccionado.getCodigoPatrocinio());
            campoCodigo.setText(String.valueOf(detalle.getCodigoPatrocinio()));
            campoInstitucion.setText(detalle.getInstitucion());
            campoTipoRegistro.setText(detalle.getTipoRegistro());
            campoNivel.setText(detalle.getNivel().toString());
            campoMonto.setText(String.valueOf(detalle.getMonto()));
            campoCantRegistrosGratis.setText(String.valueOf(detalle.getCantRegistrosGratis()));
            DTFecha fecha = detalle.getFecha();
            campoFecha.setText(fecha.getDia() + "/" + fecha.getMes() + "/" + fecha.getAnio());
        } catch (Exception ex) {
            limpiarDetalle();
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudo cargar el patrocinio: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarDetalle() {
        campoCodigo.setText("");
        campoInstitucion.setText("");
        campoTipoRegistro.setText("");
        campoNivel.setText("");
        campoMonto.setText("");
        campoCantRegistrosGratis.setText("");
        campoFecha.setText("");
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
        mainPanel.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Evento:");
        mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboEventos = new JComboBox();
        mainPanel.add(comboEventos, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Edicion:");
        mainPanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboEdiciones = new JComboBox();
        mainPanel.add(comboEdiciones, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Patrocinio:");
        mainPanel.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboPatrocinios = new JComboBox();
        mainPanel.add(comboPatrocinios, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelDetalle = new JPanel();
        panelDetalle.setLayout(new GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panelDetalle, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelDetalle.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-3025959)), "Detalle del Patrocinio:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, panelDetalle.getFont()), new Color(-15329250)));
        final JLabel label4 = new JLabel();
        label4.setText("Codigo:");
        panelDetalle.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panelDetalle.add(spacer1, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Institucion:");
        panelDetalle.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Tipo de Registro:");
        panelDetalle.add(label6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Nivel:");
        panelDetalle.add(label7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Monto:");
        panelDetalle.add(label8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Registros gratis:");
        panelDetalle.add(label9, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Fecha:");
        panelDetalle.add(label10, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoCodigo = new JTextField();
        panelDetalle.add(campoCodigo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoInstitucion = new JTextField();
        panelDetalle.add(campoInstitucion, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoTipoRegistro = new JTextField();
        panelDetalle.add(campoTipoRegistro, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoNivel = new JTextField();
        panelDetalle.add(campoNivel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoMonto = new JTextField();
        panelDetalle.add(campoMonto, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoCantRegistrosGratis = new JTextField();
        panelDetalle.add(campoCantRegistrosGratis, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoFecha = new JTextField();
        panelDetalle.add(campoFecha, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        botonCerrar = new JButton();
        botonCerrar.setHideActionText(false);
        botonCerrar.setHorizontalTextPosition(0);
        botonCerrar.setText("Cerrar");
        panelDetalle.add(botonCerrar, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Consulta de Patrocinio");
        mainPanel.add(label11, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}