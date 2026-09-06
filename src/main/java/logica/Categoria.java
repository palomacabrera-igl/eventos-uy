package logica;

public class Categoria {
    private String nombre;

    public Categoria(String nombre) {this.nombre = nombre;}

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public DTCategoria obtenerDT() {
        return new DTCategoria(nombre);
    }
}
