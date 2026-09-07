package logica;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador de la coleccion de Categoria (patron "collection object" de GRASP).
 *
 * Singleton, mismo patron que ManejadorUsuario de la demo ProyectoSwing.
 * Responsabilidad: guardar, buscar y listar Categoria.
 */
public class ManejadorCategoria {

    private static ManejadorCategoria instancia = null;

    /** Categorias indexadas por nombre (su identificador). */
    private final Map<String, Categoria> categoriasPorNombre;

    private ManejadorCategoria() {
        this.categoriasPorNombre = new LinkedHashMap<>();
    }

    public static ManejadorCategoria getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorCategoria();
        }
        return instancia;
    }

    /** Agrega una categoria. Asume que Sistema ya valido la unicidad del nombre. */
    public void agregar(Categoria categoria) {
        categoriasPorNombre.put(categoria.getNombre(), categoria);
    }

    /** Devuelve la categoria con ese nombre, o null si no existe. */
    public Categoria buscar(String nombre) {
        return categoriasPorNombre.get(nombre);
    }

    /** Todas las categorias de la coleccion. */
    public List<Categoria> listar() {
        return new ArrayList<>(categoriasPorNombre.values());
    }
}
