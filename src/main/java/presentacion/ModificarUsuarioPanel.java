package presentacion;

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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;

public class ModificarUsuarioPanel {

    private static final String NINGUNO = "(Elegí un usuario)";

    // ===== Todos atados al .form (mismo nombre que el binding) =====
    private JPanel mainPanel;
    private JComboBox comboUsuarios;
    private JTextField campoNickname;
    private JTextField campoCorreo;
    private JTextField campoNombre;
    private JPanel panelEspecifico;
    private JPanel panelAsistente;
    private JTextField campoApellido;
    private JSpinner spinnerDia;
    private JSpinner spinnerMes;
    private JSpinner spinnerAnio;
    private JPanel panelOrganizador;
    private JTextField campoDescripcion;
    private JTextField campoSitioWeb;
    private JButton botonAceptar;
    private JButton botonCancelar;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };
    private DTUsuario usuarioActual;

    public ModificarUsuarioPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        campoNickname.setEditable(false);
        campoNickname.setBackground(Color.LIGHT_GRAY);
        campoCorreo.setEditable(false);
        campoCorreo.setBackground(Color.LIGHT_GRAY);

        spinnerDia.setModel(new SpinnerNumberModel(1, 1, 31, 1));
        spinnerMes.setModel(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerAnio.setModel(new SpinnerNumberModel(2000, 1900, LocalDate.now().getYear(), 1));

        panelAsistente.setVisible(false);
        panelOrganizador.setVisible(false);

        cargarUsuarios();

        comboUsuarios.addActionListener(e -> seleccionarUsuario());
        botonAceptar.addActionListener(e -> aceptar());
        botonCancelar.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void reajustarTamanio() {
        JInternalFrame ventana =
                (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, mainPanel);
        if (ventana != null) {
            ventana.pack();
        }
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void cargarUsuarios() {
        comboUsuarios.addItem(NINGUNO);
        Set<DTUsuario> usuarios = controlador.listarUsuarios();
        for (DTUsuario u : usuarios) {
            comboUsuarios.addItem(u.getNickname());
        }
    }

    private void seleccionarUsuario() {
        String nickname = (String) comboUsuarios.getSelectedItem();

        if (nickname == null || NINGUNO.equals(nickname)) {
            usuarioActual = null;
            campoNickname.setText("");
            campoCorreo.setText("");
            campoNombre.setText("");
            panelAsistente.setVisible(false);
            panelOrganizador.setVisible(false);
            reajustarTamanio();
            return;
        }

        usuarioActual = controlador.seleccionarUsuario(nickname);
        campoNickname.setText(usuarioActual.getNickname());
        campoCorreo.setText(usuarioActual.getCorreo());
        campoNombre.setText(usuarioActual.getNombre());

        panelAsistente.setVisible(usuarioActual instanceof DTAsistente);
        panelOrganizador.setVisible(usuarioActual instanceof DTOrganizador);

        if (usuarioActual instanceof DTAsistente da) {
            campoApellido.setText(da.getApellido());
            spinnerDia.setValue(da.getFechaNacimiento().getDia());
            spinnerMes.setValue(da.getFechaNacimiento().getMes());
            spinnerAnio.setValue(da.getFechaNacimiento().getAnio());
        } else if (usuarioActual instanceof DTOrganizador dorg) {
            campoDescripcion.setText(dorg.getDescripcion());
            campoSitioWeb.setText(dorg.getSitioWeb());
        }

        reajustarTamanio();
    }

    private void aceptar() {
        if (usuarioActual == null) {
            JOptionPane.showMessageDialog(mainPanel, "Elegí un usuario primero.",
                    "Modificar Datos de Usuario", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "El nombre no puede quedar vacío.",
                    "Modificar Datos de Usuario", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (usuarioActual instanceof DTAsistente) {
            if (campoApellido.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "El apellido no puede quedar vacío.",
                        "Modificar Datos de Usuario", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LocalDate fechaNac;
            try {
                fechaNac = LocalDate.of((int) spinnerAnio.getValue(),
                        (int) spinnerMes.getValue(), (int) spinnerDia.getValue());
            } catch (DateTimeException ex) {
                JOptionPane.showMessageDialog(mainPanel,
                        "La fecha de nacimiento no existe (revisá el día para ese mes).",
                        "Modificar Datos de Usuario", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!fechaNac.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(mainPanel,
                        "La fecha de nacimiento debe ser anterior a la fecha actual.",
                        "Modificar Datos de Usuario", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            DTUsuario dtModificado;
            if (usuarioActual instanceof DTAsistente) {
                DTFecha fecha = new DTFecha((int) spinnerDia.getValue(),
                        (int) spinnerMes.getValue(), (int) spinnerAnio.getValue());
                dtModificado = new DTAsistente(usuarioActual.getNickname(),
                        nombre, usuarioActual.getCorreo(),
                        campoApellido.getText().trim(), fecha);
            } else {
                dtModificado = new DTOrganizador(usuarioActual.getNickname(),
                        nombre, usuarioActual.getCorreo(),
                        campoDescripcion.getText().trim(), campoSitioWeb.getText().trim());
            }
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
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Usuario:");
        mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        comboUsuarios = new JComboBox();
        mainPanel.add(comboUsuarios, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Nickname");
        mainPanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        campoNickname = new JTextField();
        mainPanel.add(campoNickname, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Correo:");
        mainPanel.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        campoCorreo = new JTextField();
        campoCorreo.setText("");
        mainPanel.add(campoCorreo, new GridConstraints(3, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Nombre:");
        mainPanel.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        campoNombre = new JTextField();
        campoNombre.setText("");
        mainPanel.add(campoNombre, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panelEspecifico = new JPanel();
        panelEspecifico.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panelEspecifico, new GridConstraints(5, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelAsistente = new JPanel();
        panelAsistente.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelEspecifico.add(panelAsistente, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Apellido:");
        panelAsistente.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoApellido = new JTextField();
        panelAsistente.add(campoApellido, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Fecha de Nacimiento:");
        panelAsistente.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerDia = new JSpinner();
        panelAsistente.add(spinnerDia, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerAnio = new JSpinner();
        panelAsistente.add(spinnerAnio, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerMes = new JSpinner();
        panelAsistente.add(spinnerMes, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelOrganizador = new JPanel();
        panelOrganizador.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelEspecifico.add(panelOrganizador, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Descripcion:");
        panelOrganizador.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Sitio web:");
        panelOrganizador.add(label8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        campoDescripcion = new JTextField();
        panelOrganizador.add(campoDescripcion, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        campoSitioWeb = new JTextField();
        panelOrganizador.add(campoSitioWeb, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        botonAceptar = new JButton();
        botonAceptar.setText("Aceptar");
        mainPanel.add(botonAceptar, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        botonCancelar = new JButton();
        botonCancelar.setText("Cancelar");
        mainPanel.add(botonCancelar, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Modificar datos de usuario");
        mainPanel.add(label9, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}