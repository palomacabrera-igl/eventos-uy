package presentacion;

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
    private JPanel PanelDetalle;
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

    areaDetalle.setText(
            "Edición: " + edicion.getNombre()
                    + "\nSigla: " + edicion.getSigla()
                    + "\nFecha de alta: " + edicion.getFechaAlta()
                    + "\nInicio: " + edicion.getFechaIni()
                    + "\nFin: " + edicion.getFechaFin()
                    + "\nCiudad: " + edicion.getCiudad()
                    + "\nPaís: " + edicion.getPais()
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
}