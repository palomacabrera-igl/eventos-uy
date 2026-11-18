package presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import logica.DTEvento;
import logica.IControladorSistema;

/**
 * Ventana de la Estacion con los 5 eventos mas visitados, sin base de datos y
 * sin mostrar nada en pantalla: el controlador es uno de mentira que contesta
 * solo listarEventosMasVisitados, y la ventana se arma pero no se muestra.
 *
 * <pre>
 *  Se arma con el ranking que da el controlador
 *  CV  una fila por evento: posicion (1, 2, 3...), nombre y visitas
 *  CV  le pide 5, que es lo que dice la letra
 *  CV  sin visitas: tabla vacia y un cartel que lo dice
 *
 *  actualizar() (la ventana principal lo llama cada 5 segundos)
 *  CV  si el ranking cambio, la tabla lo refleja
 *  CV  si no cambio, la tabla no se entera (no pierde la seleccion)
 *  CI  si el controlador falla, la excepcion sale y la tabla conserva lo que tenia
 * </pre>
 */
@DisplayName("Estacion: eventos mas visitados")
class VentanaEventosMasVisitadosTest {

    private List<DTEvento> ranking;
    private final AtomicInteger pedidos = new AtomicInteger();
    private final AtomicInteger cantidadPedida = new AtomicInteger();
    private Supplier<List<DTEvento>> respuesta;

    @BeforeEach
    void armar() {
        ranking = new ArrayList<>(List.of(
                evento("Maraton de Montevideo", 120),
                evento("JIAP", 80),
                evento("Montevideo Rock", 80)));
        pedidos.set(0);
        cantidadPedida.set(0);
        respuesta = () -> ranking;
    }

    private static DTEvento evento(String nombre, long visitas) {
        DTEvento evento = new DTEvento();
        evento.setNombre(nombre);
        evento.setVisitas(visitas);
        return evento;
    }

    /** Un controlador que contesta solo listarEventosMasVisitados; cualquier otra cosa es un error. */
    private IControladorSistema controlador() {
        return (IControladorSistema) Proxy.newProxyInstance(
                IControladorSistema.class.getClassLoader(),
                new Class<?>[]{IControladorSistema.class},
                (proxy, metodo, args) -> {
                    if (!metodo.getName().equals("listarEventosMasVisitados")) {
                        throw new UnsupportedOperationException(metodo.getName());
                    }
                    pedidos.incrementAndGet();
                    cantidadPedida.set((Integer) args[0]);
                    return respuesta.get();
                });
    }

    private static JTable tablaDe(VentanaEventosMasVisitados ventana) {
        JScrollPane desplazamiento = (JScrollPane) ((javax.swing.JPanel) ventana.getContentPane())
                .getComponent(1);
        return (JTable) desplazamiento.getViewport().getView();
    }

    private static JLabel cartelDe(VentanaEventosMasVisitados ventana) {
        return (JLabel) ((javax.swing.JPanel) ventana.getContentPane()).getComponent(2);
    }

    // ----- Lo que muestra -----

    @Test
    @DisplayName("una fila por evento, con su posicion, su nombre y sus visitas")
    void unaFilaPorEvento() {
        JTable tabla = tablaDe(new VentanaEventosMasVisitados(controlador()));

        assertEquals(3, tabla.getRowCount());
        assertEquals(1, tabla.getValueAt(0, 0));
        assertEquals("Maraton de Montevideo", tabla.getValueAt(0, 1));
        assertEquals(120L, tabla.getValueAt(0, 2));
        assertEquals(2, tabla.getValueAt(1, 0));
        assertEquals("JIAP", tabla.getValueAt(1, 1));
        assertEquals(3, tabla.getValueAt(2, 0));
        assertEquals("Montevideo Rock", tabla.getValueAt(2, 1));
        assertEquals(80L, tabla.getValueAt(2, 2));
    }

    @Test
    @DisplayName("respeta el orden que da el controlador, sin reordenar por su cuenta")
    void respetaElOrden() {
        ranking = new ArrayList<>(List.of(evento("B", 9), evento("A", 9), evento("C", 1)));

        JTable tabla = tablaDe(new VentanaEventosMasVisitados(controlador()));

        assertEquals("B", tabla.getValueAt(0, 1));
        assertEquals("A", tabla.getValueAt(1, 1));
        assertEquals("C", tabla.getValueAt(2, 1));
    }

    @Test
    @DisplayName("le pide al controlador 5 eventos, que es lo que dice la letra")
    void pideCinco() {
        new VentanaEventosMasVisitados(controlador());

        assertEquals(5, cantidadPedida.get());
        assertEquals(5, VentanaEventosMasVisitados.CANTIDAD);
    }

    @Test
    @DisplayName("las columnas se llaman Posicion, Evento y Visitas")
    void encabezados() {
        JTable tabla = tablaDe(new VentanaEventosMasVisitados(controlador()));

        assertEquals("Posición", tabla.getColumnName(0));
        assertEquals("Evento", tabla.getColumnName(1));
        assertEquals("Visitas", tabla.getColumnName(2));
    }

    @Test
    @DisplayName("la tabla es de solo lectura")
    void soloLectura() {
        JTable tabla = tablaDe(new VentanaEventosMasVisitados(controlador()));

        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            assertFalse(tabla.isCellEditable(0, columna));
        }
    }

    @Test
    @DisplayName("con datos, el cartel dice que se actualiza sola")
    void cartelConDatos() {
        VentanaEventosMasVisitados ventana = new VentanaEventosMasVisitados(controlador());

        assertEquals(VentanaEventosMasVisitados.TEXTO_ACTUALIZACION, cartelDe(ventana).getText());
    }

    @Test
    @DisplayName("sin visitas: tabla vacia y un cartel que lo dice")
    void sinVisitas() {
        ranking = new ArrayList<>();

        VentanaEventosMasVisitados ventana = new VentanaEventosMasVisitados(controlador());

        assertEquals(0, tablaDe(ventana).getRowCount());
        assertEquals(VentanaEventosMasVisitados.TEXTO_SIN_VISITAS, cartelDe(ventana).getText());
    }

    // ----- Se actualiza sola -----

    @Test
    @DisplayName("es Actualizable: la ventana principal la encuentra y la actualiza")
    void esActualizable() {
        VentanaEventosMasVisitados ventana = new VentanaEventosMasVisitados(controlador());

        assertSame(ventana, Actualizable.de(ventana));
    }

    @Test
    @DisplayName("al actualizar, si el ranking cambio la tabla lo refleja")
    void actualizarConCambios() {
        VentanaEventosMasVisitados ventana = new VentanaEventosMasVisitados(controlador());
        JTable tabla = tablaDe(ventana);

        ranking = new ArrayList<>(List.of(
                evento("JIAP", 130), evento("Maraton de Montevideo", 120),
                evento("Montevideo Rock", 80), evento("Libro1", 3)));
        ventana.actualizar();

        assertEquals(4, tabla.getRowCount());
        assertEquals("JIAP", tabla.getValueAt(0, 1));
        assertEquals(130L, tabla.getValueAt(0, 2));
        assertEquals("Libro1", tabla.getValueAt(3, 1));
    }

    @Test
    @DisplayName("al actualizar, el cartel pasa a decir que no hay visitas si se vaciaron")
    void actualizarVaciando() {
        VentanaEventosMasVisitados ventana = new VentanaEventosMasVisitados(controlador());

        ranking = new ArrayList<>();
        ventana.actualizar();

        assertEquals(0, tablaDe(ventana).getRowCount());
        assertEquals(VentanaEventosMasVisitados.TEXTO_SIN_VISITAS, cartelDe(ventana).getText());
    }

    @Test
    @DisplayName("al actualizar sin cambios, la tabla no se entera: no pierde la seleccion")
    void actualizarSinCambios() {
        VentanaEventosMasVisitados ventana = new VentanaEventosMasVisitados(controlador());
        JTable tabla = tablaDe(ventana);
        tabla.setRowSelectionInterval(1, 1);
        AtomicInteger avisos = new AtomicInteger();
        tabla.getModel().addTableModelListener(e -> avisos.incrementAndGet());

        // El controlador devuelve objetos nuevos cada vez, con los mismos datos.
        ranking = new ArrayList<>(List.of(
                evento("Maraton de Montevideo", 120), evento("JIAP", 80), evento("Montevideo Rock", 80)));
        ventana.actualizar();
        ventana.actualizar();

        assertEquals(0, avisos.get());
        assertEquals(1, tabla.getSelectedRow());
        assertEquals(3, pedidos.get());
    }

    @Test
    @DisplayName("si el controlador falla al actualizar, la excepcion sale y la tabla queda como estaba")
    void actualizarConFalla() {
        VentanaEventosMasVisitados ventana = new VentanaEventosMasVisitados(controlador());
        respuesta = () -> {
            throw new IllegalStateException("la base se cayo");
        };

        assertThrows(IllegalStateException.class, ventana::actualizar);

        JTable tabla = tablaDe(ventana);
        assertEquals(3, tabla.getRowCount());
        assertEquals("Maraton de Montevideo", tabla.getValueAt(0, 1));
        assertNotNull(cartelDe(ventana).getText());
        assertTrue(cartelDe(ventana).getText().length() > 0);
    }

    // ----- El modelo de la tabla -----

    @Test
    @DisplayName("el modelo dice si cambio: la primera vez si, la misma lista no, otra lista si")
    void modeloDiceSiCambio() {
        VentanaEventosMasVisitados.ModeloMasVisitados modelo = new VentanaEventosMasVisitados.ModeloMasVisitados();

        assertTrue(modelo.reemplazar(List.of(evento("A", 2), evento("B", 1))));
        assertFalse(modelo.reemplazar(List.of(evento("A", 2), evento("B", 1))));
        assertTrue(modelo.reemplazar(List.of(evento("A", 3), evento("B", 1))));
        assertTrue(modelo.reemplazar(List.of()));
        assertFalse(modelo.reemplazar(List.of()));
    }

    @Test
    @DisplayName("las clases de las columnas dejan que Swing alinee los numeros")
    void clasesDeLasColumnas() {
        VentanaEventosMasVisitados.ModeloMasVisitados modelo = new VentanaEventosMasVisitados.ModeloMasVisitados();

        assertEquals(Integer.class, modelo.getColumnClass(0));
        assertEquals(String.class, modelo.getColumnClass(1));
        assertEquals(Long.class, modelo.getColumnClass(2));
    }
}
