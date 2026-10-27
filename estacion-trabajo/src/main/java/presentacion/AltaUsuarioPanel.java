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
 * Contenido de la ventana interna "Alta de Usuario" (caso de uso Alta de
 * Usuario). El diseño esta en AltaUsuarioPanel.form (editable por
 * arrastre); esta clase contiene el comportamiento.
 */
public class AltaUsuarioPanel {

    /**
     * Titulo de todos los dialogos de este caso de uso.
     */
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
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmacion;
    private JButton elegirImagenButton;
    private JLabel lblImagen;

    /** Imagen elegida con "Elegir imagen...", o null si no se eligio ninguna (es opcional). */
    private transient DTImagen imagenElegida;

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
        elegirImagenButton.addActionListener(e -> elegirImagen());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Define que hacer cuando el panel pide cerrarse (lo decide la ventana principal).
     */
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

        // Contrasena y confirmacion: obligatorias y tienen que coincidir.
        // getPassword() devuelve char[] y no String: es lo que da JPasswordField.
        String password = new String(txtPassword.getPassword());
        String confirmacion = new String(txtConfirmacion.getPassword());
        if (password.isEmpty() || confirmacion.isEmpty()) {
            Mensajes.aviso(mainPanel, TITULO, "Ingresá la contraseña y su confirmación.");
            return;
        }
        if (!password.equals(confirmacion)) {
            Mensajes.aviso(mainPanel, TITULO, "La contraseña y la confirmación no coinciden.");
            return;
        }

        TipoUsuario tipo = asistenteRadioButton.isSelected() ? TipoUsuario.ASISTENTE : TipoUsuario.ORGANIZADOR;

        // La fecha se valida aca porque el formulario la arma con tres combos y
        // puede quedar una fecha que no existe (31 de febrero).
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

        // (b) Llamada a la LOGICA: el alta entera es UNA sola llamada, que
        // tambien guarda la contrasena (la entidad la hashea) y la imagen.
        // La logica vuelve a validar todo (vacios, contrasenas, nickname y
        // correo repetidos), porque la usa tambien el sitio web.
        try {
            DTUsuario datos;
            String institucion = null;
            if (tipo == TipoUsuario.ASISTENTE) {
                datos = datosAsistente(nickname, nombre, correo);
                String elegida = (String) InstitucionCBox.getSelectedItem();
                if (elegida != null && !SIN_INSTITUCION.equals(elegida)) {
                    institucion = elegida;
                }
            } else {
                datos = new DTOrganizador(nickname, nombre, correo,
                        textField1.getText().trim(), textField2.getText().trim());
            }

            controlador.altaUsuario(datos, tipo, password, confirmacion, institucion, imagenElegida);

            Mensajes.exito(mainPanel, TITULO, "El usuario se ha creado con éxito.");
            limpiar();
            accionCerrar.run();
        } catch (ReglaNegocioException ex) {
            // [nickname/correo en uso, contrasenas distintas]: se avisa y la
            // ventana queda abierta para corregir (LOOP del DSS).
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

    /** Arma el DTAsistente con el apellido y la fecha elegida en los combos. */
    private DTAsistente datosAsistente(String nickname, String nombre, String correo) {
        int dia = (Integer) FechaNacDiaCBox.getSelectedItem();
        int mes = (Integer) FechaNacMesCBox.getSelectedItem();
        int anio = (Integer) FechaNacAnioCBox.getSelectedItem();
        DTFecha fechaNac = new DTFecha(dia, mes, anio);
        // La institucion viaja aparte, como parametro de altaUsuario.
        return new DTAsistente(nickname, nombre, correo,
                txtApellido.getText().trim(), fechaNac, null);
    }

    /** "Elegir imagen...": la guarda en memoria hasta que se confirma el alta. */
    private void elegirImagen() {
        SelectorImagen.Eleccion eleccion = SelectorImagen.elegir(mainPanel, TITULO);
        if (eleccion != null) {
            imagenElegida = eleccion.imagen();
            lblImagen.setText(eleccion.nombreArchivo());
        }
    }

    private void limpiar() {
        txtPassword.setText("");
        txtConfirmacion.setText("");
        imagenElegida = null;
        lblImagen.setText("Sin imagen");
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
        mainPanel.setLayout(new GridLayoutManager(10, 4, new Insets(15, 15, 15, 15), 10, 10));
        final JLabel label1 = new JLabel();
        label1.setText("Alta de Usuario");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Correo Electrónico");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Nombre");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtCorreo = new JTextField();
        mainPanel.add(txtCorreo, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Nickname");
        mainPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtNombre = new JTextField();
        mainPanel.add(txtNombre, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtNickname = new JTextField();
        mainPanel.add(txtNickname, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, -1), null, 0, false));
        final JLabel labelPassword = new JLabel();
        labelPassword.setText("Contraseña");
        mainPanel.add(labelPassword, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtPassword = new JPasswordField();
        mainPanel.add(txtPassword, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel labelConfirmacion = new JLabel();
        labelConfirmacion.setText("Confirmar contraseña");
        mainPanel.add(labelConfirmacion, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtConfirmacion = new JPasswordField();
        mainPanel.add(txtConfirmacion, new GridConstraints(5, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel labelImagen = new JLabel();
        labelImagen.setText("Imagen (opcional)");
        mainPanel.add(labelImagen, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        elegirImagenButton = new JButton();
        elegirImagenButton.setText("Elegir imagen...");
        mainPanel.add(elegirImagenButton, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblImagen = new JLabel();
        lblImagen.setText("Sin imagen");
        mainPanel.add(lblImagen, new GridConstraints(6, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        asistenteRadioButton = new JRadioButton();
        asistenteRadioButton.setText("Asistente");
        mainPanel.add(asistenteRadioButton, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        organizadorRadioButton = new JRadioButton();
        organizadorRadioButton.setText("Organizador");
        mainPanel.add(organizadorRadioButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PanelAsistente = new JPanel();
        PanelAsistente.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(PanelAsistente, new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Apellido = new JLabel();
        Apellido.setText("Apellido");
        PanelAsistente.add(Apellido, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtApellido = new JTextField();
        PanelAsistente.add(txtApellido, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        PanelAsistente.add(FechaNacDiaCBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        FechaNacMesCBox = new JComboBox();
        PanelAsistente.add(FechaNacMesCBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        FechaNacAnioCBox = new JComboBox();
        PanelAsistente.add(FechaNacAnioCBox, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        PanelOrganizador = new JPanel();
        PanelOrganizador.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(PanelOrganizador, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Descripción");
        PanelOrganizador.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        PanelOrganizador.add(textField1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Sitio Web");
        PanelOrganizador.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField2 = new JTextField();
        PanelOrganizador.add(textField2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        confirmarButton = new JButton();
        confirmarButton.setText("Aceptar");
        mainPanel.add(confirmarButton, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelarButton = new JButton();
        cancelarButton.setText("Cancelar");
        mainPanel.add(cancelarButton, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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