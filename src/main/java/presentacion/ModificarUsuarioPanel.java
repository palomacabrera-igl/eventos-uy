/*package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import logica.DTAsistente;
import logica.DTFecha;
import logica.DTOrganizador;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Contenido de la ventana interna "Modificar Datos de Usuario".
 * El diseño general esta en ModificarUsuarioPanel.form (editable por
 * arrastre); esta clase contiene el comportamiento. El panel especifico de
 * Asistente/Organizador (panelEspecifico) se arma en codigo porque cambia
 * dinamicamente segun el tipo de usuario seleccionado.
 */
/*public class ModificarUsuarioPanel {

    private JPanel mainPanel;
    private JComboBox comboUsuarios;
    private JTextField campoNickname;
    private JTextField campoCorreo;
    private JTextField campoNombre;
    private JPanel panelEspecifico;
    private JButton botonCancelar;
    private JButton botonAceptar;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    // Sub-paneles de panelEspecifico (armados en codigo, ver construirPanelEspecifico())
    private CardLayout cardLayout;
    private JTextField campoApellido;
    private JSpinner spinnerDia;
    private JSpinner spinnerMes;
    private JSpinner spinnerAnio;
    private JTextField campoDescripcion;
    private JTextField campoSitioWeb;

    private DTUsuario usuarioActual;

    public ModificarUsuarioPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        campoNickname.setEditable(false);
        campoNickname.setBackground(Color.LIGHT_GRAY);
        campoCorreo.setEditable(false);
        campoCorreo.setBackground(Color.LIGHT_GRAY);

        construirPanelEspecifico();
        cargarUsuarios();

        comboUsuarios.addActionListener(e -> seleccionarUsuario());
        botonAceptar.addActionListener(e -> aceptar());
        botonCancelar.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /** Define que hacer cuando el panel pide cerrarse (lo decide la ventana principal). */
    /*public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void construirPanelEspecifico() {
        cardLayout = new CardLayout();
        panelEspecifico.setLayout(cardLayout);

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
            JOptionPane.showMessageDialog(mainPanel, "Datos actualizados correctamente.",
                    "Modificar Datos de Usuario", JOptionPane.INFORMATION_MESSAGE);
            accionCerrar.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "Ocurrio un error al modificar los datos: "
                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    /*private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(6, 2, new Insets(15, 15, 15, 15), 10, 10));
        final JLabel label1 = new JLabel();
        label1.setText("Usuario:");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboUsuarios = new JComboBox();
        mainPanel.add(comboUsuarios, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Nickname:");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoNickname = new JTextField();
        mainPanel.add(campoNickname, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(220, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Correo:");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoCorreo = new JTextField();
        mainPanel.add(campoCorreo, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(220, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Nombre:");
        mainPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoNombre = new JTextField();
        mainPanel.add(campoNombre, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(220, -1), null, 0, false));
        panelEspecifico = new JPanel();
        panelEspecifico.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panelEspecifico, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        botonCancelar = new JButton();
        botonCancelar.setText("Cancelar");
        mainPanel.add(botonCancelar, new GridConstraints(5, 0, 1, 1, 3, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        botonAceptar = new JButton();
        botonAceptar.setText("Aceptar");
        mainPanel.add(botonAceptar, new GridConstraints(5, 1, 1, 1, 3, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
   /* public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}*/
