package presentacion;

import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTPatrocinio;
import logica.IControladorSistema;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Pantalla del caso de uso Consulta de Patrocinio. Encadena 3 combos
 * (Evento -> Edicion -> Patrocinio), cada uno disparando el listado del
 * siguiente nivel. Es de solo lectura, no hay Aceptar/Cancelar.
 */
public class VentanaConsultaPatrocinio extends JInternalFrame {

    private final IControladorSistema controlador;

    private JComboBox<DTEvento> comboEventos;
    private JComboBox<DTEdicionEvento> comboEdiciones;
    private JComboBox<DTPatrocinio> comboPatrocinios;

    private JTextField campoCodigo;
    private JTextField campoInstitucion;
    private JTextField campoTipoRegistro;
    private JTextField campoNivel;
    private JTextField campoMonto;
    private JTextField campoCantRegistrosGratis;
    private JTextField campoFecha;

    public VentanaConsultaPatrocinio(IControladorSistema controlador) {
        super("Consulta de Patrocinio", true, true, true, true);
        this.controlador = controlador;
        construirUI();
        cargarEventos();
        setSize(420, 480);
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelSeleccion = new JPanel();
        panelSeleccion.setLayout(new BoxLayout(panelSeleccion, BoxLayout.Y_AXIS));

        comboEventos = new JComboBox<>();
        comboEventos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEvento e) {
                    setText(e.getNombre());
                }
                return this;
            }
        });
        comboEventos.addActionListener(e -> seleccionarEvento());
        panelSeleccion.add(construirFila("Evento:", comboEventos));

        comboEdiciones = new JComboBox<>();
        comboEdiciones.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTEdicionEvento ed) {
                    setText(ed.getNombre());
                }
                return this;
            }
        });
        comboEdiciones.addActionListener(e -> seleccionarEdicion());
        panelSeleccion.add(construirFila("Edicion:", comboEdiciones));

        comboPatrocinios = new JComboBox<>();
        comboPatrocinios.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTPatrocinio p) {
                    setText(p.getCodigoPatrocinio() + " - " + p.getInstitucion());
                }
                return this;
            }
        });
        comboPatrocinios.addActionListener(e -> mostrarPatrocinio());
        panelSeleccion.add(construirFila("Patrocinio:", comboPatrocinios));

        add(panelSeleccion, BorderLayout.NORTH);

        JPanel panelDetalle = new JPanel();
        panelDetalle.setLayout(new BoxLayout(panelDetalle, BoxLayout.Y_AXIS));
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del patrocinio"));

        campoCodigo = campoSoloLectura();
        campoInstitucion = campoSoloLectura();
        campoTipoRegistro = campoSoloLectura();
        campoNivel = campoSoloLectura();
        campoMonto = campoSoloLectura();
        campoCantRegistrosGratis = campoSoloLectura();
        campoFecha = campoSoloLectura();

        panelDetalle.add(construirFila("Codigo:", campoCodigo));
        panelDetalle.add(construirFila("Institucion:", campoInstitucion));
        panelDetalle.add(construirFila("Tipo de registro:", campoTipoRegistro));
        panelDetalle.add(construirFila("Nivel:", campoNivel));
        panelDetalle.add(construirFila("Monto:", campoMonto));
        panelDetalle.add(construirFila("Registros gratis:", campoCantRegistrosGratis));
        panelDetalle.add(construirFila("Fecha:", campoFecha));

        add(panelDetalle, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton botonCerrar = new JButton("Cerrar");
        botonCerrar.addActionListener(e -> dispose());
        panelBotones.add(botonCerrar);
        add(panelBotones, BorderLayout.SOUTH);
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

    private void cargarEventos() {
        // listarEventos() : set<DTEvento>
        Set<DTEvento> eventos = controlador.listarEventos();
        for (DTEvento e : eventos) {
            comboEventos.addItem(e);
        }
    }

    private void seleccionarEvento() {
        DTEvento evento = (DTEvento) comboEventos.getSelectedItem();
        comboEdiciones.removeAllItems();
        comboPatrocinios.removeAllItems();
        limpiarDetalle();
        if (evento == null) {
            return;
        }
        // listarEdicionesDeEvento(nombreEvento) : set<DTEdicionEvento>
        Set<DTEdicionEvento> ediciones = controlador.listarEdicionesDeEvento(evento.getNombre());
        for (DTEdicionEvento ed : ediciones) {
            comboEdiciones.addItem(ed);
        }
    }

    private void seleccionarEdicion() {
        DTEdicionEvento edicion = (DTEdicionEvento) comboEdiciones.getSelectedItem();
        comboPatrocinios.removeAllItems();
        limpiarDetalle();
        if (edicion == null) {
            return;
        }
        // listarPatrociniosDeEdicion(nombreEdicion) : set<DTPatrocinio>
        Set<DTPatrocinio> patrocinios = controlador.listarPatrociniosDeEdicion(edicion.getNombre());
        for (DTPatrocinio p : patrocinios) {
            comboPatrocinios.addItem(p);
        }
    }

    private void mostrarPatrocinio() {
        DTPatrocinio seleccionado = (DTPatrocinio) comboPatrocinios.getSelectedItem();
        if (seleccionado == null) {
            limpiarDetalle();
            return;
        }
        // mostrarPatrocinio(codigoPatrocinio) : DTPatrocinio
        DTPatrocinio detalle = controlador.mostrarPatrocinio(seleccionado.getCodigoPatrocinio());
        campoCodigo.setText(String.valueOf(detalle.getCodigoPatrocinio()));
        campoInstitucion.setText(detalle.getInstitucion());
        campoTipoRegistro.setText(detalle.getTipoRegistro());
        campoNivel.setText(detalle.getNivel().toString());
        campoMonto.setText(String.valueOf(detalle.getMonto()));
        campoCantRegistrosGratis.setText(String.valueOf(detalle.getCantRegistrosGratis()));
        DTFecha fecha = detalle.getFecha();
        campoFecha.setText(fecha.getDia() + "/" + fecha.getMes() + "/" + fecha.getAnio());
    }

    private void limpiarDetalle() {
        campoCodigo.setText("");
        campoInstitucion.setText("");
        campoTipoRegistro.setText("");
        campoNivel.setText("");
        campoMonto.setText("");
        campoCantRegistrosGratis.setText("");
        campoFecha.setText("");
    }
}