package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import logica.DTFecha;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.TipoUsuario;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Contenido de la ventana interna "Crear Cuenta" (caso de uso Alta de
 * Usuario). El diseño esta en AltaUsuarioPanel.form (editable por
 * arrastre); esta clase contiene el comportamiento.
 */
public class AltaUsuarioPanel {

    private static final String SIN_INSTITUCION = "(Ninguna)";

    private JPanel mainPanel;
    private JTextField txtCorreo;
    private JTextField txtNombre;
    private JTextField txtNickname;
    private JRadioButton asistenteRadioButton;
    private JRadioButton organizadorRadioButton;
    private JPanel PanelAsistente;
    private JPanel PanelOrganizador;
    private JTextField txtApellido;
    private JLabel Apellido;
    private JComboBox InstitucionCBox;
    private JLabel Institucion;
    private JComboBox FechaNacDiaCBox;
    private JComboBox FechaNacMesCBox;
    private JComboBox FechaNacAnioCBox;
    private JTextField textField1;
    private JTextField textField2;
    private JButton confirmarButton;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    public AltaUsuarioPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        cargarCombos();
        asistenteRadioButton.setSelected(true);
        mostrarPanelEspecifico();

        asistenteRadioButton.addActionListener(e -> mostrarPanelEspecifico());
        organizadorRadioButton.addActionListener(e -> mostrarPanelEspecifico());
        confirmarButton.addActionListener(e -> confirmar());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /** Define que hacer cuando el panel pide cerrarse (lo decide la ventana principal). */
    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void cargarCombos() {
        for (int dia = 1; dia <= 31; dia++) {
            FechaNacDiaCBox.addItem(dia);
        }
        for (int mes = 1; mes <= 12; mes++) {
            FechaNacMesCBox.addItem(mes);
        }
        int anioActual = LocalDate.now().getYear();
        for (int anio = anioActual; anio >= anioActual - 100; anio--) {
            FechaNacAnioCBox.addItem(anio);
        }

        InstitucionCBox.addItem(SIN_INSTITUCION);
        // listarNombresInstituciones() : set<String>
        Set<String> instituciones = controlador.listarNombresInstituciones();
        for (String nombre : instituciones) {
            InstitucionCBox.addItem(nombre);
        }
    }

    private void mostrarPanelEspecifico() {
        PanelAsistente.setVisible(asistenteRadioButton.isSelected());
        PanelOrganizador.setVisible(organizadorRadioButton.isSelected());
    }

    private void confirmar() {
        String correo = txtCorreo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String nickname = txtNickname.getText().trim();

        if (correo.isEmpty() || nombre.isEmpty() || nickname.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Complete correo, nombre y nickname.",
                    "Crear Cuenta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TipoUsuario tipo = asistenteRadioButton.isSelected() ? TipoUsuario.ASISTENTE : TipoUsuario.ORGANIZADOR;
        DTUsuario datos = new DTUsuario(nickname, nombre, correo);

        // ingresarDatosUsuario(datos, tipo) : boolean
        if (!controlador.ingresarDatosUsuario(datos, tipo)) {
            // [nickname/correo en uso]: se avisa y se deja la ventana abierta para reintentar (LOOP del dss).
            JOptionPane.showMessageDialog(mainPanel,
                    "Ya existe un usuario con ese nickname o ese correo.",
                    "Crear Cuenta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tipo == TipoUsuario.ASISTENTE) {
            confirmarAsistente();
        } else {
            confirmarOrganizador();
        }

        JOptionPane.showMessageDialog(mainPanel, "El usuario se ha creado con éxito.",
                "Crear Cuenta", JOptionPane.INFORMATION_MESSAGE);
        limpiar();
        accionCerrar.run();
    }

    private void confirmarAsistente() {
        String apellido = txtApellido.getText().trim();
        int dia = (Integer) FechaNacDiaCBox.getSelectedItem();
        int mes = (Integer) FechaNacMesCBox.getSelectedItem();
        int anio = (Integer) FechaNacAnioCBox.getSelectedItem();
        DTFecha fechaNac = new DTFecha(dia, mes, anio);

        // ingresarDatosAsistente(apellido, fechaNac)
        controlador.ingresarDatosAsistente(apellido, fechaNac);

        String institucion = (String) InstitucionCBox.getSelectedItem();
        if (institucion != null && !SIN_INSTITUCION.equals(institucion)) {
            // [pertenece a institucion]: seleccionarInstitucion(nombreInstitucion)
            controlador.seleccionarInstitucion(institucion);
        }
    }

    private void confirmarOrganizador() {
        String descripcion = textField1.getText().trim();
        String sitioWeb = textField2.getText().trim();

        // ingresarDatosOrganizador(descripcion, sitioWeb)
        controlador.ingresarDatosOrganizador(descripcion, sitioWeb);
    }

    private void limpiar() {
        txtCorreo.setText("");
        txtNombre.setText("");
        txtNickname.setText("");
        txtApellido.setText("");
        textField1.setText("");
        textField2.setText("");
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
        label1.setText("Crear Cuenta");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Correo Electrónico");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Nombre");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtCorreo = new JTextField();
        mainPanel.add(txtCorreo, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Nickname");
        mainPanel.add(label4, new GridConstraints(3, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtNombre = new JTextField();
        mainPanel.add(txtNombre, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtNickname = new JTextField();
        mainPanel.add(txtNickname, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, -1), null, 0, false));
        asistenteRadioButton = new JRadioButton();
        asistenteRadioButton.setText("Asistente");
        mainPanel.add(asistenteRadioButton, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        organizadorRadioButton = new JRadioButton();
        organizadorRadioButton.setText("Organizador");
        mainPanel.add(organizadorRadioButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PanelAsistente = new JPanel();
        PanelAsistente.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(PanelAsistente, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Apellido = new JLabel();
        Apellido.setText("Apellido");
        PanelAsistente.add(Apellido, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtApellido = new JTextField();
        PanelAsistente.add(txtApellido, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        InstitucionCBox = new JComboBox();
        PanelAsistente.add(InstitucionCBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Institucion = new JLabel();
        Institucion.setText("Institución");
        PanelAsistente.add(Institucion, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Fecha Nacimiento");
        PanelAsistente.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        FechaNacDiaCBox = new JComboBox();
        FechaNacDiaCBox.setEditable(false);
        PanelAsistente.add(FechaNacDiaCBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        FechaNacMesCBox = new JComboBox();
        PanelAsistente.add(FechaNacMesCBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        FechaNacAnioCBox = new JComboBox();
        PanelAsistente.add(FechaNacAnioCBox, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        PanelOrganizador = new JPanel();
        PanelOrganizador.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(PanelOrganizador, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Descripción");
        PanelOrganizador.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        PanelOrganizador.add(textField1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Sitio Web");
        PanelOrganizador.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField2 = new JTextField();
        PanelOrganizador.add(textField2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        confirmarButton = new JButton();
        confirmarButton.setText("Confirmar");
        mainPanel.add(confirmarButton, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(asistenteRadioButton);
        buttonGroup.add(organizadorRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
