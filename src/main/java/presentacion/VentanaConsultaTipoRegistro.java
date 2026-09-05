package presentacion;

import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTTipoRegistro;
import logica.Fabrica;
import logica.IControladorSistema;

import javax.swing.*;
import java.util.Set;

public class VentanaConsultaTipoRegistro extends JInternalFrame {

    private JPanel mainPanel;
    private JComboBox eventoBox;
    private JComboBox edicionBox;
    private JComboBox registroBox;
    private JTextField nombreTxt;
    private JTextField descTxt;
    private JTextField costoTxt;
    private JTextField cupoTxt;
    private JButton botonCerrar;

    private final IControladorSistema controlador;

    public VentanaConsultaTipoRegistro() {
        super("Consulta de Tipo de Registro", true, true, true, true);

        controlador = Fabrica.getInstancia().getControladorSistema();

        setContentPane(mainPanel);
        pack();

        nombreTxt.setEditable(false);
        descTxt.setEditable(false);
        costoTxt.setEditable(false);
        cupoTxt.setEditable(false);

        cargarEventos();

        eventoBox.addActionListener(e -> cargarEdiciones());
        edicionBox.addActionListener(e -> cargarTiposRegistro());
        registroBox.addActionListener(e -> mostrarTipoRegistro());
        botonCerrar.addActionListener(e -> dispose());
    }

    private void cargarEventos() {
        for (DTEvento evento : controlador.listarEventos()) {
            eventoBox.addItem(evento.getNombre());
        }
        cargarEdiciones();
    }

    private void cargarEdiciones() {
        edicionBox.removeAllItems();
        registroBox.removeAllItems();
        limpiarDetalle();

        String nombreEvento = (String) eventoBox.getSelectedItem();
        if (nombreEvento == null) {
            return;
        }

        Set<DTEdicionEvento> ediciones =
                controlador.listarEdicionesDeEvento(nombreEvento);

        for (DTEdicionEvento edicion : ediciones) {
            edicionBox.addItem(edicion.getNombre());
        }
    }

    private void cargarTiposRegistro() {
        registroBox.removeAllItems();
        limpiarDetalle();

        String nombreEdicion = (String) edicionBox.getSelectedItem();
        if (nombreEdicion == null) {
            return;
        }

        Set<DTTipoRegistro> tipos =
                controlador.listarTiposRegistroDeEdicion(nombreEdicion);

        for (DTTipoRegistro tipo : tipos) {
            registroBox.addItem(tipo.getNombre());
        }
    }

    private void mostrarTipoRegistro() {
        String nombreTipo = (String) registroBox.getSelectedItem();
        if (nombreTipo == null) {
            limpiarDetalle();
            return;
        }

        DTTipoRegistro tipo =
                controlador.seleccionarTipoRegistro(nombreTipo);

        nombreTxt.setText(tipo.getNombre());
        descTxt.setText(tipo.getDescripcion());
        costoTxt.setText(String.valueOf(tipo.getCosto()));
        cupoTxt.setText(String.valueOf(tipo.getCupo()));
    }

    private void limpiarDetalle() {
        nombreTxt.setText("");
        descTxt.setText("");
        costoTxt.setText("");
        cupoTxt.setText("");
    }
}
