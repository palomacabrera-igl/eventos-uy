package presentacion;

import logica.DTEvento;
import logica.IControladorSistema;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Consulta de los eventos mas visitados (letra, Estacion de Trabajo): una tabla
 * con los 5 eventos que mas veces se consultaron, desde el sitio de escritorio y
 * el de celular, con su posicion y su cantidad de visitas.
 *
 * <h2>Que muestra</h2>
 * Solo los eventos con al menos una visita, del que mas tiene al que menos (a
 * igualdad, por nombre). Incluye los eventos finalizados: la Estacion es la que
 * los sigue viendo. Si todavia nadie visito ninguno, la tabla queda vacia y un
 * cartel lo dice.
 *
 * <h2>Se actualiza sola</h2>
 * El sitio cuenta una visita cada vez que alguien abre un evento. Con la ventana
 * abierta, el administrador ve subir los numeros: cada 5 segundos la ventana
 * principal le pide {@link #actualizar()} (ver {@link Actualizable}). Usa
 * listarEventosMasVisitados, que no deja nada seleccionado en el controlador, asi
 * que es seguro llamarla sola.
 */
public class VentanaEventosMasVisitados extends JInternalFrame implements Actualizable {

    /** Cuantos eventos muestra el ranking. */
    static final int CANTIDAD = 5;

    static final String TITULO = "Consulta de Eventos más Visitados";
    static final String TEXTO_SIN_VISITAS = "Todavía no hay eventos con visitas.";
    static final String TEXTO_ACTUALIZACION = "Se actualiza sola mientras la ventana está abierta.";

    private final transient IControladorSistema controlador;
    private final ModeloMasVisitados modelo = new ModeloMasVisitados();
    private final JTable tabla = new JTable(modelo);
    private final JLabel etiquetaEstado = new JLabel();

    public VentanaEventosMasVisitados(IControladorSistema controlador) {
        super(TITULO, true, true, true, true);
        this.controlador = controlador;

        armarPantalla();
        try {
            actualizar();
        } catch (Exception ex) {
            Mensajes.errorInesperado(getContentPane(), TITULO, ex);
        }
        pack();
    }

    /**
     * Se llama sola cada 5 segundos (ver Actualizable). Vuelve a pedir el ranking
     * y, si no cambio, no toca la tabla. Si falla, la excepcion sale (la ventana
     * principal la anota en la consola) y la tabla conserva lo que tenia.
     */
    @Override
    public void actualizar() {
        modelo.reemplazar(controlador.listarEventosMasVisitados(CANTIDAD));
        etiquetaEstado.setText(modelo.getRowCount() == 0 ? TEXTO_SIN_VISITAS : TEXTO_ACTUALIZACION);
    }

    // ===== Pantalla =====

    private void armarPantalla() {
        JLabel titulo = new JLabel("Los " + CANTIDAD + " eventos más visitados", SwingConstants.CENTER);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        centrar(tabla.getColumnModel().getColumn(ModeloMasVisitados.COLUMNA_POSICION), SwingConstants.CENTER, 80);
        centrar(tabla.getColumnModel().getColumn(ModeloMasVisitados.COLUMNA_EVENTO), SwingConstants.LEFT, 320);
        centrar(tabla.getColumnModel().getColumn(ModeloMasVisitados.COLUMNA_VISITAS), SwingConstants.RIGHT, 90);

        JScrollPane desplazamiento = new JScrollPane(tabla);
        // Swing engancha los encabezados al mostrar la ventana; asi ya estan armados antes.
        desplazamiento.setColumnHeaderView(tabla.getTableHeader());
        // Alto de la cabecera mas las 5 filas: el ranking se ve completo sin barra.
        desplazamiento.setPreferredSize(new Dimension(490, 28 + 24 * CANTIDAD + 4));

        etiquetaEstado.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(desplazamiento, BorderLayout.CENTER);
        panel.add(etiquetaEstado, BorderLayout.SOUTH);
        setContentPane(panel);
    }

    private static void centrar(TableColumn columna, int alineacion, int ancho) {
        DefaultTableCellRenderer dibujo = new DefaultTableCellRenderer();
        dibujo.setHorizontalAlignment(alineacion);
        columna.setCellRenderer(dibujo);
        columna.setPreferredWidth(ancho);
    }

    // ===== Datos de la tabla =====

    /**
     * Las filas del ranking. Solo lee: la tabla no se edita. Se separa de la
     * ventana para poder probarlo sin abrir ninguna pantalla.
     */
    static final class ModeloMasVisitados extends AbstractTableModel {

        static final int COLUMNA_POSICION = 0;
        static final int COLUMNA_EVENTO = 1;
        static final int COLUMNA_VISITAS = 2;

        private static final String[] ENCABEZADOS = {"Posición", "Evento", "Visitas"};

        /** Una fila del ranking. */
        record Fila(int posicion, String evento, long visitas) {
        }

        private List<Fila> filas = List.of();

        /**
         * Pone el ranking que llego. Si es el mismo que ya se muestra no avisa a la
         * tabla (asi no pierde la seleccion cada 5 segundos).
         *
         * @param eventos ya ordenados, del mas visitado al menos visitado
         * @return si la tabla cambio
         */
        boolean reemplazar(List<DTEvento> eventos) {
            List<Fila> nuevas = new ArrayList<>();
            for (DTEvento evento : eventos) {
                nuevas.add(new Fila(nuevas.size() + 1, evento.getNombre(), evento.getVisitas()));
            }
            if (nuevas.equals(filas)) {
                return false;
            }
            filas = nuevas;
            fireTableDataChanged();
            return true;
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return ENCABEZADOS.length;
        }

        @Override
        public String getColumnName(int columna) {
            return ENCABEZADOS[columna];
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return switch (columna) {
                case COLUMNA_POSICION -> Integer.class;
                case COLUMNA_VISITAS -> Long.class;
                default -> String.class;
            };
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Fila datos = filas.get(fila);
            return switch (columna) {
                case COLUMNA_POSICION -> datos.posicion();
                case COLUMNA_EVENTO -> datos.evento();
                default -> datos.visitas();
            };
        }
    }
}
