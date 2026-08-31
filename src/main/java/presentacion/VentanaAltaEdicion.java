package presentacion;

import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTOrganizador;
import logica.IControladorSistema;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * Pantalla del caso de uso Alta de Edicion de Evento. Combo de Evento +
 * combo de Organizador (ambos disparan la seleccion correspondiente en
 * Sistema, que retiene las referencias) + formulario de datos de la
 * edicion. Si el nombre ya existe, ingresarDatosEdicion() devuelve false y
 * la ventana queda abierta para corregir (LOOP del dss).
 */
public class VentanaAltaEdicion extends JInternalFrame {

    private final IControladorSistema controlador;

    private JComboBox<DTEvento> comboEventos;
    private JComboBox<DTOrganizador> comboOrganizadores;

    private JTextField campoNombre;
    private JTextField campoSigla;
    private JTextField campoCiudad;
    private JTextField campoPais;

    private JSpinner spinnerDiaInicio, spinnerMesInicio, spinnerAnioInicio;
    private JSpinner spinnerDiaFin, spinnerMesFin, spinnerAnioFin;
    private JSpinner spinnerDiaAlta, spinnerMesAlta, spinnerAnioAlta;

    public VentanaAltaEdicion(IControladorSistema controlador) {
        super("Alta de Edicion de Evento", true, true, true, true);
        this.controlador = controlador;
        construirUI();
        cargarEventosYOrganizadores();
        setSize(430, 560);
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));

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
        panelFormulario.add(construirFila("Evento:", comboEventos));

        comboOrganizadores = new JComboBox<>();
        comboOrganizadores.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DTOrganizador o) {
                    setText(o.getNombre() + " (" + o.getNickname() + ")");
                }
                return this;
            }
        });
        comboOrganizadores.addActionListener(e -> seleccionarOrganizador());
        panelFormulario.add(construirFila("Organizador:", comboOrganizadores));

        campoNombre = new JTextField();
        campoSigla = new JTextField();
        campoCiudad = new JTextField();
        campoPais = new JTextField();
        panelFormulario.add(construirFila("Nombre:", campoNombre));
        panelFormulario.add(construirFila("Sigla:", campoSigla));
        panelFormulario.add(construirFila("Ciudad:", campoCiudad));
        panelFormulario.add(construirFila("Pais:", campoPais));

        spinnerDiaInicio = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
        spinnerMesInicio = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerAnioInicio = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));
        panelFormulario.add(construirFilaFecha("Fecha de inicio:",
                spinnerDiaInicio, spinnerMesInicio, spinnerAnioInicio));

        spinnerDiaFin = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
        spinnerMesFin = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerAnioFin = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));
        panelFormulario.add(construirFilaFecha("Fecha de fin:",
                spinnerDiaFin, spinnerMesFin, spinnerAnioFin));

        spinnerDiaAlta = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
        spinnerMesAlta = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spinnerAnioAlta = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));
        panelFormulario.add(construirFilaFecha("Fecha de alta:",
                spinnerDiaAlta, spinnerMesAlta, spinnerAnioAlta));

        add(new JScrollPane(panelFormulario), BorderLayout.CENTER);

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

    private JPanel construirFilaFecha(String etiqueta, JSpinner dia, JSpinner mes, JSpinner anio) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.add(new JLabel(etiqueta));
        fila.add(dia);
        fila.add(mes);
        fila.add(anio);
        return fila;
    }

    private void cargarEventosYOrganizadores() {
        // listarEventos() : set<DTEvento>
        Set<DTEvento> eventos = controlador.listarEventos();
        for (DTEvento e : eventos) {
            comboEventos.addItem(e);
        }
        // listarOrganizadores() : set<DTOrganizador>
        Set<DTOrganizador> organizadores = controlador.listarOrganizadores();
        for (DTOrganizador o : organizadores) {
            comboOrganizadores.addItem(o);
        }
    }

    private void seleccionarEvento() {
        DTEvento evento = (DTEvento) comboEventos.getSelectedItem();
        if (evento == null) {
            return;
        }
        // seleccionarEvento(nombre) : DTEvento -- Sistema retiene eventoSeleccionado
        controlador.seleccionarEvento(evento.getNombre());
    }

    private void seleccionarOrganizador() {
        DTOrganizador organizador = (DTOrganizador) comboOrganizadores.getSelectedItem();
        if (organizador == null) {
            return;
        }
        // seleccionarOrganizador(nickname) : DTOrganizador -- Sistema retiene organizadorSeleccionado
        controlador.seleccionarOrganizador(organizador.getNickname());
    }

    private void aceptar() {
        if (comboEventos.getSelectedItem() == null || comboOrganizadores.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Elegi un evento y un organizador.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            DTFecha fechaInicio = new DTFecha((int) spinnerDiaInicio.getValue(),
                    (int) spinnerMesInicio.getValue(), (int) spinnerAnioInicio.getValue());
            DTFecha fechaFin = new DTFecha((int) spinnerDiaFin.getValue(),
                    (int) spinnerMesFin.getValue(), (int) spinnerAnioFin.getValue());
            DTFecha fechaAlta = new DTFecha((int) spinnerDiaAlta.getValue(),
                    (int) spinnerMesAlta.getValue(), (int) spinnerAnioAlta.getValue());

            DTEdicionEvento dt = new DTEdicionEvento(campoNombre.getText(), campoSigla.getText(),
                    campoCiudad.getText(), campoPais.getText(), fechaInicio, fechaFin, fechaAlta);

            // ingresarDatosEdicion(dt) : boolean
            boolean exito = controlador.ingresarDatosEdicion(dt);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Edicion de evento dada de alta correctamente.",
                        "Alta de Edicion de Evento", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                // LOOP [nombre ya existe y el administrador desea corregir los datos]:
                // se avisa y se deja la ventana abierta para reintentar, no se cierra.
                JOptionPane.showMessageDialog(this,
                        "Ya existe una edicion con el nombre \"" + campoNombre.getText()
                                + "\". Elegi otro nombre.",
                        "Nombre en uso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrio un error al dar de alta la edicion: "
                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}