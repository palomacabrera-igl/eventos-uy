package presentacion;

import logica.DTAsistente;
import logica.DTFecha;
import logica.DTOrganizador;
import logica.DTUsuario;
import logica.IControladorSistema;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Pantalla del caso de uso Modificar Datos de Usuario. Solo conoce
 * IControladorSistema y DTs -- nunca objetos de dominio.
 */
public class VentanaModificarUsuario extends JInternalFrame {

    private final IControladorSistema controlador;

    private JComboBox<String> comboUsuarios;
    private JTextField campoNickname;
    private JTextField campoCorreo;
    private JTextField campoNombre;

    // Panel que cambia segun el tipo de usuario seleccionado
    private CardLayout cardLayout;
    private JPanel panelEspecifico;

    // Campos propios de Asistente
    private JTextField campoApellido;
    private JSpinner spinnerDia;
    private JSpinner spinnerMes;
    private JSpinner spinnerAnio;

    // Campos propios de Organizador
    private JTextField campoDescripcion;
    private JTextField campoSitioWeb;

    private DTUsuario usuarioActual;

    public VentanaModificarUsuario(IControladorSistema controlador) {
        super("Modificar Datos de Usuario", true, true, true, true);
        this.controlador = controlador;
        construirUI();
        cargarUsuarios();
        setSize(420, 380);
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSeleccion.add(new JLabel("Usuario:"));
        comboUsuarios = new JComboBox<>();
        comboUsuarios.addActionListener(e -> seleccionarUsuario());
        panelSeleccion.add(comboUsuarios);
        add(panelSeleccion, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));

        campoNickname = new JTextField();
        campoNickname.setEditable(false);
        campoCorreo = new JTextField();
        campoCorreo.setEditable(false);
        campoNombre = new JTextField();

        panelFormulario.add(construirFila("Nickname:", campoNickname));
        panelFormulario.add(construirFila("Correo:", campoCorreo));
        panelFormulario.add(construirFila("Nombre:", campoNombre));

        cardLayout = new CardLayout();
        panelEspecifico = new JPanel(cardLayout);

        JPanel panelAsistente = new JPanel();
        panelAsistente.setLayout(new BoxLayout(panelAsistente, BoxLayout.Y_AXIS));
        campoApellido = new JTextField();
        panelAsistente.add(construirFila("Apellido:", campoApellido));
        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spinnerDia = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
        spinnerMes = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerAnio = new JSpinner(new SpinnerNumberModel(2000, 1900, 2026, 1));
        panelFecha.add(new JLabel("Fecha de nacimiento:"));
        panelFecha.add(spinnerDia);
        panelFecha.add(spinnerMes);
        panelFecha.add(spinnerAnio);
        panelAsistente.add(panelFecha);

        JPanel panelOrganizador = new JPanel();
        panelOrganizador.setLayout(new BoxLayout(panelOrganizador, BoxLayout.Y_AXIS));
        campoDescripcion = new JTextField();
        campoSitioWeb = new JTextField();
        panelOrganizador.add(construirFila("Descripcion:", campoDescripcion));
        panelOrganizador.add(construirFila("Sitio web:", campoSitioWeb));

        panelEspecifico.add(panelAsistente, "ASISTENTE");
        panelEspecifico.add(panelOrganizador, "ORGANIZADOR");
        panelFormulario.add(panelEspecifico);

        add(panelFormulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton botonAceptar = new JButton("Aceptar");
        botonAceptar.addActionListener(e -> aceptar());
        JButton botonCancelar = new JButton("Cancelar");
        botonCancelar.addActionListener(e -> dispose());
        panelBotones.add(botonAceptar);
        panelBotones.add(botonCancelar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel construirFila(String etiqueta, JComponent campo) {
        JPanel fila = new JPanel(new BorderLayout(5, 0));
        fila.add(new JLabel(etiqueta), BorderLayout.WEST);
        fila.add(campo, BorderLayout.CENTER);
        return fila;
    }

    private void cargarUsuarios() {
        // listarUsuarios() : set<DTUsuario>
        Set<DTUsuario> usuarios = controlador.listarUsuarios();
        for (DTUsuario u : usuarios) {
            comboUsuarios.addItem(u.getNickname());
        }
        if (comboUsuarios.getItemCount() > 0) {
            comboUsuarios.setSelectedIndex(0);
        }
    }

    private void seleccionarUsuario() {
        String nickname = (String) comboUsuarios.getSelectedItem();
        if (nickname == null) {
            return;
        }
        // seleccionarUsuario(nickname) : DTUsuario
        usuarioActual = controlador.seleccionarUsuario(nickname);
        campoNickname.setText(usuarioActual.getNickname());
        campoCorreo.setText(usuarioActual.getCorreo());
        campoNombre.setText(usuarioActual.getNombre());

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

    private void aceptar() {
        try {
            DTUsuario dtModificado;
            if (usuarioActual instanceof DTAsistente) {
                DTFecha fecha = new DTFecha((int) spinnerDia.getValue(),
                        (int) spinnerMes.getValue(), (int) spinnerAnio.getValue());
                dtModificado = new DTAsistente(usuarioActual.getNickname(),
                        campoNombre.getText(), usuarioActual.getCorreo(),
                        campoApellido.getText(), fecha);
            } else {
                dtModificado = new DTOrganizador(usuarioActual.getNickname(),
                        campoNombre.getText(), usuarioActual.getCorreo(),
                        campoDescripcion.getText(), campoSitioWeb.getText());
            }
            // modificarDatosUsuario(dt)
            controlador.modificarDatosUsuario(dtModificado);
            JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.",
                    "Modificar Datos de Usuario", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrio un error al modificar los datos: "
                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}