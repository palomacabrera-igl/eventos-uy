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
 * abrirXXX() que crea su JInternalFrame. Asi cada uno toca lineas
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

        // TODO (Elias / Leandro / Sebastian): agregar aca su propio
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
        VentanaModificarUsuario ventana = new VentanaModificarUsuario(controlador);
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirAltaUsuario() {
        VentanaAltaUsuario ventana = new VentanaAltaUsuario();
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirAltaEdicion() {
        VentanaAltaEdicion ventana = new VentanaAltaEdicion(controlador);
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private void abrirConsultaPatrocinio() {
        VentanaConsultaPatrocinio ventana = new VentanaConsultaPatrocinio(controlador);
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
