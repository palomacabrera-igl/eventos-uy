package logica;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Unica interfaz que conoce la capa de presentacion (GUI). No expone
 * objetos de dominio: todo lo que cruza hacia la GUI son DTs.
 *
 * Las precondiciones que declara cada operacion se verifican en la capa
 * logica. Si no se cumplen —no existe lo que se pide, o falta una seleccion
 * previa— la operacion lanza ReglaNegocioException con un mensaje para
 * mostrarle al administrador, en lugar de fallar con NullPointerException.
 */
public interface IControladorSistema {

    // ===== Modificar Datos de Usuario =====

    /** Precondicion: ninguna. */
    Set<DTUsuario> listarUsuarios();

    /** Precondicion: debe existir un Usuario con ese nickname. */
    DTUsuario seleccionarUsuario(String nickname) throws ReglaNegocioException;

    /**
     * Precondicion: debe haberse ejecutado seleccionarUsuario() previamente,
     * y dt debe corresponder al mismo nickname seleccionado.
     */
    void modificarDatosUsuario(DTUsuario dt) throws ReglaNegocioException;

    // ===== Consulta de Patrocinio =====

    /** Precondicion: ninguna. */
    Set<DTEvento> listarEventos();

    /** Precondicion: debe existir un Evento con ese nombre. */
    Set<DTEdicionEvento> listarEdicionesDeEvento(String nombreEvento)
            throws ReglaNegocioException;

    /** Precondicion: debe existir una EdicionEvento con ese nombre (del evento seleccionado). */
    Set<DTPatrocinio> listarPatrociniosDeEdicion(String nombreEdicion)
            throws ReglaNegocioException;

    /** Precondicion: debe existir un Patrocinio con ese codigo (de la edicion seleccionada). */
    DTPatrocinio mostrarPatrocinio(String codigoPatrocinio)
            throws ReglaNegocioException;

    // ===== Alta de Edicion de Evento =====
    // (listarEventos() ya esta declarado arriba, en Consulta de Patrocinio, y se reutiliza)

    /** Precondicion: debe existir un Evento con ese nombre. */
    DTEvento seleccionarEvento(String nombre) throws ReglaNegocioException;

    /** Precondicion: ninguna. */
    Set<DTOrganizador> listarOrganizadores();

    /** Precondicion: debe existir un Organizador con ese nickname. */
    DTOrganizador seleccionarOrganizador(String nickname)
            throws ReglaNegocioException;

    /**
     * Precondicion: deben haberse seleccionado previamente un evento y un
     * organizador (seleccionarEvento() y seleccionarOrganizador()).
     */
    void ingresarDatosEdicion(DTEdicionEvento dt) throws ReglaNegocioException;

    // ===== Alta de Usuario =====

    /** Precondicion: tipo corresponde a asistente u organizador. */
    void ingresarDatosUsuario(DTUsuario datos, TipoUsuario tipo) throws ReglaNegocioException;

    /** Precondicion: ingresarDatosUsuario() se ejecuto con exito y el tipo recordado es ASISTENTE. */
    void ingresarDatosAsistente(String apellido, DTFecha fechaNac)
            throws ReglaNegocioException;

    /**
     * Precondicion: ingresarDatosAsistente() se ejecuto previamente durante
     * la misma alta, y existe una Institucion con ese nombre.
     */
    void seleccionarInstitucion(String nombreInstitucion)
            throws ReglaNegocioException;

    /** Precondicion: ingresarDatosUsuario() se ejecuto con exito y el tipo recordado es ORGANIZADOR. */
    void ingresarDatosOrganizador(String descripcion, String sitioWeb)
            throws ReglaNegocioException;

    /** Precondicion: ninguna. */
    Set<String> listarNombresInstituciones();

    // ===== Alta de Tipo de Registro =====
    // listarEventos() ya esta declarado arriba y se reutiliza; listarEdicionesDeEvento()
    // ya esta declarado arriba, en Consulta de Patrocinio, y tambien recuerda el Evento
    // seleccionado.
    /**
     * Precondicion: se ejecuto listarEdicionesDeEvento() previamente (recuerda
     * el Evento seleccionado), y existe una EdicionEvento con ese nombre
     * asociada a dicho evento.
     */
    DTEdicionEvento seleccionarEdicionEvento(String nombreEdicion)
            throws ReglaNegocioException;

    /** Precondicion: se ejecuto seleccionarEdicionEvento() previamente. */
    void ingresarDatosTipoRegistro(String nombre, String descripcion, double costo, int cupo)
            throws ReglaNegocioException;

    // ===== Consulta de Evento =====
    // listarEventos() ya esta declarado arriba y se reutiliza como consultarEvento();
    // seleccionarEvento() ya esta declarado arriba, en Alta de Edicion de Evento,
    // listarEdicionesDeEvento() ya esta declarado arriba, en Consulta de Patrocinio, y
    // seleccionarEdicionEvento() ya esta declarado arriba, en Alta de Tipo de Registro,
    // devolviendo el DTEdicionEvento que este caso de uso necesita mostrar.

    // ===== Registro a Edicion de Evento =====
    // (listarEventos() ya esta declarado arriba; listarEdicionesDeEvento() ya esta declarado
    // arriba, en Consulta de Patrocinio, y ademas recuerda el Evento seleccionado, asi que se reutiliza para
    // el combo de ediciones.)

    /**
     * Precondicion: se ejecuto listarEdicionesDeEvento() previamente (recuerda
     * el Evento seleccionado) y existe una EdicionEvento con ese nombre en dicho
     * evento. Recuerda la Edicion seleccionada. Retorna un DTDatosRegistro con
     * los tipos de registro de la edicion y todos los asistentes existentes.
     */
    DTDatosRegistro listarDatosRegistro(String nombreEdicion)
            throws ReglaNegocioException;

    /**
     * Precondicion: se ejecuto listarDatosRegistro() previamente (recuerda la
     * Edicion seleccionada); existen un Asistente con ese nickname y un
     * TipoRegistro con ese nombre en la edicion.
     */
    void altaRegistro(String nickname, String nombreEdicion, String nombreTipo)
            throws ReglaNegocioException;

    /**
     * Igual que altaRegistro(nickname, nombreEdicion, nombreTipo), pero el
     * asistente presenta un codigo de patrocinio y el registro no se cobra.
     *
     * @param codigoPatrocinio codigo que le dio su institucion; null si paga.
     * @throws ReglaNegocioException si el codigo no es de un patrocinio de esa
     *         edicion, si el asistente no pertenece a la institucion que lo
     *         otorga, si el tipo de registro no es el que el patrocinio regala,
     *         o si ya se usaron todos los registros gratuitos.
     */
    void altaRegistro(String nickname, String nombreEdicion, String nombreTipo,
                      String codigoPatrocinio) throws ReglaNegocioException;

    // ===== Consulta de Usuario =====

    /**
     * Precondicion: se ejecuto seleccionarUsuario() sobre un organizador
     * (Sistema recuerda el Organizador seleccionado). Lista sus ediciones.
     */
    Set<DTEdicionEvento> listarEdiciones() throws ReglaNegocioException;

    /**
     * Precondicion: se ejecuto listarEdiciones() previamente (recuerda el
     * Organizador seleccionado) y existe una EdicionEvento suya con ese nombre.
     */
    DTEdicionCompleto seleccionarEdicion(String nombreEdicion)
            throws ReglaNegocioException;

    /** Precondicion: existe un Asistente con ese nickname. */
    Set<DTRegistro> listarRegistroUsuario(String nickname)
            throws ReglaNegocioException;

    /**
     * Precondicion: se ejecuto seleccionarUsuario() sobre un asistente
     * (Sistema recuerda el Asistente seleccionado) y ese asistente tiene un
     * Registro en la edicion 'nombreEdicion'.
     */
    DTRegistro obtenerRegistro(String nombreEdicion) throws ReglaNegocioException;

    // ===== Consulta de Registro =====
    // (listarUsuarios() y listarRegistroUsuario(nickname) ya estan declarados. Para llenar
    // los registros de un usuario, la capa de presentacion llama antes a seleccionarUsuario(nickname),
    // que deja recordado el asistente seleccionado del que listarRegistroUsuario() toma los registros.

    /**
     * Precondicion: existe un Asistente con ese nickname y un Registro suyo en
     * la edicion 'nombre'. Retorna el DTRegistro detallado (edicion, tipo de
     * registro, costo y fecha) de ese registro.
     *
     * Sobrecarga: convive con obtenerRegistro(nombreEdicion), que usa el
     * asistente ya seleccionado en vez de recibir el nickname.
     */
    DTRegistro obtenerRegistro(String nickname, String nombre)
            throws ReglaNegocioException;

    // ===== Alta de Categoria =====

    /** Precondicion: ninguna. */
    Set<DTCategoria> listarCategorias();

    /**
     * Precondicion: ninguna.*/
    Set<DTCategoria> listarCategoriasArbol();

    /**
     * Precondicion: si nombrePadre no es null ni vacio, debe existir una Categoria
     * con ese nombre (sera la categoria padre). Si ya existe una Categoria con
     * 'nombre' (unico en la plataforma) retorna ERROR y no crea nada; en caso
     * contrario la crea (como raiz si nombrePadre es null/vacio, o como hija de la
     * categoria padre) y retorna OK.
     */
    void altaCategoria(String nombre, String nombrePadre) throws ReglaNegocioException;

    // ===== Consulta de Tipo de Registro =====
    Set<DTTipoRegistro> listarTiposRegistroDeEdicion(String nombreEdicion)
            throws ReglaNegocioException;

    DTTipoRegistro seleccionarTipoRegistro(String nombreTipoRegistro)
            throws ReglaNegocioException;

    // ===== Alta Institucion  =====
    void altaInstitucion(String nombre, String descripcion, String sitioWeb)
            throws ReglaNegocioException;

    // ===== Consulta de Edicion de Evento =====

    /**
     * Precondicion: se ejecuto listarEdicionesDeEvento() o seleccionarEvento()
     * previamente (Sistema recuerda el Evento seleccionado), y existe una
     * EdicionEvento con ese nombre dentro de ese evento.
     */
    DTEdicionCompleto seleccionarEdicionCompleta(String nombreEdicion)
            throws ReglaNegocioException;

    // ===== Alta de Patrocinio =====

    /**
     * Precondicion: se ejecuto listarTiposRegistroDeEdicion() previamente
     * (Sistema recuerda la EdicionEvento), existe una Institucion con el
     * nombre de dt.institucion y un TipoRegistro con el nombre de
     * dt.tipoRegistro en esa edicion.
     *
     * Postcondicion: se crea un Patrocinio asociado a la edicion recordada,
     * a la institucion y al tipo de registro indicados.
     *
     * @throws ReglaNegocioException si la institucion ya tiene un patrocinio
     *         para esa edicion, o si el valor de los registros gratuitos
     *         supera el 20% del aporte economico.
     */
    void altaPatrocinio(DTPatrocinio dt) throws ReglaNegocioException;
    void ingresarDatosEvento(String nombre, String descripcion, LocalDate fechaAlta,
                             String sigla, List<String> nombresCategorias)
            throws ReglaNegocioException;

    // =================================================================
    //  OPERACIONES PARA EL SITIO WEB
    // =================================================================
    //
    //  Se agregan sin tocar las de arriba, que son las del Laboratorio 1 y
    //  las sigue usando la Estacion de Trabajo.
    //
    //  Dos diferencias con aquellas:
    //
    //  1. Reciben TODO lo que necesitan en una sola llamada. No dependen de
    //     que antes se haya llamado a un seleccionar*, porque en la web cada
    //     peticion llega sola y no hay forma de saber si la anterior fue del
    //     mismo usuario.
    //
    //  2. Varias reciben "solicitante": el nickname de quien tiene la sesion
    //     iniciada, o null si es un visitante. Es lo que le permite a la
    //     LOGICA decidir que datos devuelve. La letra (7.4) no deja que esa
    //     decision la tome la pagina: si la JSP esconde un dato, el dato
    //     igual viajo hasta el navegador.

    // ----- Sesion y usuarios (CU 1 a 5) -----

    /**
     * Inicio de Sesion (CU 1). El identificador puede ser el nickname o el
     * correo electronico, como pide la letra.
     *
     * @throws ReglaNegocioException con un mensaje unico si los datos no
     *         corresponden a un usuario. A proposito no distingue entre
     *         usuario inexistente y contrasena incorrecta.
     */
    DTUsuario iniciarSesion(String identificador, String password)
            throws ReglaNegocioException;

    /**
     * Alta de Usuario (CU 3), en una sola llamada. Hoy el mismo alta son tres
     * llamadas encadenadas que dependen del estado del controlador.
     *
     * La contrasena va en TEXTO PLANO: la entidad la hashea sola.
     * nombreInstitucion e imagen pueden ser null: los dos son opcionales.
     */
    void altaUsuario(DTUsuario datos, TipoUsuario tipo, String password,
                     String confirmacion, String nombreInstitucion, DTImagen imagen)
            throws ReglaNegocioException;

    /** Consulta de Usuario (CU 4). El perfil basico, sin la contrasena. */
    DTUsuario consultarUsuario(String nickname, String solicitante)
            throws ReglaNegocioException;

    /**
     * Las ediciones de un organizador (CU 4). Devuelve solo las Aceptada,
     * salvo que el organizador este consultando su propio perfil: ahi vienen
     * las tres, como pide la letra.
     */
    Set<DTEdicionEvento> listarEdicionesDeOrganizador(String nickname, String solicitante)
            throws ReglaNegocioException;

    /**
     * Los registros de un asistente (CU 4). Solo cuando el asistente consulta
     * su propio perfil; para cualquier otro devuelve la lista vacia.
     */
    Set<DTRegistro> listarRegistrosDeAsistente(String nickname, String solicitante)
            throws ReglaNegocioException;

    /**
     * Modificar Datos de Usuario (CU 5). El nickname y el correo no se
     * modifican nunca.
     *
     * nuevaPassword y confirmacion pueden venir vacias: significa que no se
     * cambia la contrasena. Si vienen, tienen que coincidir.
     */
    void modificarUsuario(String solicitante, DTUsuario datos, String nuevaPassword,
                          String confirmacion, DTImagen imagen)
            throws ReglaNegocioException;

    /**
     * Los bytes de la imagen de un usuario, para el servlet que las entrega.
     * Devuelve null si no tiene.
     */
    DTImagen obtenerImagenUsuario(String nickname) throws ReglaNegocioException;

    /** Asigna o reemplaza la imagen de un usuario existente. */
    void asignarImagenUsuario(String nickname, DTImagen imagen)
            throws ReglaNegocioException;

    // ----- Eventos y ediciones (CU 6 a 9) -----

    /**
     * Los eventos de una categoria (CU 7). Con null o vacio devuelve todos,
     * asi el servlet llama siempre a la misma operacion: el cabezal manda la
     * categoria elegida, o nada cuando se entra a la cartelera.
     */
    Set<DTEvento> listarEventos(String nombreCategoria);

    /** Consulta de Evento (CU 7): los datos del evento, sin sus ediciones. */
    DTEvento consultarEvento(String nombre) throws ReglaNegocioException;

    /**
     * Las ediciones publicas de un evento (CU 7 y 9): SOLO las Aceptada.
     * Las Ingresada y Rechazada se piden por listarEdicionesDeOrganizador,
     * que decide segun quien pregunta.
     */
    Set<DTEdicionEvento> listarEdicionesAceptadas(String nombreEvento)
            throws ReglaNegocioException;

    /**
     * Alta de Evento (CU 6). La fecha de alta la pone el sistema, no el
     * formulario. La imagen puede ser null. Solo un organizador puede.
     */
    void altaEvento(String solicitante, String nombre, String descripcion, String sigla,
                    List<String> nombresCategorias, DTImagen imagen)
            throws ReglaNegocioException;

    /**
     * Alta de Edicion de Evento (CU 8). La edicion nace INGRESADA y queda
     * asociada al organizador que inicio sesion; la fecha de alta la pone el
     * sistema. Del DT se ignoran el estado y la fecha de alta si vinieran.
     */
    void altaEdicion(String solicitante, String nombreEvento, DTEdicionEvento datos,
                     DTImagen imagen)
            throws ReglaNegocioException;

    /**
     * Consulta de Edicion de Evento (CU 9), con sus tipos de registro,
     * patrocinios y registros.
     *
     * Quien pregunta cambia lo que recibe: una edicion que no esta Aceptada
     * solo la ve su organizador; el listado de registros, solo su
     * organizador; el codigo de patrocinio, solo su organizador.
     */
    DTEdicionCompleto consultarEdicion(String nombreEdicion, String solicitante)
            throws ReglaNegocioException;

    /** Los bytes de la imagen de un evento, o null si no tiene. */
    DTImagen obtenerImagenEvento(String nombreEvento) throws ReglaNegocioException;

    /** Los bytes de la imagen de una edicion, o null si no tiene. */
    DTImagen obtenerImagenEdicion(String nombreEdicion) throws ReglaNegocioException;

    /** Asigna o reemplaza la imagen de un evento existente. */
    void asignarImagenEvento(String nombreEvento, DTImagen imagen)
            throws ReglaNegocioException;

    /** Asigna o reemplaza la imagen de una edicion existente. */
    void asignarImagenEdicion(String nombreEdicion, DTImagen imagen)
            throws ReglaNegocioException;

    // ----- Tipos de registro e instituciones (CU 10, 11 y 14) -----

    /**
     * Alta de Tipo de Registro (CU 10). La edicion puede estar en cualquier
     * estado, pero tiene que ser del organizador que pide.
     */
    void altaTipoRegistro(String solicitante, String nombreEdicion, String nombre,
                          String descripcion, double costo, int cupo)
            throws ReglaNegocioException;

    /** Consulta de Tipo de Registro (CU 11). */
    DTTipoRegistro consultarTipoRegistro(String nombreEdicion, String nombreTipo)
            throws ReglaNegocioException;

    /** Alta de Institucion (CU 14). Solo un organizador puede; la imagen es opcional. */
    void altaInstitucion(String solicitante, String nombre, String descripcion,
                         String sitioWeb, DTImagen imagen)
            throws ReglaNegocioException;

    /**
     * Las instituciones con todos sus datos. La que ya existia,
     * listarNombresInstituciones(), sigue sirviendo para los combos.
     */
    Set<DTInstitucion> listarInstituciones();

    /** Los datos de una institucion, para la ficha de patrocinio. */
    DTInstitucion consultarInstitucion(String nombre) throws ReglaNegocioException;

    /** Los bytes de la imagen de una institucion, o null si no tiene. */
    DTImagen obtenerImagenInstitucion(String nombre) throws ReglaNegocioException;

    /** Asigna o reemplaza la imagen de una institucion existente. */
    void asignarImagenInstitucion(String nombre, DTImagen imagen)
            throws ReglaNegocioException;

    // ----- Registro a Edicion de Evento (CU 12) -----
    //
    //  No lleva operaciones nuevas: altaRegistro(nickname, edicion, tipo,
    //  codigo) ya recibe todo de una vez y valida cupo, registro duplicado y
    //  codigo de patrocinio. Lo que falta es dentro: exigir que la edicion
    //  este ACEPTADA y que el asistente sea quien tiene la sesion.

    // ----- Consulta de Registro (CU 13) -----

    /**
     * Los registros de una edicion, para su organizador (CU 13). Si quien
     * pregunta no es el organizador de esa edicion, es un error.
     */
    Set<DTRegistro> listarRegistrosDeEdicion(String nombreEdicion, String solicitante)
            throws ReglaNegocioException;

    /**
     * El detalle de un registro (CU 13). Lo pueden ver el asistente duenio del
     * registro y el organizador de la edicion, nadie mas.
     */
    DTRegistro consultarRegistro(String nombreEdicion, String nickname, String solicitante)
            throws ReglaNegocioException;

    // ----- Patrocinios (CU 15 y 16) -----

    /**
     * Alta de Patrocinio (CU 15), sin depender de una edicion seleccionada
     * antes. Ademas de las reglas que ya estaban (una institucion no patrocina
     * dos veces la misma edicion, y los registros gratuitos no superan el 20%
     * del aporte), tiene que rechazar un codigo que ya exista en la
     * plataforma: hoy eso solo lo frena la base de datos.
     */
    void altaPatrocinio(String solicitante, String nombreEdicion, DTPatrocinio datos)
            throws ReglaNegocioException;

    /**
     * Los patrocinios de una edicion (CU 16). El codigo de patrocinio viene
     * SOLO si quien pregunta es el organizador de esa edicion; para el resto
     * viaja vacio, porque la letra no lo publica.
     */
    Set<DTPatrocinio> listarPatrociniosDeEdicion(String nombreEdicion, String solicitante)
            throws ReglaNegocioException;

    /** Un patrocinio concreto (CU 16), con la misma regla del codigo. */
    DTPatrocinio consultarPatrocinio(String nombreEdicion, String nombreInstitucion,
                                     String solicitante)
            throws ReglaNegocioException;

    // ----- Aceptar/Rechazar Edicion de Evento (Estacion de Trabajo) -----

    /** Las ediciones de un evento que estan INGRESADA, para que el administrador elija. */
    Set<DTEdicionEvento> listarEdicionesIngresadas(String nombreEvento)
            throws ReglaNegocioException;

    /**
     * Acepta una edicion: pasa a ACEPTADA y desde ese momento se lista al
     * publico. Solo se puede desde INGRESADA.
     */
    void aceptarEdicion(String nombreEdicion) throws ReglaNegocioException;

    /** Rechaza una edicion. Solo se puede desde INGRESADA. */
    void rechazarEdicion(String nombreEdicion) throws ReglaNegocioException;

    // ----- Mantenimiento -----

    /**
     * Descarta lo que la logica tiene guardado en memoria para que la proxima
     * consulta vuelva a leer de la base.
     *
     * Hace falta porque el sitio y la Estacion de Trabajo son dos programas
     * distintos sobre la misma base. En la Estacion de Trabajo la dispara el
     * boton "Actualizar datos"; en el sitio, un filtro al empezar cada pagina.
     *
     * Esta en el contrato para que el modulo web no tenga que conocer la capa
     * de persistencia: habla solo con IControladorSistema y con los DT.
     */
    void refrescarDatos();

    /**
     * Empieza una unidad de trabajo: espera el turno, refresca y queda listo
     * para leer. La usa el sitio al empezar cada pagina.
     *
     * Hace falta porque la logica tiene un solo EntityManager y Tomcat atiende
     * cada peticion en un hilo distinto. Sin esto, dos paginas pedidas a la
     * vez se pisan.
     */
    void abrirUnidadDeTrabajo();

    /**
     * Termina la unidad de trabajo. SIEMPRE se llama desde un finally: si una
     * pagina falla y no se cierra, el sitio queda colgado para todos.
     */
    void cerrarUnidadDeTrabajo();
}
