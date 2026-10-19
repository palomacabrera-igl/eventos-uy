package logica;

import java.util.ArrayList;
import java.util.List;

/**
 * <<DataType>> DTCategoria del DSS de Alta de Categoria.
 *
 * Ademas del nombre, encapsula la coleccion de DTCategoria de sus hijas. Esta
 * estructura de DataTypes anidados (un DTCategoria que contiene otros
 * DTCategoria) es la forma de representar la relacion recursiva
 * Categoria -> 0..* categorias hijas en la informacion que viaja a la presentacion.
 */
public class DTCategoria {

    private String nombre;
    private List<DTCategoria> hijas;

    /** Categoria sin hijas (hoja o cuando la jerarquia no interesa). */
    public DTCategoria(String nombre) {
        this(nombre, new ArrayList<>());
    }

    public DTCategoria(String nombre, List<DTCategoria> hijas) {
        this.nombre = nombre;
        this.hijas = hijas;
    }

    public String getNombre() {
        return nombre;
    }

    public List<DTCategoria> getHijas() {
        return hijas;
    }
}
