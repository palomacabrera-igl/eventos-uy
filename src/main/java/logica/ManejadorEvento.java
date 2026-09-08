package logica;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador de la coleccion de Evento (patron "collection object" de GRASP).
 *
 * Singleton. Ademas de guardar/buscar/listar Evento, ofrece buscarEdicion(nombre): las
 * EdicionEvento NO son una coleccion de primer nivel (viven dentro de su
 * Evento), asi que para encontrar una edicion por nombre hay que recorrer los
 * eventos.
 */
public class ManejadorEvento {

    private static ManejadorEvento instancia = null;

    /** Eventos indexados por nombre (su identificador). */
    private final Map<String, Evento> eventosPorNombre;

    private ManejadorEvento() {
        this.eventosPorNombre = new LinkedHashMap<>();
    }

    public static ManejadorEvento getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorEvento();
        }
        return instancia;
    }

    /** Agrega un evento. Asume que Sistema ya valido la unicidad del nombre. */
    public void agregar(Evento evento) {
        eventosPorNombre.put(evento.getNombre(), evento);
    }

    /** Devuelve el evento con ese nombre, o null si no existe. */
    public Evento buscar(String nombre) {
        return eventosPorNombre.get(nombre);
    }

    /** Todos los eventos de la coleccion. */
    public List<Evento> listar() {
        return new ArrayList<>(eventosPorNombre.values());
    }

    /**
     * Busca una edicion por nombre entre TODOS los eventos. Devuelve null si
     * ningun evento tiene una edicion con ese nombre.
     */
    public EdicionEvento buscarEdicion(String nombreEdicion) {
        for (Evento e : eventosPorNombre.values()) {
            EdicionEvento ed = e.buscarEdicion(nombreEdicion);
            if (ed != null) {
                return ed;
            }
        }
        return null;
    }
}
