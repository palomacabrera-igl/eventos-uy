package presentacion;

import javax.swing.JComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Ayuda para que un combo se actualice solo sin pisar lo que la persona eligio.
 * Volver a cargarlo de cero (removeAllItems y addItem) pierde la seleccion y
 * dispara el ActionListener, que en estas pantallas carga las listas de abajo.
 * sincronizar() no hace nada si la lista es la misma; si cambio, la reescribe
 * conservando lo elegido y sin avisarle al panel, salvo que lo elegido haya
 * desaparecido de la base (ahi hay que limpiar lo que dependia de eso).
 */
final class Combos {

    private Combos() {
    }

    /**
     * Pone en el combo los elementos de {@code nuevos}, si cambiaron.
     *
     * @param combo      el combo a actualizar
     * @param nuevos     lo que hay ahora en la base
     * @param clave      lo que identifica a cada elemento (nombre, nickname), para
     *                   comparar dos lecturas de lo mismo
     * @param reservados cuantos elementos del principio no vienen de la base (por
     *                   ejemplo "(Elegi un evento)"): no se tocan
     */
    static <T> void sincronizar(JComboBox<T> combo, Collection<T> nuevos,
                                Function<T, String> clave, int reservados) {
        List<T> ordenados = new ArrayList<>(nuevos);
        ordenados.sort(Comparator.comparing(clave));

        // 1. Si la lista es la misma, no se toca nada.
        List<String> clavesActuales = new ArrayList<>();
        for (int i = reservados; i < combo.getItemCount(); i++) {
            clavesActuales.add(clave.apply(combo.getItemAt(i)));
        }
        clavesActuales.sort(Comparator.naturalOrder());
        List<String> clavesNuevas = ordenados.stream().map(clave).toList();
        if (clavesActuales.equals(clavesNuevas)) {
            return;
        }

        // 2. Se anota que estaba elegido: un elemento de la base (por su
        //    clave) o, si no, la posicion de un reservado o la ausencia de eleccion.
        int posicionElegida = combo.getSelectedIndex();
        String claveElegida = posicionElegida >= reservados
                ? clave.apply(combo.getItemAt(posicionElegida))
                : null;

        // 3. Se reescribe con los oyentes apagados, para que los cambios del
        //    medio no disparen la carga de las listas de abajo.
        ActionListener[] oyentes = combo.getActionListeners();
        for (ActionListener oyente : oyentes) {
            combo.removeActionListener(oyente);
        }
        boolean seguiaEstando = false;
        try {
            while (combo.getItemCount() > reservados) {
                combo.removeItemAt(combo.getItemCount() - 1);
            }
            int nuevaPosicion = claveElegida == null ? posicionElegida : -1;
            for (T elemento : ordenados) {
                combo.addItem(elemento);
                if (claveElegida != null && claveElegida.equals(clave.apply(elemento))) {
                    nuevaPosicion = combo.getItemCount() - 1;
                    seguiaEstando = true;
                }
            }
            if (claveElegida != null && !seguiaEstando) {
                // Lo elegido desaparecio: queda el primer reservado, o nada.
                nuevaPosicion = reservados > 0 ? 0 : -1;
            }
            combo.setSelectedIndex(nuevaPosicion);
        } finally {
            for (ActionListener oyente : oyentes) {
                combo.addActionListener(oyente);
            }
        }

        // 4. Solo si lo que estaba elegido ya no existe se le avisa al panel,
        //    como si la persona hubiera cambiado la eleccion a mano: asi
        //    limpia el detalle y las listas que dependian de eso.
        if (claveElegida != null && !seguiaEstando) {
            ActionEvent aviso = new ActionEvent(combo, ActionEvent.ACTION_PERFORMED, "comboBoxChanged");
            for (ActionListener oyente : oyentes) {
                oyente.actionPerformed(aviso);
            }
        }
    }
}
