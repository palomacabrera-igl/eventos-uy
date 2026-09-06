package presentacion;

import logica.Fabrica;
import logica.IControladorSistema;

import javax.swing.*;

/**
 * Ventana principal de la Estacion de Trabajo (GUI Swing). Contiene el
 * menu de acceso a los casos de uso y el JDesktopPane donde cada caso de
 * uso se abre como un JInternalFrame independiente.
 *
 * Convencion para el grupo: cada integrante agrega SU propio JMenuItem en
 * construirMenu() (en el JMenu que corresponda) y SU propio metodo
 * abrirXXX() que crea su JInternalFrame. Asi cada uno toca líneas
 * distintas del archivo y se evitan conflictos de merge.
 */
public class VentanaPrincipal extends JFrame {

    private final IControladorSistema controlador;
    private final JDesktopPane escritorio;

    public VentanaPrincipal() {
        super("eventos.uy - Estacion de Trabajo");
        this.controlador = Fabrica.getInstancia().getControladorSistema();

        this.escritorio = new JDesktopPane();
        setContentPane(escritorio);

        setJMenuBar(construirMenu());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    private JMenuBar construirMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuUsuarios = new JMenu("Usuarios");
        JMenu menuEventos = new JMenu("Eventos y Ediciones");
        JMenu menuInstituciones = new JMenu("Instituciones y Patrocinios");
        JMenu menuCategorias = new JMenu("Categorias");
        JMenu menuRegistros = new JMenu("Registros");

        // ===== Paloma: Modificar Datos de Usuario =====
        JMenuItem itemModificarUsuario = new JMenuItem("Modificar Datos de Usuario");
        itemModificarUsuario.addActionListener(e -> abrirModificarUsuario());
        menuUsuarios.add(itemModificarUsuario);

        // ===== Marti: Alta de Usuario =====
        JMenuItem itemAltaUsuario = new JMenuItem("Alta de Usuario");
        itemAltaUsuario.addActionListener(e -> abrirAltaUsuario());
        menuUsuarios.add(itemAltaUsuario);

        // ===== Paloma: Alta de Edicion de Evento =====
        JMenuItem itemAltaEdicion = new JMenuItem("Alta de Edicion de Evento");
        itemAltaEdicion.addActionListener(e -> abrirAltaEdicion());
        menuEventos.add(itemAltaEdicion);

        // ===== Paloma: Consulta de Patrocinio =====
        JMenuItem itemConsultaPatrocinio = new JMenuItem("Consulta de Patrocinio");
        itemConsultaPatrocinio.addActionListener(e -> abrirConsultaPatrocinio());
        menuInstituciones.add(itemConsultaPatrocinio);

        // ===== Sebastian : Consulta de Usuario =====
        JMenuItem itemConsultaUsuario = new JMenuItem("Consulta de Usuario");
        itemConsultaUsuario.addActionListener(e -> abrirConsultaUsuario());
        menuUsuarios.add(itemConsultaUsuario);

        // ===== Marti: Alta de Tipo de Registro =====
        JMenuItem itemAltaTipoRegistro = new JMenuItem("Alta de Tipo de Registro");
        itemAltaTipoRegistro.addActionListener(e -> abrirAltaTipoRegistro());
        menuRegistros.add(itemAltaTipoRegistro);

        // ===== Marti: Consulta de Evento =====
        JMenuItem itemConsultaEvento = new JMenuItem("Consulta de Evento");
        itemConsultaEvento.addActionListener(e -> abrirConsultaEvento());
        menuEventos.add(itemConsultaEvento);

        // ===== Leandro: Registro a Edicion de Evento =====
        JMenuItem itemRegistroEdicion = new JMenuItem("Registro a Edicion de Evento");
        itemRegistroEdicion.addActionListener(e -> abrirRegistroEdicion());
        menuRegistros.add(itemRegistroEdicion);

        // ===== Leandro: Consulta de Registro =====
        JMenuItem itemConsultaRegistro = new JMenuItem("Consulta de Registro");
        itemConsultaRegistro.addActionListener(e -> abrirConsultaRegistro());
        menuRegistros.add(itemConsultaRegistro);

        // ===== Leandro: Alta de Categoria =====
        JMenuItem itemAltaCategoria = new JMenuItem("Alta de Categoria");
        itemAltaCategoria.addActionListener(e -> abrirAltaCategoria());
        menuCategorias.add(itemAltaCategoria);

        // ===== Sebastian: Consulta Tipo de Registro =====
        JMenuItem itemConsultaTipoRegistro = new JMenuItem("Consulta de Tipo de Registro");
        itemConsultaTipoRegistro.addActionListener(e -> abrirConsultaTipoRegistro());
        menuRegistros.add(itemConsultaTipoRegistro);

        // ===== Sebastian: Alta Institucion =====
        JMenuItem itemAltaInstitucion = new JMenuItem("Alta de Institución");
        itemAltaInstitucion.addActionListener(e -> abrirAltaInstitucion());
        menuInstituciones.add(itemAltaInstitucion);

        // ===== Elias: Consulta de Edicion de Evento =====
        JMenuItem itemConsultaEdicion = new JMenuItem("Consulta de Edicion de Evento");
        itemConsultaEdicion.addActionListener(e -> abrirConsultaEdicion());
        menuEventos.add(itemConsultaEdicion);

        // ===== Sebastián: Alta de Evento =====
        JMenuItem itemAltaEvento = new JMenuItem("Alta de Evento");
        itemAltaEvento.addActionListener(e -> abrirAltaEvento());
        menuEventos.add(itemAltaEvento);

        // TODO (Elias): agregar aca su propio
        // JMenuItem en el JMenu que corresponda, siguiendo el mismo patron:
        // 1) crear el JMenuItem con el nombre del caso de uso
        // 2) itemXXX.addActionListener(e -> abrirXXX());
        // 3) menuYYY.add(itemXXX);
        // 4) agregar su propio metodo privado abrirXXX() mas abajo.

        menuBar.add(menuUsuarios);
        menuBar.add(menuEventos);
        menuBar.add(menuInstituciones);
        menuBar.add(menuCategorias);
        menuBar.add(menuRegistros);
        return menuBar;
    }

    // ===== Metodos que abren cada pantalla =====

    private void abrirModificarUsuario() {
        VentanaModificarUsuario ventana = new VentanaModificarUsuario();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirAltaUsuario() {
        VentanaAltaUsuario ventana = new VentanaAltaUsuario();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirAltaEdicion() {
        VentanaAltaEdicion ventana = new VentanaAltaEdicion();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirConsultaPatrocinio() {
        VentanaConsultaPatrocinio ventana = new VentanaConsultaPatrocinio();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirConsultaUsuario() {
        VentanaConsultaUsuario ventana = new VentanaConsultaUsuario(controlador);
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirAltaTipoRegistro() {
        VentanaAltaTipoRegistro ventana = new VentanaAltaTipoRegistro();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirConsultaEvento() {
        VentanaConsultaEvento ventana = new VentanaConsultaEvento();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirRegistroEdicion() {
        VentanaRegistroEdicionDeEvento ventana = new VentanaRegistroEdicionDeEvento();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirConsultaRegistro() {
        VentanaConsultaDeRegistro ventana = new VentanaConsultaDeRegistro();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirAltaCategoria() {
        VentanaAltaCategoria ventana = new VentanaAltaCategoria();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirConsultaTipoRegistro() {
        VentanaConsultaTipoRegistro ventana = new VentanaConsultaTipoRegistro();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirAltaInstitucion() {
        VentanaAltaInstitucion ventana = new VentanaAltaInstitucion();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirConsultaEdicion() {
        VentanaConsultaEdicion ventana = new VentanaConsultaEdicion();
        ventana.setVisible(true);
        ventana.setLocation(30, 30);
        escritorio.add(ventana);
    }

    private void abrirAltaEvento() {
        VentanaAltaEvento ventana = new VentanaAltaEvento();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }


    /**
     * Placeholder disponible para el resto del grupo: mientras alguno de
     * los otros casos de uso no tenga su pantalla real todavia, puede
     * usar "mostrarPlaceholder(\"nombre del caso de uso\")" en su propio
     * metodo abrirXXX() como stand-in temporal.
     */
    private void mostrarPlaceholder(String nombreCasoDeUso) {
        JInternalFrame ventana = new JInternalFrame(nombreCasoDeUso, true, true, true, true);
        ventana.add(new JLabel("  Pantalla de \"" + nombreCasoDeUso + "\" - en construccion  "));
        ventana.setSize(380, 120);
        ventana.setVisible(true);
        escritorio.add(ventana);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
