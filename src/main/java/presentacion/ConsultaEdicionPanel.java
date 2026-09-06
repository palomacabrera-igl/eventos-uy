package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import logica.DTEdicionCompleto;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTPatrocinio;
import logica.DTRegistro;
import logica.DTTipoRegistro;
import logica.Fabrica;
import logica.IControladorSistema;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ConsultaEdicionPanel {
    private JPanel mainPanel;
    private JPanel SeleccionPanel;
    private JPanel DatosPanel;
    private JTabbedPane ColeccionesTPane;
    private JPanel BotonesPanel;
    private JComboBox EventosCBox;
    private JComboBox EdicionesCBox;
    private JTextField NombreTxt;
    private JTextField SiglaTxt;
    private JTextField InicioTxt;
    private JTextField AltaTxt;
    private JTextField CiudadTxt;
    private JTextField PaisTxt;
    private JTextField FinTxt;
    private JTextField OrganizadorTxt;
    private JList TiposList;
    private JPanel DetalleTipoPanel;
    private JTextField TRNombreTxt;
    private JTextField TRDescripcionTxt;
    private JTextField TRCostoTxt;
    private JTextField TRCupoTxt;
    private JList RegistrosList;
    private JList PatrociniosList;
    private JTextField PatCodigoTxt;
    private JTextField PatInstitucionTxt;
    private JTextField PatTipoRegistroTxt;
    private JTextField PatNivelTxt;
    private JTextField PatAporteTxt;
    private JTextField PatCantGratisTxt;
    private JTextField PatFechaTxt;
    private JButton CerrarButton;
    private JPanel DetallePatrocinioPanel;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    /** Ignora los eventos que dispara el propio codigo al recargar los combos. */
    private boolean cargando = false;

    public ConsultaEdicionPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        configurarCamposSoloLectura();
        configurarRenderers();

        TiposList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        PatrociniosList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cargarEventos();

        EventosCBox.addActionListener(e -> seleccionarEvento());
        EdicionesCBox.addActionListener(e -> seleccionarEdicion());

        TiposList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarTipoRegistro();
        });
        PatrociniosList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarPatrocinio();
        });

        CerrarButton.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void configurarCamposSoloLectura() {
        for (JTextField f : new JTextField[]{
                NombreTxt, SiglaTxt, CiudadTxt, PaisTxt, InicioTxt, FinTxt, AltaTxt, OrganizadorTxt,
                TRNombreTxt, TRDescripcionTxt, TRCostoTxt, TRCupoTxt,
                PatCodigoTxt, PatInstitucionTxt, PatTipoRegistroTxt, PatNivelTxt,
                PatAporteTxt, PatCantGratisTxt, PatFechaTxt}) {
            f.setEditable(false);
            f.setBackground(Color.LIGHT_GRAY);
        }
    }
    private void configurarRenderers() {
        EventosCBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEvento ev) setText(ev.getNombre());
                return this;
            }
        });

        EdicionesCBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEdicionEvento ed) setText(ed.getNombre());
                return this;
            }
        });

        TiposList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTTipoRegistro tr) setText(tr.getNombre());
                return this;
            }
        });

        RegistrosList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTRegistro r)
                    setText(r.getNickname() + "  —  " + r.getTipoRegistro() + "  —  $" + r.getCosto() + "  —  " + formatear(r.getFechaRegistro()));
                return this;
            }
        });

        PatrociniosList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTPatrocinio p) setText(p.getCodigoPatrocinio() + " - " + p.getInstitucion());
                return this;
            }
        });
    }

    // ===== Carga y cascada =====

    private void cargarEventos() {
        cargando = true;
        EventosCBox.removeAllItems();
        for (DTEvento e : controlador.listarEventos()) {
            EventosCBox.addItem(e);
        }
        EventosCBox.setSelectedIndex(-1);
        cargando = false;
    }

    private void seleccionarEvento() {
        if (cargando) return;
        DTEvento evento = (DTEvento) EventosCBox.getSelectedItem();
        cargando = true;
        try {
            EdicionesCBox.removeAllItems();
            limpiarTodo();
            if (evento != null) {
                for (DTEdicionEvento ed : controlador.listarEdicionesDeEvento(evento.getNombre())) {
                    EdicionesCBox.addItem(ed);
                }
            }
            EdicionesCBox.setSelectedIndex(-1);
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
        DTEdicionEvento sel = (DTEdicionEvento) EdicionesCBox.getSelectedItem();
        if (sel == null) {
            limpiarTodo();
            return;
        }
        try {
            DTEdicionCompleto ed = controlador.seleccionarEdicionCompleta(sel.getNombre());

            NombreTxt.setText(ed.getNombre());
            SiglaTxt.setText(ed.getSigla());
            CiudadTxt.setText(ed.getCiudad());
            PaisTxt.setText(ed.getPais());
            InicioTxt.setText(formatear(ed.getFechaIni()));
            FinTxt.setText(formatear(ed.getFechaFin()));
            AltaTxt.setText(formatear(ed.getFechaAlta()));
            OrganizadorTxt.setText(ed.getOrganizador().getNickname());

            TiposList.setListData(ed.getTiposRegistro().stream()
                    .sorted(Comparator.comparing(DTTipoRegistro::getNombre))
                    .toArray(DTTipoRegistro[]::new));

            RegistrosList.setListData(ed.getRegistros().stream()
                    .sorted(Comparator.comparing(DTRegistro::getNickname))
                    .toArray(DTRegistro[]::new));

            PatrociniosList.setListData(ed.getPatrocinios().stream()
                    .sorted(Comparator.comparingInt(DTPatrocinio::getCodigoPatrocinio))
                    .toArray(DTPatrocinio[]::new));

        } catch (Exception ex) {
            limpiarTodo();
            JOptionPane.showMessageDialog(mainPanel,
                    "No se pudo cargar la edicion: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Detalles =====

    private void mostrarTipoRegistro() {
        DTTipoRegistro tr = (DTTipoRegistro) TiposList.getSelectedValue();
        if (tr == null) {
            limpiarDetalleTipo();
            return;
        }
        TRNombreTxt.setText(tr.getNombre());
        TRDescripcionTxt.setText(tr.getDescripcion());
        TRCostoTxt.setText(String.valueOf(tr.getCosto()));
        TRCupoTxt.setText(String.valueOf(tr.getCupo()));
    }

    private void mostrarPatrocinio() {
        DTPatrocinio p = (DTPatrocinio) PatrociniosList.getSelectedValue();
        if (p == null) {
            limpiarDetallePatrocinio();
            return;
        }
        PatCodigoTxt.setText(String.valueOf(p.getCodigoPatrocinio()));
        PatInstitucionTxt.setText(p.getInstitucion());
        PatTipoRegistroTxt.setText(p.getTipoRegistro());
        PatNivelTxt.setText(p.getNivel().toString());
        PatAporteTxt.setText(String.valueOf(p.getMonto()));
        PatCantGratisTxt.setText(String.valueOf(p.getCantRegistrosGratis()));
        DTFecha f = p.getFecha();
        PatFechaTxt.setText(f.getDia() + "/" + f.getMes() + "/" + f.getAnio());
    }

    // ===== Helpers =====

    private static String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO);
    }

    private void limpiarDatosEdicion() {
        NombreTxt.setText("");
        SiglaTxt.setText("");
        CiudadTxt.setText("");
        PaisTxt.setText("");
        InicioTxt.setText("");
        FinTxt.setText("");
        AltaTxt.setText("");
        OrganizadorTxt.setText("");
    }

    private void limpiarDetalleTipo() {
        TRNombreTxt.setText("");
        TRDescripcionTxt.setText("");
        TRCostoTxt.setText("");
        TRCupoTxt.setText("");
    }

    private void limpiarDetallePatrocinio() {
        PatCodigoTxt.setText("");
        PatInstitucionTxt.setText("");
        PatTipoRegistroTxt.setText("");
        PatNivelTxt.setText("");
        PatAporteTxt.setText("");
        PatCantGratisTxt.setText("");
        PatFechaTxt.setText("");
    }

    private void limpiarListas() {
        TiposList.setListData(new DTTipoRegistro[0]);
        RegistrosList.setListData(new DTRegistro[0]);
        PatrociniosList.setListData(new DTPatrocinio[0]);
    }

    private void limpiarTodo() {
        limpiarDatosEdicion();
        limpiarListas();
        limpiarDetalleTipo();
        limpiarDetallePatrocinio();
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
        mainPanel.setLayout(new GridLayoutManager(4, 1, new Insets(15, 15, 15, 15), 10, 10));
        SeleccionPanel = new JPanel();
        SeleccionPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(SeleccionPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Evento:");
        SeleccionPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EventosCBox = new JComboBox();
        SeleccionPanel.add(EventosCBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(300, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Edición:");
        SeleccionPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EdicionesCBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        EdicionesCBox.setModel(defaultComboBoxModel1);
        SeleccionPanel.add(EdicionesCBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(300, -1), null, 0, false));
        DatosPanel = new JPanel();
        DatosPanel.setLayout(new GridLayoutManager(4, 4, new Insets(8, 8, 8, 8), 6, 6));
        mainPanel.add(DatosPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DatosPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Datos de la Edición", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("Nombre");
        DatosPanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        NombreTxt = new JTextField();
        NombreTxt.setText("");
        DatosPanel.add(NombreTxt, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Sigla");
        DatosPanel.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SiglaTxt = new JTextField();
        SiglaTxt.setText("");
        DatosPanel.add(SiglaTxt, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Inicio");
        DatosPanel.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        InicioTxt = new JTextField();
        DatosPanel.add(InicioTxt, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Alta");
        DatosPanel.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        AltaTxt = new JTextField();
        DatosPanel.add(AltaTxt, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Ciudad");
        DatosPanel.add(label7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("País");
        DatosPanel.add(label8, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Fin");
        DatosPanel.add(label9, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Organizador");
        DatosPanel.add(label10, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CiudadTxt = new JTextField();
        DatosPanel.add(CiudadTxt, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        PaisTxt = new JTextField();
        DatosPanel.add(PaisTxt, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        FinTxt = new JTextField();
        FinTxt.setText("");
        DatosPanel.add(FinTxt, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        OrganizadorTxt = new JTextField();
        DatosPanel.add(OrganizadorTxt, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(180, -1), null, 0, false));
        ColeccionesTPane = new JTabbedPane();
        mainPanel.add(ColeccionesTPane, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        ColeccionesTPane.addTab("Tipos de Registro", panel1);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 160), null, 0, false));
        TiposList = new JList();
        scrollPane1.setViewportView(TiposList);
        DetalleTipoPanel = new JPanel();
        DetalleTipoPanel.setLayout(new GridLayoutManager(5, 2, new Insets(8, 8, 8, 8), 6, 6));
        panel1.add(DetalleTipoPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        DetalleTipoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Detalle del Tipo de Registro", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label11 = new JLabel();
        label11.setText("Nombre");
        DetalleTipoPanel.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TRNombreTxt = new JTextField();
        DetalleTipoPanel.add(TRNombreTxt, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Descripción");
        DetalleTipoPanel.add(label12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TRDescripcionTxt = new JTextField();
        DetalleTipoPanel.add(TRDescripcionTxt, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Costo");
        DetalleTipoPanel.add(label13, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TRCostoTxt = new JTextField();
        DetalleTipoPanel.add(TRCostoTxt, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Cupo");
        DetalleTipoPanel.add(label14, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TRCupoTxt = new JTextField();
        DetalleTipoPanel.add(TRCupoTxt, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        DetalleTipoPanel.add(spacer1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        ColeccionesTPane.addTab("Registros", panel2);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(460, 160), null, 0, false));
        RegistrosList = new JList();
        scrollPane2.setViewportView(RegistrosList);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        ColeccionesTPane.addTab("Patrocinios", panel3);
        final JScrollPane scrollPane3 = new JScrollPane();
        panel3.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 160), null, 0, false));
        PatrociniosList = new JList();
        scrollPane3.setViewportView(PatrociniosList);
        DetallePatrocinioPanel = new JPanel();
        DetallePatrocinioPanel.setLayout(new GridLayoutManager(8, 2, new Insets(8, 8, 8, 8), 6, 6));
        panel3.add(DetallePatrocinioPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        DetallePatrocinioPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Detalle del Patrocinio", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label15 = new JLabel();
        label15.setText("Código");
        DetallePatrocinioPanel.add(label15, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PatCodigoTxt = new JTextField();
        DetallePatrocinioPanel.add(PatCodigoTxt, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Institución");
        DetallePatrocinioPanel.add(label16, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PatInstitucionTxt = new JTextField();
        DetallePatrocinioPanel.add(PatInstitucionTxt, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Tipo de Registro");
        DetallePatrocinioPanel.add(label17, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PatTipoRegistroTxt = new JTextField();
        DetallePatrocinioPanel.add(PatTipoRegistroTxt, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Nivel");
        DetallePatrocinioPanel.add(label18, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PatNivelTxt = new JTextField();
        DetallePatrocinioPanel.add(PatNivelTxt, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("Aporte");
        DetallePatrocinioPanel.add(label19, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PatAporteTxt = new JTextField();
        DetallePatrocinioPanel.add(PatAporteTxt, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("Registros gratis");
        DetallePatrocinioPanel.add(label20, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PatCantGratisTxt = new JTextField();
        DetallePatrocinioPanel.add(PatCantGratisTxt, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("Fecha");
        DetallePatrocinioPanel.add(label21, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PatFechaTxt = new JTextField();
        PatFechaTxt.setText("");
        DetallePatrocinioPanel.add(PatFechaTxt, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        DetallePatrocinioPanel.add(spacer2, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        BotonesPanel = new JPanel();
        BotonesPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(BotonesPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        BotonesPanel.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        CerrarButton = new JButton();
        CerrarButton.setText("Cerrar");
        BotonesPanel.add(CerrarButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
