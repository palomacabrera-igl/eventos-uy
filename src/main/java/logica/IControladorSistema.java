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

    // ===== Alta de Usuario =====

    /** Precondicion: tipo corresponde a asistente u organizador. */
    boolean ingresarDatosUsuario(DTUsuario datos, TipoUsuario tipo);

    /** Precondicion: ingresarDatosUsuario() se ejecuto con exito y el tipo recordado es ASISTENTE. */
    void ingresarDatosAsistente(String apellido, DTFecha fechaNac);

    /**
     * Precondicion: ingresarDatosAsistente() se ejecuto previamente durante
     * la misma alta, y existe una Institucion con ese nombre.
     */
    void seleccionarInstitucion(String nombreInstitucion);

    /** Precondicion: ingresarDatosUsuario() se ejecuto con exito y el tipo recordado es ORGANIZADOR. */
    void ingresarDatosOrganizador(String descripcion, String sitioWeb);

    /** Precondicion: ninguna. */
    Set<String> listarNombresInstituciones();

    // ===== Alta de Tipo de Registro =====
    // (listarEventos() ya esta declarado arriba y se reutiliza; listarEdicionesDeEvento()
    // ya esta declarado arriba, en Consulta de Patrocinio, y tambien recuerda el Evento
    // seleccionado, asi que no hace falta un seleccionarEvento() aparte aca)

    /**
     * Precondicion: se ejecuto listarEdicionesDeEvento() previamente (recuerda
     * el Evento seleccionado), y existe una EdicionEvento con ese nombre
     * asociada a dicho evento.
     *
     * Nota: el DSS de este caso de uso (y tambien el de Consulta de Evento,
     * que reutiliza esta misma operacion) la llama "seleccionarEdicion", pero
     * ese nombre ya lo usa Consulta de Usuario con otra firma (retorna
     * DTEdicionCompleto en vez de DTEdicionEvento, y busca dentro del
     * organizador seleccionado en vez del evento seleccionado). Java no
     * permite dos metodos iguales que solo difieran en el tipo de retorno,
     * asi que esta operacion se llama seleccionarEdicionEvento() en el codigo.
     */
    DTEdicionEvento seleccionarEdicionEvento(String nombreEdicion);

    /** Precondicion: se ejecuto seleccionarEdicionEvento() previamente. */
    boolean ingresarDatosTipoRegistro(String nombre, String descripcion, double costo, int cupo);

    // ===== Consulta de Evento =====
    // (listarEventos() ya esta declarado arriba y se reutiliza como consultarEvento();
    // seleccionarEvento() ya esta declarado arriba, en Alta de Edicion de Evento, con la
    // misma firma y semantica exacta que pide este DSS; listarEdicionesDeEvento() ya esta
    // declarado arriba, en Consulta de Patrocinio, para llenar el combo de ediciones; y
    // seleccionarEdicionEvento() ya esta declarado arriba, en Alta de Tipo de Registro,
    // devolviendo el DTEdicionEvento que este caso de uso necesita mostrar. No hace falta
    // ningun metodo nuevo para Consulta de Evento.

    // ===== Registro a Edicion de Evento =====
    // (listarEventos() ya esta declarado arriba y se reutiliza para llenar el combo
    // de eventos; listarEdicionesDeEvento() ya esta declarado arriba, en Consulta de
    // Patrocinio, y ademas recuerda el Evento seleccionado, asi que se reutiliza para
    // el combo de ediciones.)

    /**
     * Precondicion: se ejecuto listarEdicionesDeEvento() previamente (recuerda
     * el Evento seleccionado) y existe una EdicionEvento con ese nombre en dicho
     * evento. Recuerda la Edicion seleccionada. Retorna un DTDatosRegistro con
     * los tipos de registro de la edicion y todos los asistentes existentes.
     */
    DTDatosRegistro listarDatosRegistro(String nombreEdicion);

    /**
     * Precondicion: se ejecuto listarDatosRegistro() previamente (recuerda la
     * Edicion seleccionada); existen un Asistente con ese nickname y un
     * TipoRegistro con ese nombre en la edicion.
     * Retorna ERROR y no crea nada si el asistente ya esta registrado en la
     * edicion o el tipo de registro no tiene cupo; en caso contrario crea el
     * registro (fecha actual, costo = costo del tipo) y retorna OK.
     */
    Status altaRegistro(String nickname, String nombreEdicion, String nombreTipo);

    // ===== Consulta de Usuario =====

    Set<DTEdicionEvento> listarEdiciones();

    DTEdicionCompleto seleccionarEdicion(String nombreEdicion);

    Set<DTRegistro> listarRegistroUsuario(String nickname);

    DTRegistro obtenerRegistro(String nombreEdicion);

    // ===== Consulta de Registro =====
    // (listarUsuarios() y listarRegistroUsuario(nickname) ya estan declarados arriba
    // y se reutilizan. Para llenar los registros de un usuario, la capa de
    // presentacion llama antes a seleccionarUsuario(nickname), que deja recordado
    // el asistente seleccionado del que listarRegistroUsuario() toma los registros.)

    /**
     * Precondicion: existe un Asistente con ese nickname y un Registro suyo en
     * la edicion 'nombre'. Retorna el DTRegistro detallado (edicion, tipo de
     * registro, costo y fecha) de ese registro.
     *
     * Sobrecarga propia de Consulta de Registro (su DSS pide
     * obtenerRegistro(nickname, nombre)); no reemplaza al obtenerRegistro(nombreEdicion)
     * de Consulta de Usuario, convive con el como sobrecarga.
     */
    DTRegistro obtenerRegistro(String nickname, String nombre);

    // ===== Alta de Categoria =====

    /** Precondicion: ninguna. Retorna las categorias existentes en la plataforma. */
    Set<DTCategoria> listarCategorias();

    /**
     * Precondicion: ninguna. Si ya existe una Categoria con ese nombre retorna
     * ERROR y no crea nada; en caso contrario crea la Categoria (c.nombre =
     * nombre) y retorna OK.
     */
    Status altaCategoria(String nombre);

    // ===== Consulta de Tipo de Registro =====
    Set<DTTipoRegistro> listarTiposRegistroDeEdicion(String nombreEdicion);

    DTTipoRegistro seleccionarTipoRegistro(String nombreTipoRegistro);

    // ===== Alta Institucion  =====
    Status altaInstitucion(String nombre, String descripcion, String sitioWeb);

    // ===== Consulta de Edicion de Evento =====

    /**
     * Precondicion: se ejecuto listarEdicionesDeEvento() o seleccionarEvento()
     * previamente (Sistema recuerda el Evento seleccionado), y existe una
     * EdicionEvento con ese nombre dentro de ese evento.
     *
     * Nota: el DSS de este caso de uso la llama "seleccionarEdicion", pero ese
     * nombre ya lo usa Consulta de Usuario, que busca la edicion dentro del
     * organizador seleccionado en vez de dentro del evento seleccionado.
     *
     * Ademas de retornar el DT, Sistema retiene la edicion como
     * edicionSeleccionada.
     */
    DTEdicionCompleto seleccionarEdicionCompleta(String nombreEdicion);
}
