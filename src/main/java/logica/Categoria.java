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

/**
 * PRIMERA ENTIDAD MAPEADA CON JPA (prueba piloto del equipo).
 *
 * Se eligio Categoria para empezar porque es la mas simple del dominio: un
 * solo atributo y ninguna asociacion. Una vez que este patron se entiende,
 * se replica al resto de las entidades.
 *
 * Que hace cada anotacion:
 *  - @Entity  : le dice a JPA que esta clase se guarda en la base.
 *  - @Table   : nombre de la tabla (si no se pone, usa el nombre de la clase).
 *  - extends EntidadBase : hereda el campo id (@Id @GeneratedValue).
 *  - @Column  : reglas de la columna.
 */
@Entity
@Table(name = "categoria")
public class Categoria extends EntidadBase {

    /**
     * La letra pide que el nombre de la categoria sea "unico en la plataforma".
     *
     *  - unique = true    -> PostgreSQL crea una restriccion UNIQUE y rechaza
     *                        duplicados aunque el programa se equivoque.
     *  - nullable = false -> la columna no acepta NULL.
     *
     * Ademas ManejadorCategoria lo sigue validando ANTES de insertar. La letra
     * (seccion 7.1) pide justamente las dos cosas: las restricciones de
     * unicidad "deben estar reflejadas en el mapeo Y verificadas por la logica".
     */
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    /**
     * Auto-asociacion recursiva: una Categoria tiene 0..1 padre (null si es
     * raiz) y 0..* hijas. Es UNA SOLA TABLA con una columna padre_id que
     * apunta a la misma tabla.
     *
     * Es el mismo par @ManyToOne / @OneToMany(mappedBy) de la demo del profe
     * (Categoria 1--< Libro), pero apuntando a la propia clase.
     */
    @ManyToOne
    @JoinColumn(name = "padre_id",
                foreignKey = @ForeignKey(name = "fk_categoria_padre"))
    private Categoria padre;

    /**
     * Lado INVERSO (mappedBy): la FK vive en padre_id, no en una tabla aparte.
     *
     * OJO: NO puede ser 'final'. Al leer de la base, Hibernate reemplaza la
     * lista por una implementacion propia suya, y a un campo final no se le
     * puede asignar.
     */
    @OneToMany(mappedBy = "padre", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Categoria> hijas = new ArrayList<>();

    /**
     * Constructor sin argumentos: JPA lo NECESITA para poder crear la instancia
     * cuando lee una fila de la base. Puede ser protected, no hace falta que
     * sea public.
     */
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
