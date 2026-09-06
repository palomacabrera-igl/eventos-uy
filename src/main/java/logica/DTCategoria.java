package logica;

/**
 * <<DataType>> DTCategoria del DSS de Alta de Categoria.
 * Una categoria de la plataforma se identifica solo por su nombre.
 */
public class DTCategoria {

    private String nombre;

    public DTCategoria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
