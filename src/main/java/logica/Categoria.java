package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
public class Categoria extends EntidadBase {

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    /**
     * Auto-asociacion recursiva: una Categoria tiene 0..1 padre (null si es
     * raiz) y 0..* hijas. Es una tabla con una columna padre_id que
     * apunta a la misma tabla.
     *
     */
    @ManyToOne
    @JoinColumn(name = "padre_id",
                foreignKey = @ForeignKey(name = "fk_categoria_padre"))
    private Categoria padre;

    /**
     * Lado INVERSO (mappedBy): la FK vive en padre_id, no en una tabla aparte.
     */
    @OneToMany(mappedBy = "padre", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Categoria> hijas = new ArrayList<>();

    protected Categoria() {}

    public Categoria(String nombre) {this.nombre = nombre;}

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public List<Categoria> getHijas() {return hijas;}

    /** true si es una categoria raiz (no tiene padre). */
    public boolean esRaiz() {return padre == null;}

    /** Cuelga una categoria como hija de esta (y registra a esta como su padre). */
    public void agregarHija(Categoria hija) {
        hija.padre = this;
        hijas.add(hija);
    }

    /**
     * Arma el DTCategoria de forma RECURSIVA: incluye, anidados, los DTCategoria
     * de todas sus hijas (y estas los de las suyas, etc.). Asi la estructura de
     * DataTypes que viaja a la presentacion refleja la jerarquia completa.
     */
    public DTCategoria obtenerDT() {
        List<DTCategoria> dtHijas = new ArrayList<>();
        for (Categoria hija : hijas) {
            dtHijas.add(hija.obtenerDT());   // recursion
        }
        return new DTCategoria(nombre, dtHijas);
    }
}
