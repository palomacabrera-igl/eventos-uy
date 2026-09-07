package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import logica.DTAsistente;
import logica.DTEdicionCompleto;
import logica.DTEdicionEvento;
import logica.DTOrganizador;
import logica.DTRegistro;
import logica.DTUsuario;
import logica.IControladorSistema;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.stream.Collectors;

public class VentanaConsultaUsuario extends JInternalFrame {

    private JPanel panelPrincipal;
    private JComboBox<DTUsuario> comboUsuarios;
    private JTextArea areaDetalle;
    private JPanel panelSeleccion;
    private JPanel panelAsociaciones;
    private JPanel panelEdiciones;
    private JPanel panelRegistros;
    private JList<DTEdicionEvento> listaEdiciones;
    private JList<DTRegistro> listaRegistros;
    private JLabel Usuario;
    private JPanel panelDetalle;
    private final IControladorSistema controlador;
    private CardLayout cardLayout;

    public VentanaConsultaUsuario(IControladorSistema controlador) {
        super("Consulta de Usuario", true, true, true, true);
        this.controlador = controlador;

        setContentPane(panelPrincipal);

        configurarComponentes();
        cargarUsuarios();

        pack();
    }

    private void configurarComponentes() {
        areaDetalle.setEditable(false);

        cardLayout = new CardLayout();
        panelAsociaciones.setLayout(cardLayout);

        // Los paneles fueron creados dentro de panelAsociaciones en el .form.
        // Se re-agregan con los nombres que utilizará CardLayout.
        panelAsociaciones.removeAll();
        panelAsociaciones.add(panelEdiciones, "EDICIONES");
        panelAsociaciones.add(panelRegistros, "REGISTROS");

        comboUsuarios.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof DTUsuario usuario) {
                    String tipo = usuario instanceof DTOrganizador
                            ? "Organizador"
                            : "Asistente";

                    setText(usuario.getNickname() + " (" + tipo + ")");
                }

                return this;
            }
        });

        listaEdiciones.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof DTEdicionEvento edicion) {
                    setText(edicion.getNombre());
                }

                return this;
            }
        });

        listaRegistros.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof DTRegistro registro) {
                    setText(registro.getNombreEdicion()
                            + " — " + registro.getTipoRegistro());
                }

                return this;
            }
        });

        comboUsuarios.addActionListener(e -> seleccionarUsuario());

        listaEdiciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarEdicion();
            }
        });

        listaRegistros.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarRegistro();
            }
        });
    }

    private void cargarUsuarios() {
        comboUsuarios.removeAllItems();

        controlador.listarUsuarios().stream()
                .sorted(Comparator.comparing(DTUsuario::getNickname))
                .forEach(comboUsuarios::addItem);
    }

    private void seleccionarUsuario() {
        DTUsuario usuario = (DTUsuario) comboUsuarios.getSelectedItem();

        if (usuario == null) {
            return;
        }

        DTUsuario datos = controlador.seleccionarUsuario(usuario.getNickname());

        if (datos instanceof DTOrganizador organizador) {
            areaDetalle.setText(
                    "Nickname: " + organizador.getNickname()
                            + "\nNombre: " + organizador.getNombre()
                            + "\nCorreo: " + organizador.getCorreo()
                            + "\nDescripción: " + organizador.getDescripcion()
                            + "\nSitio web: " + organizador.getSitioWeb()
            );

            listaRegistros.setListData(new DTRegistro[0]);
            listaEdiciones.setListData(
                    controlador.listarEdiciones().toArray(new DTEdicionEvento[0])
            );

            cardLayout.show(panelAsociaciones, "EDICIONES");

        } else if (datos instanceof DTAsistente asistente) {
            areaDetalle.setText(
                    "Nickname: " + asistente.getNickname()
                            + "\nNombre: " + asistente.getNombre()
                            + "\nApellido: " + asistente.getApellido()
                            + "\nCorreo: " + asistente.getCorreo()
                            + "\nFecha de nacimiento: "
                            + asistente.getFechaNacimiento().aLocalDate()
            );

            listaEdiciones.setListData(new DTEdicionEvento[0]);
            listaRegistros.setListData(
                    controlador.listarRegistroUsuario(asistente.getNickname())
                            .toArray(new DTRegistro[0])
            );

            cardLayout.show(panelAsociaciones, "REGISTROS");
        }
    }

    private void mostrarEdicion() {
        DTEdicionEvento item = listaEdiciones.getSelectedValue();

        if (item == null) {
            return;
        }

        DTEdicionCompleto edicion =
                controlador.seleccionarEdicion(item.getNombre());

        DTOrganizador org = edicion.getOrganizador();
        String organizador = org == null
                ? "(sin organizador)"
                : org.getNombre() + " (" + org.getNickname() + ")";

        String tiposRegistro = edicion.getTiposRegistro().isEmpty()
                ? "Sin tipos de registro"
                : edicion.getTiposRegistro().stream()
                .map(t -> t.getNombre() + " - $" + t.getCosto() + " (cupo " + t.getCupo() + ")")
                .collect(Collectors.joining("\n- ", "- ", ""));

        String registros = edicion.getRegistros().isEmpty()
                ? "Sin registros"
                : edicion.getRegistros().stream()
                .map(r -> r.getTipoRegistro() + " - $" + r.getCosto() + " - " + r.getFechaRegistro())
                .collect(Collectors.joining("\n- ", "- ", ""));

        String patrocinios = edicion.getPatrocinios().isEmpty()
                ? "Sin patrocinios"
                : edicion.getPatrocinios().stream()
                .map(p -> p.getInstitucion() + " (" + p.getNivel() + ")")
                .collect(Collectors.joining("\n- ", "- ", ""));

        areaDetalle.setText(
                "Edición: " + edicion.getNombre()
                        + "\nSigla: " + edicion.getSigla()
                        + "\nFecha de alta: " + edicion.getFechaAlta()
                        + "\nInicio: " + edicion.getFechaIni()
                        + "\nFin: " + edicion.getFechaFin()
                        + "\nCiudad: " + edicion.getCiudad()
                        + "\nPaís: " + edicion.getPais()
                        + "\nOrganizador: " + organizador
                        + "\n\nTipos de registro:\n" + tiposRegistro
                        + "\n\nRegistros:\n" + registros
                        + "\n\nPatrocinios:\n" + patrocinios
        );
    }

    private void mostrarRegistro() {
        DTRegistro item = listaRegistros.getSelectedValue();

        if (item == null) {
            return;
        }

        DTRegistro registro =
                controlador.obtenerRegistro(item.getNombreEdicion());

        areaDetalle.setText(
                "Edición: " + registro.getNombreEdicion()
                        + "\nTipo de registro: " + registro.getTipoRegistro()
                        + "\nCosto: " + registro.getCosto()
                        + "\nFecha de registro: " + registro.getFechaRegistro()
        );
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
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelAsociaciones = new JPanel();
        panelAsociaciones.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panelAsociaciones, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelEdiciones = new JPanel();
        panelEdiciones.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelAsociaciones.add(panelEdiciones, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Ediciones Organizadas:");
        panelEdiciones.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panelEdiciones.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelEdiciones.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelEdiciones.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        listaEdiciones = new JList();
        scrollPane1.setViewportView(listaEdiciones);
        final Spacer spacer3 = new Spacer();
        panelAsociaciones.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panelAsociaciones.add(spacer4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelRegistros = new JPanel();
        panelRegistros.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelAsociaciones.add(panelRegistros, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Registros a Ediciones: ");
        panelRegistros.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panelRegistros.add(spacer5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panelRegistros.add(spacer6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panelRegistros.add(scrollPane2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        listaRegistros = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        listaRegistros.setModel(defaultListModel1);
        scrollPane2.setViewportView(listaRegistros);
        panelSeleccion = new JPanel();
        panelSeleccion.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panelSeleccion, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Usuario = new JLabel();
        Usuario.setText("Usuario:");
        panelSeleccion.add(Usuario, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panelSeleccion.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        comboUsuarios = new JComboBox();
        panelSeleccion.add(comboUsuarios, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelDetalle = new JPanel();
        panelDetalle.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panelDetalle, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panelDetalle.add(scrollPane3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        areaDetalle = new JTextArea();
        scrollPane3.setViewportView(areaDetalle);
        final JLabel label3 = new JLabel();
        label3.setText("Datos del Usuario");
        panelDetalle.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelPrincipal;
    }
}