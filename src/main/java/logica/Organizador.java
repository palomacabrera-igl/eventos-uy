package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario de tipo organizador. Ademas de los datos de {@link Usuario}, tiene
 * una descripcion general y un enlace a su sitio web, que puede no estar
 * definido
 */
@Entity
@DiscriminatorValue("ORGANIZADOR")
public class Organizador extends Usuario {

    /** Sin nullable = false: en las filas de Asistente esta columna va vacia. */
    @Column(length = 500)
    private String descripcion;

    @Column(name = "sitio_web", length = 300)
    private String sitioWeb;

    /** Lado INVERSO: la FK organizador_id vive en la tabla edicion_evento. */
    @OneToMany(mappedBy = "organizador", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EdicionEvento> ediciones = new  ArrayList<>();

    protected Organizador() {}

    public Organizador(String nickname, String nombre, String correoElectronico,
                       String descripcion, String sitioWeb) {
        super(nickname, nombre, correoElectronico);
        this.descripcion = descripcion;
        this.sitioWeb = sitioWeb;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    @Override
    public DTOrganizador obtenerDT() {
        return new DTOrganizador(getNickname(), getNombre(), getCorreoElectronico(),
                descripcion, sitioWeb);
    }

    @Override
    public TipoUsuario obtenerTipoUsuario() {
        return TipoUsuario.ORGANIZADOR;
    }

    @Override
    public void modificarDatos(DTUsuario dt) {
        super.modificarDatos(dt);
        DTOrganizador dorg = (DTOrganizador) dt;
        this.descripcion = dorg.getDescripcion();
        this.sitioWeb = dorg.getSitioWeb();
    }
    public void agregarEdicion(EdicionEvento edicion) {
        ediciones.add(edicion);
    }

    public List<EdicionEvento> getEdiciones() {
        return ediciones;
    }

    public EdicionEvento buscarEdicion(String nombre) {
        for (EdicionEvento ed : ediciones) {
            if (ed.getNombre().equals(nombre)) {
                return ed;
            }
        }
        return null; // si no se encuentra la edición
    }



}