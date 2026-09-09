package presentacion;

import com.formdev.flatlaf.FlatLightLaf;
import logica.Fabrica;
import logica.IControladorSistema;

import javax.swing.*;
import java.beans.PropertyVetoException;

/**
 * Ventana principal de la Estacion de Trabajo (GUI Swing). Contiene el
 * menu de acceso a los casos de uso y el JDesktopPane donde cada caso de
 * uso se abre como un JInternalFrame independiente.
 *
 * Convencion para el grupo: cada integrante agrega SU propio JMenuItem en
 * construirMenu() (en el JMenu que corresponda) y SU propio metodo
 * abrirXXX() de una linea que llama a abrir(new VentanaXXX()).
 */
public class VentanaPrincipal extends JFrame {

    private final IControladorSistema controlador;
    private final JDesktopPane escritorio;

    /** Desplazamiento para escalonar cada ventana nueva y que no se apilen. */
    private int offset = 0;

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

        // ===== Modificar Datos de Usuario =====
        JMenuItem itemModificarUsuario = new JMenuItem("Modificar Datos de Usuario");
        itemModificarUsuario.addActionListener(e -> abrirModificarUsuario());
        menuUsuarios.add(itemModificarUsuario);

        // ===== Alta de Usuario =====
        JMenuItem itemAltaUsuario = new JMenuItem("Alta de Usuario");
        itemAltaUsuario.addActionListener(e -> abrirAltaUsuario());
        menuUsuarios.add(itemAltaUsuario);

        // ===== Alta de Edicion de Evento =====
        JMenuItem itemAltaEdicion = new JMenuItem("Alta de Edicion de Evento");
        itemAltaEdicion.addActionListener(e -> abrirAltaEdicion());
        menuEventos.add(itemAltaEdicion);

        // ===== Consulta de Patrocinio =====
        JMenuItem itemConsultaPatrocinio = new JMenuItem("Consulta de Patrocinio");
        itemConsultaPatrocinio.addActionListener(e -> abrirConsultaPatrocinio());
        menuInstituciones.add(itemConsultaPatrocinio);

        // ===== Consulta de Usuario =====
        JMenuItem itemConsultaUsuario = new JMenuItem("Consulta de Usuario");
        itemConsultaUsuario.addActionListener(e -> abrirConsultaUsuario());
        menuUsuarios.add(itemConsultaUsuario);

        // ===== Alta de Tipo de Registro =====
        JMenuItem itemAltaTipoRegistro = new JMenuItem("Alta de Tipo de Registro");
        itemAltaTipoRegistro.addActionListener(e -> abrirAltaTipoRegistro());
        menuRegistros.add(itemAltaTipoRegistro);

        // ===== Consulta de Evento =====
        JMenuItem itemConsultaEvento = new JMenuItem("Consulta de Evento");
        itemConsultaEvento.addActionListener(e -> abrirConsultaEvento());
        menuEventos.add(itemConsultaEvento);

        // ===== Registro a Edicion de Evento =====
        JMenuItem itemRegistroEdicion = new JMenuItem("Registro a Edicion de Evento");
        itemRegistroEdicion.addActionListener(e -> abrirRegistroEdicion());
        menuRegistros.add(itemRegistroEdicion);

        // ===== Consulta de Registro =====
        JMenuItem itemConsultaRegistro = new JMenuItem("Consulta de Registro");
        itemConsultaRegistro.addActionListener(e -> abrirConsultaRegistro());
        menuRegistros.add(itemConsultaRegistro);

        // ===== Alta de Categoria =====
        JMenuItem itemAltaCategoria = new JMenuItem("Alta de Categoria");
        itemAltaCategoria.addActionListener(e -> abrirAltaCategoria());
        menuCategorias.add(itemAltaCategoria);

        // ===== Consulta Tipo de Registro =====
        JMenuItem itemConsultaTipoRegistro = new JMenuItem("Consulta de Tipo de Registro");
        itemConsultaTipoRegistro.addActionListener(e -> abrirConsultaTipoRegistro());
        menuRegistros.add(itemConsultaTipoRegistro);

        // ===== Alta Institucion =====
        JMenuItem itemAltaInstitucion = new JMenuItem("Alta de Institución");
        itemAltaInstitucion.addActionListener(e -> abrirAltaInstitucion());
        menuInstituciones.add(itemAltaInstitucion);

        // ===== Consulta de Edicion de Evento =====
        JMenuItem itemConsultaEdicion = new JMenuItem("Consulta de Edicion de Evento");
        itemConsultaEdicion.addActionListener(e -> abrirConsultaEdicion());
        menuEventos.add(itemConsultaEdicion);

        // ===== Alta de Evento =====
        JMenuItem itemAltaEvento = new JMenuItem("Alta de Evento");
        itemAltaEvento.addActionListener(e -> abrirAltaEvento());
        menuEventos.add(itemAltaEvento);

        // ===== Alta de Patrocinio =====
        JMenuItem itemAltaPatrocinio = new JMenuItem("Alta de Patrocinio");
        itemAltaPatrocinio.addActionListener(e -> abrirAltaPatrocinio());
        menuInstituciones.add(itemAltaPatrocinio);

        menuBar.add(menuUsuarios);
        menuBar.add(menuEventos);
        menuBar.add(menuInstituciones);
        menuBar.add(menuCategorias);
        menuBar.add(menuRegistros);
        return menuBar;
    }

    // ===== Abrir cada pantalla =====
    // Cada metodo es una linea que delega en abrir(...). Para agregar un caso
    // de uso nuevo: crear su VentanaXXX y agregar aca "abrir(new VentanaXXX());".

    private void abrirModificarUsuario()   { abrir(new VentanaModificarUsuario()); }
    private void abrirAltaUsuario()        { abrir(new VentanaAltaUsuario()); }
    private void abrirAltaEdicion()        { abrir(new VentanaAltaEdicion()); }
    private void abrirConsultaPatrocinio() { abrir(new VentanaConsultaPatrocinio()); }
    private void abrirConsultaUsuario()    { abrir(new VentanaConsultaUsuario(controlador)); }
    private void abrirAltaTipoRegistro()   { abrir(new VentanaAltaTipoRegistro()); }
    private void abrirConsultaEvento()     { abrir(new VentanaConsultaEvento()); }
    private void abrirRegistroEdicion()    { abrir(new VentanaRegistroEdicionDeEvento()); }
    private void abrirConsultaRegistro()   { abrir(new VentanaConsultaDeRegistro()); }
    private void abrirAltaCategoria()      { abrir(new VentanaAltaCategoria()); }
    private void abrirConsultaTipoRegistro(){ abrir(new VentanaConsultaTipoRegistro()); }
    private void abrirAltaInstitucion()    { abrir(new VentanaAltaInstitucion()); }
    private void abrirConsultaEdicion()    { abrir(new VentanaConsultaEdicion()); }
    private void abrirAltaEvento()         { abrir(new VentanaAltaEvento()); }
    private void abrirAltaPatrocinio()     { abrir(new VentanaAltaPatrocinio()); }

    /**
     * Agrega la ventana interna al escritorio, la posiciona escalonada
     * respecto de la anterior (para que no se apilen todas en 0,0), la
     * muestra y la deja al frente y seleccionada.
     */
    private void abrir(JInternalFrame ventana) {
        escritorio.add(ventana);
        ventana.setLocation(offset, offset);
        offset = (offset + 30) % 210;
        ventana.setVisible(true);
        ventana.toFront();
        try {
            ventana.setSelected(true);
        } catch (PropertyVetoException ignorada) {
            // Si la ventana rechaza ser seleccionada, no pasa nada.
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                // Si FlatLaf falla, se usa el look por defecto de Swing.
            }
            new VentanaPrincipal().setVisible(true);
        });
    }
}