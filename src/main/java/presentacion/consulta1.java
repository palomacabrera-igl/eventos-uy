/*package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import logica.DTAsistente;
import logica.DTFecha;
import logica.DTOrganizador;
import logica.DTUsuario;
import logica.Fabrica;
import logica.DTRegistro;
import logica.DTTipoRegistro;
import logica.DTEdicionCompleto;
import logica.DTRegistro;
import logica.IControladorSistema;


import logica.*;
import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class VentanaConsultaUsuario extends JInternalFrame {

    private final IControladorSistema controlador;

    private JComboBox<DTUsuario> comboUsuarios;
    private JComboBox<DTEdicionEvento> comboEdiciones;
    private JComboBox<DTRegistro> comboRegistros;

    private JTextField campoNickname, campoNombre, campoApellido, campoCorreo, campoTipo;
    private JTextArea campoDetalle;

    private DTUsuario usuarioActual;

    public VentanaConsultaUsuario(IControladorSistema controlador) {
        super("Consulta de Usuario", true, true, true, true);
        this.controlador = controlador;
        construirUI();
        cargarUsuarios();
        setSize(500, 500);
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        // Panel selección de usuario
        JPanel panelSeleccion = new JPanel();
        panelSeleccion.setLayout(new BoxLayout(panelSeleccion, BoxLayout.Y_AXIS));

        comboUsuarios = new JComboBox<>();
        comboUsuarios.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTUsuario u) setText(u.getNickname());
                return this;
            }
        });
        comboUsuarios.addActionListener(e -> mostrarUsuario());
        panelSeleccion.add(construirFila("Usuario:", comboUsuarios));

        add(panelSeleccion, BorderLayout.NORTH);

        // Panel detalle
        JPanel panelDetalle = new JPanel();
        panelDetalle.setLayout(new BoxLayout(panelDetalle, BoxLayout.Y_AXIS));
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));

        campoNickname = campoSoloLectura();
        campoNombre = campoSoloLectura();
        campoApellido = campoSoloLectura();
        campoCorreo = campoSoloLectura();
        campoTipo = campoSoloLectura();

        panelDetalle.add(construirFila("Nickname:", campoNickname));
        panelDetalle.add(construirFila("Nombre:", campoNombre));
        panelDetalle.add(construirFila("Apellido:", campoApellido));
        panelDetalle.add(construirFila("Correo:", campoCorreo));
        panelDetalle.add(construirFila("Tipo:", campoTipo));

        add(panelDetalle, BorderLayout.CENTER);

        // Panel inferior dinámico
        campoDetalle = new JTextArea();
        campoDetalle.setEditable(false);
        add(new JScrollPane(campoDetalle), BorderLayout.SOUTH);
    }

    private JPanel construirFila(String etiqueta, JComponent campo) {
        JPanel fila = new JPanel(new BorderLayout(5, 0));
        fila.add(new JLabel(etiqueta), BorderLayout.WEST);
        fila.add(campo, BorderLayout.CENTER);
        return fila;
    }

    private JTextField campoSoloLectura() {
        JTextField campo = new JTextField();
        campo.setEditable(false);
        campo.setBackground(Color.LIGHT_GRAY);
        return campo;
    }

    private void cargarUsuarios() {
        Set<DTUsuario> usuarios = controlador.listarUsuarios();
        for (DTUsuario u : usuarios) comboUsuarios.addItem(u);
    }

    private void seleccionarUsuario() {
        String nickname = (String) comboUsuarios.getSelectedItem();
        if (nickname == null) return;
        // Campos comunes
        campoNickname.setText(usuarioActual.getNickname());
        campoCorreo.setText(usuarioActual.getCorreo());
        campoNombre.setText(usuarioActual.getNombre());

        // Diferenciar según tipo
        if (usuarioActual instanceof DTAsistente da) {
            campoApellido.setText(da.getApellido());
            spinnerDia.setValue(da.getFechaNacimiento().getDia());
            spinnerMes.setValue(da.getFechaNacimiento().getMes());
            spinnerAnio.setValue(da.getFechaNacimiento().getAnio());
            cardLayout.show(panelEspecifico, "ASISTENTE");

        } else if (usuarioActual instanceof DTOrganizador dorg) {
            campoDescripcion.setText(dorg.getDescripcion());
            campoSitioWeb.setText(dorg.getSitioWeb());
            cardLayout.show(panelEspecifico, "ORGANIZADOR");
        }
    }
}*/