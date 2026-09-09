package logica;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Clase base de todas las entidades persistentes.
 *
 * &#64;MappedSuperclass: NO genera una tabla propia. Lo unico que hace es
 * "prestarle" el campo id a cada clase que la extienda, para no repetir las
 * mismas tres lineas en las diez entidades del dominio.
 *
 * Mismo patron que la clase EntidadBase de la demo ProyectoJPA del curso.
 */
@MappedSuperclass
public abstract class EntidadBase {

    /**
     * Clave primaria tecnica (subrogada). La genera la base de datos:
     * IDENTITY = columna autoincremental de PostgreSQL (BIGSERIAL).
     *
     * Ojo: esto NO reemplaza a las claves del dominio (nickname, nombre de
     * evento, nombre de edicion...). Esas siguen siendo unicas y se marcan
     * con @Column(unique = true) en cada entidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }
}
