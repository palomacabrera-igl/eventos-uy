package logica;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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
     * Auto-asociacion recursiva (el profesor la pidio): una Categoria tiene
     * 0..1 categoria padre (null si es raiz) y 0..* categorias hijas.
     *
     * TODO (JPA): mapear la jerarquia con @ManyToOne (padre) y
     * @OneToMany(mappedBy = "padre") (hijas). Por ahora van como @Transient para
     * no romper el arranque de JPA mientras se termina de integrar la persistencia.
     */
    @Transient
    private Categoria padre;

    @Transient
    private final List<Categoria> hijas = new ArrayList<>();

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
