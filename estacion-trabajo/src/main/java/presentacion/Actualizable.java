package presentacion;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

/**
 * Una pantalla que se vuelve a leer sola de la base cada pocos segundos, para que
 * la Estacion vea lo que cambia el sitio sin un boton de "Actualizar".
 * VentanaPrincipal tiene un temporizador que cada 5 segundos llama a actualizar()
 * en cada ventana abierta que sea Actualizable.
 *
 * Para sumar una pantalla: implementar Actualizable (actualizar() vuelve a pedir la
 * lista y la pone con Combos.sincronizar) y llamar a Actualizable.registrar(mainPanel,
 * this) en el constructor.
 *
 * Reglas para actualizar(): conservar lo que la persona eligio o escribio; usar solo
 * operaciones sin estado (listarEdicionesDeEvento y las seleccionarXxx dejan una
 * seleccion en el controlador y la cambiarian mientras alguien trabaja); y no mostrar
 * dialogos, porque un cartel cada 5 segundos con la base caida seria insoportable.
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
