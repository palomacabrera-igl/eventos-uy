package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import logica.*;

import javax.swing.*;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;

/**
 * Contenido de la ventana interna "Crear Cuenta" (caso de uso Alta de
 * Usuario). El diseño esta en AltaUsuarioPanel.form (editable por
 * arrastre); esta clase contiene el comportamiento.
 */
public class AltaUsuarioPanel {

    /** Titulo de todos los dialogos de este caso de uso (criterio del equipo). */
    private static final String TITULO = "Alta de Usuario";


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
    private JButton cancelarButton;

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
        cancelarButton.addActionListener(e -> accionCerrar.run());
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
        try {
            // listarNombresInstituciones() : set<String>
            Set<String> instituciones = controlador.listarNombresInstituciones();
            for (String nombre : instituciones) {
                InstitucionCBox.addItem(nombre);
            }
        } catch (Exception ex) {
            // El combo queda solo con "(sin institucion)", pero la ventana abre igual.
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
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

        // (a) Validacion del FORMULARIO: fuera del try de la logica.
        if (correo.isEmpty() || nombre.isEmpty() || nickname.isEmpty()) {
            Mensajes.aviso(mainPanel, TITULO, "Complete correo, nombre y nickname.");
            return;
        }

        if (!correo.contains("@")) {
            Mensajes.aviso(mainPanel, TITULO, "El correo no es válido (tiene que contener @).");
            return;
        }

        TipoUsuario tipo = asistenteRadioButton.isSelected() ? TipoUsuario.ASISTENTE : TipoUsuario.ORGANIZADOR;

        // Se valida la fecha de nacimiento ANTES de tocar a Sistema (si aplica), para
        // no dejar datos a medio recordar si el usuario todavia tiene que corregir algo.
        if (tipo == TipoUsuario.ASISTENTE && !fechaNacimientoValida()) {
            return;
        }

        if (tipo == TipoUsuario.ASISTENTE && txtApellido.getText().trim().isEmpty()) {
            Mensajes.aviso(mainPanel, TITULO, "Ingresá el apellido del asistente.");
            return;
        }

        if (tipo == TipoUsuario.ORGANIZADOR && textField1.getText().trim().isEmpty()) {
            Mensajes.aviso(mainPanel, TITULO, "Ingresá la descripción del organizador.");
            return;
        }

        DTUsuario datos = new DTUsuario(nickname, nombre, correo);

        // (b) Llamada a la LOGICA. Este alta son VARIOS pasos encadenados
        // (ingresarDatosUsuario -> ingresarDatosAsistente/Organizador ->
        // seleccionarInstitucion), asi que van todos en UN SOLO try: si falla
        // un paso del medio, se avisa una sola vez y no se dice "creado con exito".
        try {
            controlador.ingresarDatosUsuario(datos, tipo);

            if (tipo == TipoUsuario.ASISTENTE) {
                confirmarAsistente();
            } else {
                confirmarOrganizador();
            }

            Mensajes.exito(mainPanel, TITULO, "El usuario se ha creado con éxito.");
            limpiar();
            accionCerrar.run();
        } catch (ReglaNegocioException ex) {
            // [nickname/correo en uso]: se avisa y la ventana queda abierta (LOOP del dss)
            Mensajes.error(mainPanel, TITULO, ex.getMessage());
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
    }

    private boolean fechaNacimientoValida() {
        int dia = (Integer) FechaNacDiaCBox.getSelectedItem();
        int mes = (Integer) FechaNacMesCBox.getSelectedItem();
        int anio = (Integer) FechaNacAnioCBox.getSelectedItem();
        LocalDate fechaNac;
        try {
            fechaNac = LocalDate.of(anio, mes, dia);
        } catch (DateTimeException ex) {
            // Validacion de formato del formulario, no un fallo del sistema.
            Mensajes.aviso(mainPanel, TITULO,
                    "La fecha de nacimiento no existe (revisá el día para ese mes).");
            return false;
        }
        if (!fechaNac.isBefore(LocalDate.now())) {
            Mensajes.aviso(mainPanel, TITULO,
                    "La fecha de nacimiento debe ser anterior a la fecha actual.");
            return false;
        }
        return true;
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
        mainPanel.setLayout(new GridLayoutManager(7, 4, new Insets(15, 15, 15, 15), 10, 10));
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
        confirmarButton.setText("Aceptar");
        mainPanel.add(confirmarButton, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelarButton = new JButton();
        cancelarButton.setText("Cancelar");
        mainPanel.add(cancelarButton, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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