package logica;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador de la coleccion de Institucion (patron "collection object" de GRASP).
 *
 * Singleton, mismo patron que ManejadorUsuario de la demo ProyectoSwing.
 * Responsabilidad: guardar, buscar y listar Institucion.
 */
public class ManejadorInstitucion {

    private static ManejadorInstitucion instancia = null;

    /** Instituciones indexadas por nombre (su identificador). */
    private final Map<String, Institucion> institucionesPorNombre;

    private ManejadorInstitucion() {
        this.institucionesPorNombre = new LinkedHashMap<>();
    }

    public static ManejadorInstitucion getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorInstitucion();
        }
        return instancia;
    }

    /** Agrega una institucion. Asume que Sistema ya valido la unicidad del nombre. */
    public void agregar(Institucion institucion) {
        institucionesPorNombre.put(institucion.getNombre(), institucion);
    }

    /** Devuelve la institucion con ese nombre, o null si no existe. */
    public Institucion buscar(String nombre) {
        return institucionesPorNombre.get(nombre);
    }

    /** Todas las instituciones de la coleccion. */
    public List<Institucion> listar() {
        return new ArrayList<>(institucionesPorNombre.values());
    }
}
