package presentacion;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contenido de la ventana "Aceptar/Rechazar Edicion de Evento". El diseño
 * esta en AceptarRechazarEdicionPanel.form; esta clase tiene el comportamiento.
 *
 * Sigue el DSS: listarEventos() -> el administrador elige un evento ->
 * listarEdicionesIngresadas(evento) -> elige una edicion -> aceptarEdicion()
 * o rechazarEdicion(). Es el unico lugar del sistema donde una edicion cambia
 * de estado.
 */
public class AceptarRechazarEdicionPanel {

    /** Titulo de todos los dialogos de este caso de uso. */
    private static final String TITULO = "Aceptar/Rechazar Edición";

    private static final String NINGUNO = "(Elegí un evento)";

    // ===== Atados al .form (mismo nombre que el binding) =====
    private JPanel mainPanel;
    private JComboBox<String> comboEventos;
    private JList<String> listaEdiciones;
    private JLabel etiquetaDetalle;
    private JButton botonCerrar;
    private JButton botonRechazar;
    private JButton botonAceptar;

    private final transient IControladorSistema controlador;
    private transient Runnable accionCerrar = () -> {
    };

    /** Las ediciones que se muestran en la lista, por nombre, para ver su detalle. */
    private final transient Map<String, DTEdicionEvento> edicionesMostradas = new LinkedHashMap<>();

    public AceptarRechazarEdicionPanel() {
        controlador = Fabrica.getInstancia().getControladorSistema();

        cargarEventos();

        comboEventos.addActionListener(e -> cargarEdiciones());
        listaEdiciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });
        botonAceptar.addActionListener(e -> cambiarEstado(true));
        botonRechazar.addActionListener(e -> cambiarEstado(false));
        botonCerrar.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    /** Paso 1 del DSS: listarEventos(). */
    private void cargarEventos() {
        comboEventos.addItem(NINGUNO);
        try {
            Set<DTEvento> eventos = controlador.listarEventos();
            eventos.stream()
                    .map(DTEvento::getNombre)
                    .sorted()
                    .forEach(comboEventos::addItem);
        } catch (Exception ex) {
            // El combo queda solo con el placeholder, pero la ventana abre igual.
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
    }

    /** Paso 2 del DSS: listarEdicionesIngresadas(evento). */
    private void cargarEdiciones() {
        edicionesMostradas.clear();
        listaEdiciones.setListData(new String[0]);
        actualizarBotones();

        String evento = eventoElegido();
        if (evento == null) {
            etiquetaDetalle.setText("Elegí un evento para ver sus ediciones Ingresadas.");
            return;
        }

        try {
            List<DTEdicionEvento> ingresadas = controlador.listarEdicionesIngresadas(evento)
                    .stream()
                    .sorted(Comparator.comparing(DTEdicionEvento::getNombre))
                    .toList();
            for (DTEdicionEvento dt : ingresadas) {
                edicionesMostradas.put(dt.getNombre(), dt);
            }
            listaEdiciones.setListData(edicionesMostradas.keySet().toArray(new String[0]));
            etiquetaDetalle.setText(ingresadas.isEmpty()
                    ? "Este evento no tiene ediciones Ingresadas."
                    : "Elegí una edición para aceptarla o rechazarla.");
        } catch (ReglaNegocioException ex) {
            Mensajes.error(mainPanel, TITULO, ex.getMessage());
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
    }

    /** Muestra donde y cuando es la edicion elegida, para decidir con datos. */
    private void mostrarDetalle() {
        actualizarBotones();
        String nombre = listaEdiciones.getSelectedValue();
        if (nombre == null) {
            return;
        }
        DTEdicionEvento dt = edicionesMostradas.get(nombre);
        etiquetaDetalle.setText(dt.getCiudad() + ", " + dt.getPais()
                + " · del " + formatear(dt.getFechaInicio())
                + " al " + formatear(dt.getFechaFin()));
    }

    /** Paso 3 del DSS: aceptarEdicion(nombre) o rechazarEdicion(nombre). */
    private void cambiarEstado(boolean aceptar) {
        // (a) Validacion del FORMULARIO: fuera del try de la logica.
        String nombre = listaEdiciones.getSelectedValue();
        if (nombre == null) {
            Mensajes.aviso(mainPanel, TITULO, "Elegí una edición primero.");
            return;
        }

        String accion = aceptar ? "aceptar" : "rechazar";
        int respuesta = JOptionPane.showConfirmDialog(mainPanel,
                "¿Seguro que querés " + accion + " la edición \"" + nombre + "\"?\n"
                        + "Después no se puede cambiar.",
                TITULO, JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        // (b) Llamada a la LOGICA: siempre dentro del try.
        try {
            if (aceptar) {
                controlador.aceptarEdicion(nombre);
                Mensajes.exito(mainPanel, TITULO,
                        "La edición \"" + nombre + "\" fue aceptada y ya se ve en el sitio.");
            } else {
                controlador.rechazarEdicion(nombre);
                Mensajes.exito(mainPanel, TITULO, "La edición \"" + nombre + "\" fue rechazada.");
            }
            // La edicion ya no esta Ingresada: se vuelve a pedir la lista.
            cargarEdiciones();
        } catch (ReglaNegocioException ex) {
            // Por ejemplo, si otra Estacion de Trabajo ya la acepto o rechazo.
            Mensajes.error(mainPanel, TITULO, ex.getMessage());
            cargarEdiciones();
        } catch (Exception ex) {
            Mensajes.errorInesperado(mainPanel, TITULO, ex);
        }
    }

    private void actualizarBotones() {
        boolean hayEdicion = listaEdiciones.getSelectedValue() != null;
        botonAceptar.setEnabled(hayEdicion);
        botonRechazar.setEnabled(hayEdicion);
    }

    private String eventoElegido() {
        String evento = (String) comboEventos.getSelectedItem();
        return (evento == null || NINGUNO.equals(evento)) ? null : evento;
    }

    private static String formatear(DTFecha fecha) {
        return String.format("%02d/%02d/%d", fecha.getDia(), fecha.getMes(), fecha.getAnio());
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
        mainPanel.setLayout(new GridLayoutManager(5, 4, new Insets(15, 15, 15, 15), 10, 10));
        final JLabel label1 = new JLabel();
        label1.setText("Aceptar/Rechazar Edición de Evento");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Evento");
        mainPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboEventos = new JComboBox();
        mainPanel.add(comboEventos, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Ediciones Ingresadas");
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(380, 160), null, 0, false));
        listaEdiciones = new JList();
        listaEdiciones.setSelectionMode(0);
        scrollPane1.setViewportView(listaEdiciones);
        etiquetaDetalle = new JLabel();
        etiquetaDetalle.setText("Elegí un evento para ver sus ediciones Ingresadas.");
        mainPanel.add(etiquetaDetalle, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        botonCerrar = new JButton();
        botonCerrar.setText("Cerrar");
        mainPanel.add(botonCerrar, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        botonRechazar = new JButton();
        botonRechazar.setEnabled(false);
        botonRechazar.setText("Rechazar");
        mainPanel.add(botonRechazar, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        botonAceptar = new JButton();
        botonAceptar.setEnabled(false);
        botonAceptar.setText("Aceptar");
        mainPanel.add(botonAceptar, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
