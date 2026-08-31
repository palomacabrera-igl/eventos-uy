package logica;

import java.util.Set;

/**
 * Unica interfaz que conoce la capa de presentacion (GUI). No expone
 * objetos de dominio: todo lo que cruza hacia la GUI son DTs.
 */
public interface IControladorSistema {

    // ===== Modificar Datos de Usuario =====

    /** Precondicion: ninguna. */
    Set<DTUsuario> listarUsuarios();

    /** Precondicion: debe existir un Usuario con ese nickname. */
    DTUsuario seleccionarUsuario(String nickname);

    /**
     * Precondicion: debe haberse ejecutado seleccionarUsuario() previamente,
     * y dt debe corresponder al mismo nickname seleccionado.
     */
    void modificarDatosUsuario(DTUsuario dt);

    // ===== Consulta de Patrocinio =====

    /** Precondicion: ninguna. */
    Set<DTEvento> listarEventos();

    /** Precondicion: debe existir un Evento con ese nombre. */
    Set<DTEdicionEvento> listarEdicionesDeEvento(String nombreEvento);

    /** Precondicion: debe existir una EdicionEvento con ese nombre (del evento seleccionado). */
    Set<DTPatrocinio> listarPatrociniosDeEdicion(String nombreEdicion);

    /** Precondicion: debe existir un Patrocinio con ese codigo (de la edicion seleccionada). */
    DTPatrocinio mostrarPatrocinio(int codigoPatrocinio);

    // ===== Alta de Edicion de Evento =====
    // (listarEventos() ya esta declarado arriba, en Consulta de Patrocinio, y se reutiliza)

    /** Precondicion: debe existir un Evento con ese nombre. */
    DTEvento seleccionarEvento(String nombre);

    /** Precondicion: ninguna. */
    Set<DTOrganizador> listarOrganizadores();

    /** Precondicion: debe existir un Organizador con ese nickname. */
    DTOrganizador seleccionarOrganizador(String nickname);

    /**
     * Precondicion: deben haberse seleccionado previamente un evento y un
     * organizador (seleccionarEvento() y seleccionarOrganizador()).
     * Retorna false y no crea ninguna instancia si ya existe una
     * EdicionEvento con el mismo nombre; retorna true y da de alta la
     * edicion en caso contrario.
     */
    boolean ingresarDatosEdicion(DTEdicionEvento dt);

}