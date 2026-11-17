package presentacion;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

/**
 * Una pantalla que se vuelve a leer sola de la base cada pocos segundos.
 *
 * <h2>Para que sirve</h2>
 *
 * El sitio web y la Estacion de Trabajo son dos programas sobre la misma base.
 * Si un organizador da de alta una edicion desde el sitio, el administrador que
 * tiene abierta la ventana "Aceptar/Rechazar Edicion" tiene que verla aparecer
 * sin hacer nada. Antes habia que ir al menu y apretar "Actualizar datos": el
 * programa dependia de que alguien se acordara de apretarlo.
 *
 * Ahora la ventana principal (VentanaPrincipal) tiene un temporizador que cada
 * 5 segundos le pide actualizar() a cada ventana abierta que sea Actualizable.
 *
 * <h2>Como hacer que una pantalla se actualice sola</h2>
 *
 * <pre>
 *   1. Implementar Actualizable en el panel y escribir actualizar(): volver a
 *      pedir la lista al controlador y ponerla con Combos.sincronizar(...).
 *   2. En el constructor del panel: Actualizable.registrar(mainPanel, this);
 *      (si la ventana es un JInternalFrame que implementa Actualizable ella
 *      misma, no hace falta registrar nada).
 * </pre>
 *
 * <h2>Reglas para escribir actualizar()</h2>
 *
 * <ul>
 *   <li><b>No pisar lo que la persona esta haciendo.</b> Se actualizan las
 *       listas, pero lo elegido y lo escrito se conserva (Combos.sincronizar lo
 *       hace). Si nada cambio en la base, no se toca nada: no hay parpadeo.</li>
 *   <li><b>Solo operaciones SIN estado.</b> listarEventos(), listarUsuarios(),
 *       listarNombresInstituciones() y listarEdicionesIngresadas(...) no dejan
 *       nada "seleccionado" en el controlador. En cambio listarEdicionesDeEvento(...)
 *       y todas las seleccionarXxx(...) del Laboratorio 1 SI lo dejan, y como
 *       actualizar() corre sola, cada 5 segundos, en TODAS las ventanas, le
 *       cambiarian la seleccion a la ventana donde alguien esta trabajando. Por
 *       eso las listas de segundo nivel (por ejemplo, las ediciones del evento
 *       elegido) se siguen cargando al elegir, no solas.</li>
 *   <li><b>No mostrar dialogos.</b> Si algo falla, la excepcion sale y la ventana
 *       principal la anota en la consola: un cartel cada 5 segundos con la base
 *       caida seria insoportable.</li>
 * </ul>
 */
public interface Actualizable {

    /** Con este nombre se guarda la pantalla dentro de su panel principal. */
    String PROPIEDAD = "presentacion.actualizable";

    /** Vuelve a leer lo que muestra esta pantalla. Corre en el hilo de Swing. */
    void actualizar() throws Exception;

    /** Deja anotado, en el panel principal de una ventana, quien lo actualiza. */
    static void registrar(JComponent panelPrincipal, Actualizable pantalla) {
        panelPrincipal.putClientProperty(PROPIEDAD, pantalla);
    }

    /**
     * Quien actualiza esta ventana, o null si la ventana no se actualiza sola
     * (por ejemplo, las de alta, que son formularios y no muestran datos que
     * cambien).
     */
    static Actualizable de(JInternalFrame ventana) {
        if (ventana instanceof Actualizable propia) {
            return propia;
        }
        if (ventana.getContentPane() instanceof JComponent contenido
                && contenido.getClientProperty(PROPIEDAD) instanceof Actualizable registrada) {
            return registrada;
        }
        return null;
    }
}
