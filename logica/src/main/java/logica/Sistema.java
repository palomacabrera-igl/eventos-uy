package logica;

import persistencia.Persistencia;

import java.time.LocalDate;
import java.util.*;

/**
 * Controlador del sistema (patron GRASP Controller, mapea a :Sistema en
 * los diagramas).
 *
 * Sistema solo coordina el caso de uso: valida reglas de negocio y guarda
 * las referencias retenidas entre llamados.
 */
public class Sistema implements IControladorSistema {

    private final ManejadorCategoria manejadorCategoria;
    private final ManejadorUsuario manejadorUsuario;
    private final ManejadorInstitucion manejadorInstitucion;
    private final ManejadorEvento manejadorEvento;

    // Referencias retenidas entre llamados
    private Usuario usuarioSeleccionado;
    private Evento eventoSeleccionado;
    private EdicionEvento edicionSeleccionada;
    private Organizador organizadorSeleccionado;
    private Asistente asistenteSeleccionado;

    // Referencias retenidas durante un alta de usuario en curso
    private DTUsuario datosUsuarioRecordados;
    private TipoUsuario tipoUsuarioRecordado;
    private Asistente asistenteRecordado;

    public Sistema() {
        this.manejadorCategoria = ManejadorCategoria.getInstancia();
        this.manejadorUsuario = ManejadorUsuario.getInstancia();
        this.manejadorInstitucion = ManejadorInstitucion.getInstancia();
        this.manejadorEvento = ManejadorEvento.getInstancia();
        // Los datos de prueba ya NO se cargan aca: son un programa aparte
        // (persistencia.CargarDatos). Con JPA quedan guardados en la base, asi
        // que recrearlos en cada arranque daria nombres repetidos.
    }

    // ===== Modificar Datos de Usuario =====

    @Override
    public Set<DTUsuario> listarUsuarios() {
        // 1*[foreach]: u := next()  /  2*: dt := obtenerDT()
        Set<DTUsuario> resultado = new HashSet<>();
        for (Usuario u : manejadorUsuario.listar()) {
            resultado.add(u.obtenerDT());
        }
        return resultado;
    }

    @Override
    public DTUsuario seleccionarUsuario(String nickname) throws ReglaNegocioException {
        Usuario u = find(nickname);
        if (u == null) {
            throw new ReglaNegocioException(
                    "No existe un usuario con el nickname \"" + nickname + "\".");
        }
        this.usuarioSeleccionado = u;

        if (u instanceof Asistente) {
            this.asistenteSeleccionado = (Asistente) u;
            this.organizadorSeleccionado = null; // limpiar si antes había uno
        } else if (u instanceof Organizador) {
            this.organizadorSeleccionado = (Organizador) u;
            this.asistenteSeleccionado = null; // limpiar si antes había uno
        }

        return u.obtenerDT();
    }


    @Override
    public void modificarDatosUsuario(DTUsuario dt) throws ReglaNegocioException {
        if (usuarioSeleccionado == null) {
            throw new ReglaNegocioException(
                    "Primero hay que seleccionar el usuario a modificar.");
        }
        // 1: usuarioSeleccionado.modificarDatos(dt)
        usuarioSeleccionado.modificarDatos(dt);
        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorUsuario.actualizar(usuarioSeleccionado);
    }

    /** Busqueda de Usuario por nickname (delega en ManejadorUsuario). */
    private Usuario find(String nickname) {
        return manejadorUsuario.buscar(nickname);
    }

    // ===== Consulta de Patrocinio =====

    @Override
    public Set<DTEvento> listarEventos() {
        Set<DTEvento> resultado = new HashSet<>();
        for (Evento e : manejadorEvento.listar()) {
            resultado.add(e.obtenerDT());
        }
        return resultado;
    }

    @Override
    public Set<DTEdicionEvento> listarEdicionesDeEvento(String nombreEvento)
            throws ReglaNegocioException {
        Evento e = findEvento(nombreEvento);
        if (e == null) {
            throw new ReglaNegocioException(
                    "No existe un evento con el nombre \"" + nombreEvento + "\".");
        }
        this.eventoSeleccionado = e;
        return e.obtenerEdiciones();
    }

    @Override
    public Set<DTPatrocinio> listarPatrociniosDeEdicion(String nombreEdicion)
            throws ReglaNegocioException {
        EdicionEvento ed = buscarEdicionDelEvento(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerPatrocinios();
    }

    @Override
    public DTPatrocinio mostrarPatrocinio(String codigoPatrocinio)
            throws ReglaNegocioException {
        if (edicionSeleccionada == null) {
            throw new ReglaNegocioException("Primero hay que seleccionar una edicion.");
        }
        Patrocinio p = edicionSeleccionada.buscarPatrocinio(codigoPatrocinio);
        if (p == null) {
            throw new ReglaNegocioException("La edicion " + edicionSeleccionada.getNombre()
                    + " no tiene un patrocinio con el codigo " + codigoPatrocinio + ".");
        }
        return p.obtenerDT();
    }

    /** Busqueda de Evento por nombre (delega en ManejadorEvento). */
    private Evento findEvento(String nombre) {
        return manejadorEvento.buscar(nombre);
    }

    // ===== Alta de Edicion de Evento =====

    @Override
    public DTEvento seleccionarEvento(String nombre) throws ReglaNegocioException {
        Evento e = findEvento(nombre);
        if (e == null) {
            throw new ReglaNegocioException(
                    "No existe un evento con el nombre \"" + nombre + "\".");
        }
        this.eventoSeleccionado = e;
        return e.obtenerDT();
    }

    @Override
    public Set<DTOrganizador> listarOrganizadores() {
        Set<DTOrganizador> resultado = new HashSet<>();
        for (Usuario u : manejadorUsuario.listar()) {
            if (u.obtenerTipoUsuario() == TipoUsuario.ORGANIZADOR) {
                resultado.add((DTOrganizador) u.obtenerDT());
            }
        }
        return resultado;
    }

    @Override
    public DTOrganizador seleccionarOrganizador(String nickname)
            throws ReglaNegocioException {
        Usuario u = find(nickname);
        if (!(u instanceof Organizador)) {
            throw new ReglaNegocioException(
                    "No existe un organizador con el nickname \"" + nickname + "\".");
        }
        this.organizadorSeleccionado = (Organizador) u;
        return (DTOrganizador) u.obtenerDT();
    }

    @Override
    public void ingresarDatosEdicion(DTEdicionEvento dt) throws ReglaNegocioException {
        // Una edicion no puede repetir nombre en NINGUN evento.
        if (manejadorEvento.buscarEdicion(dt.getNombre()) != null) {
            throw new ReglaNegocioException(
                    "Ya existe una edicion con el nombre \"" + dt.getNombre() + "\".");
        }
        if (eventoSeleccionado == null || organizadorSeleccionado == null) {
            throw new ReglaNegocioException(
                    "Primero hay que seleccionar el evento y el organizador de la edicion.");
        }
        eventoSeleccionado.altaEdicion(dt, organizadorSeleccionado);
        // JPA: confirma el cambio en la base. La edicion nueva viaja por el
        // cascade del @OneToMany de Evento.
        manejadorEvento.actualizar(eventoSeleccionado);
    }

    // ===== Alta de Usuario =====

    @Override
    public void ingresarDatosUsuario(DTUsuario datos, TipoUsuario tipo)
            throws ReglaNegocioException {
        if (find(datos.getNickname()) != null) {
            throw new ReglaNegocioException(
                    "Ya existe un usuario con el nickname \"" + datos.getNickname() + "\".");
        }
        if (findPorCorreo(datos.getCorreo()) != null) {
            throw new ReglaNegocioException(
                    "Ya existe un usuario con el correo \"" + datos.getCorreo() + "\".");
        }
        this.datosUsuarioRecordados = datos;
        this.tipoUsuarioRecordado = tipo;
    }

    @Override
    public void ingresarDatosAsistente(String apellido, DTFecha fechaNac)
            throws ReglaNegocioException {
        exigirDatosDeUsuarioEnCurso();
        Asistente a = new Asistente(datosUsuarioRecordados.getNickname(), datosUsuarioRecordados.getNombre(),
                datosUsuarioRecordados.getCorreo(), apellido, fechaNac.aLocalDate());
        manejadorUsuario.agregar(a);
        this.asistenteRecordado = a;
    }

    @Override
    public void seleccionarInstitucion(String nombreInstitucion)
            throws ReglaNegocioException {
        Institucion i = findInstitucion(nombreInstitucion);
        if (i == null) {
            throw new ReglaNegocioException(
                    "No existe una institucion con el nombre \"" + nombreInstitucion + "\".");
        }
        if (asistenteRecordado == null) {
            throw new ReglaNegocioException(
                    "Primero hay que ingresar los datos del asistente.");
        }
        asistenteRecordado.setInstitucion(i);
        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorUsuario.actualizar(asistenteRecordado);
    }

    @Override
    public void ingresarDatosOrganizador(String descripcion, String sitioWeb)
            throws ReglaNegocioException {
        exigirDatosDeUsuarioEnCurso();
        Organizador o = new Organizador(datosUsuarioRecordados.getNickname(), datosUsuarioRecordados.getNombre(),
                datosUsuarioRecordados.getCorreo(), descripcion, sitioWeb);
        manejadorUsuario.agregar(o);
    }

    @Override
    public Set<String> listarNombresInstituciones() {
        Set<String> resultado = new HashSet<>();
        for (Institucion i : manejadorInstitucion.listar()) {
            resultado.add(i.getNombre());
        }
        return resultado;
    }

    /** Busqueda de Usuario por correo (delega en ManejadorUsuario). */
    private Usuario findPorCorreo(String correo) {
        return manejadorUsuario.buscarPorCorreo(correo);
    }

    /** Busqueda de Institucion por nombre (delega en ManejadorInstitucion). */
    private Institucion findInstitucion(String nombre) {
        return manejadorInstitucion.buscar(nombre);
    }

    // ===== Alta de Tipo de Registro =====

    @Override
    public DTEdicionEvento seleccionarEdicionEvento(String nombreEdicion)
            throws ReglaNegocioException {
        EdicionEvento ed = buscarEdicionDelEvento(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerDT();
    }

    @Override
    public void ingresarDatosTipoRegistro(String nombre, String descripcion, double costo, int cupo)
            throws ReglaNegocioException {
        if (edicionSeleccionada == null) {
            throw new ReglaNegocioException("Primero hay que seleccionar una edicion.");
        }
        if (edicionSeleccionada.buscarTipoRegistro(nombre) != null) {
            throw new ReglaNegocioException("Ya existe un tipo de registro con el nombre \""
                    + nombre + "\" en la edicion " + edicionSeleccionada.getNombre() + ".");
        }
        edicionSeleccionada.crearTipoRegistro(nombre, descripcion, costo, cupo);
        // JPA: se actualiza por el Evento, que es la raiz del agregado.
        manejadorEvento.actualizar(edicionSeleccionada.getEvento());
    }

    // ===== Registro a Edicion de Evento =====

    @Override
    public DTDatosRegistro listarDatosRegistro(String nombreEdicion)
            throws ReglaNegocioException {
        EdicionEvento ed = buscarEdicionDelEvento(nombreEdicion);
        this.edicionSeleccionada = ed;
        Set<DTTipoRegistro> tiposRegistro = ed.obtenerTiposRegistro();
        Set<DTAsistente> asistentes = listarAsistentes();
        return new DTDatosRegistro(tiposRegistro, asistentes);
    }

    @Override
    public void altaRegistro(String nickname, String nombreEdicion, String nombreTipo)
            throws ReglaNegocioException {
        altaRegistro(nickname, nombreEdicion, nombreTipo, null);
    }

    @Override
    public void altaRegistro(String nickname, String nombreEdicion, String nombreTipo,
                             String codigoPatrocinio) throws ReglaNegocioException {
        // La edicion se resuelve por el nombre que llega como parametro, no por
        // la que quedo retenida de un llamado anterior.
        EdicionEvento ed = manejadorEvento.buscarEdicion(nombreEdicion);
        if (ed == null) {
            throw new ReglaNegocioException(
                    "No existe una edicion con el nombre \"" + nombreEdicion + "\".");
        }
        this.edicionSeleccionada = ed;

        TipoRegistro tr = ed.buscarTipoRegistro(nombreTipo);
        if (tr == null) {
            throw new ReglaNegocioException("La edicion " + ed.getNombre()
                    + " no tiene un tipo de registro \"" + nombreTipo + "\".");
        }

        if (ed.estaRegistrado(nickname)) {
            throw new ReglaNegocioException("El asistente " + nickname
                    + " ya esta registrado en la edicion " + ed.getNombre() + ".");
        }
        if (!ed.hayCupo(tr)) {
            throw new ReglaNegocioException("El tipo de registro \"" + nombreTipo
                    + "\" ya alcanzo su cupo de " + tr.getCupo() + " lugares.");
        }

        Usuario u = find(nickname);
        if (!(u instanceof Asistente)) {
            throw new ReglaNegocioException(
                    "No existe un asistente con el nickname \"" + nickname + "\".");
        }

        Patrocinio patrocinio = null;
        if (codigoPatrocinio != null) {
            patrocinio = validarCodigoPatrocinio(ed, tr, (Asistente) u, codigoPatrocinio);
        }

        ed.altaRegistro((Asistente) u, tr, LocalDate.now(), patrocinio);
        manejadorEvento.actualizar(ed.getEvento());
    }

    /**
     * Reglas del registro gratuito por patrocinio (ver letra, seccion 4): el
     * codigo tiene que ser de un patrocinio de ESA edicion, el asistente tiene
     * que pertenecer a la institucion que lo otorga, el tipo de registro tiene
     * que ser el que el patrocinio regala, y tienen que quedar lugares gratuitos.
     */
    private Patrocinio validarCodigoPatrocinio(EdicionEvento ed, TipoRegistro tr,
                                               Asistente asistente, String codigo)
            throws ReglaNegocioException {
        Patrocinio p = ed.buscarPatrocinio(codigo);
        if (p == null) {
            throw new ReglaNegocioException("La edicion " + ed.getNombre()
                    + " no tiene un patrocinio con el codigo " + codigo + ".");
        }

        Institucion institucion = asistente.getInstitucion();
        if (institucion == null
                || !institucion.getNombre().equals(p.getInstitucion().getNombre())) {
            throw new ReglaNegocioException("El asistente " + asistente.getNickname()
                    + " no pertenece a " + p.getInstitucion().getNombre()
                    + ", que es la institucion del codigo " + codigo + ".");
        }

        if (p.getTipoRegistro() == null
                || !p.getTipoRegistro().getNombre().equals(tr.getNombre())) {
            String tipoDelPatrocinio = (p.getTipoRegistro() == null)
                    ? "ninguno" : p.getTipoRegistro().getNombre();
            throw new ReglaNegocioException("El codigo " + codigo
                    + " da registros gratuitos del tipo \"" + tipoDelPatrocinio
                    + "\", no del tipo \"" + tr.getNombre() + "\".");
        }

        int usados = ed.registrosGratisUsados(p);
        if (usados >= p.getCantRegistrosGratis()) {
            throw new ReglaNegocioException("El patrocinio " + codigo
                    + " ya uso sus " + p.getCantRegistrosGratis()
                    + " registros gratuitos.");
        }

        return p;
    }

    private Set<DTAsistente> listarAsistentes() {
        Set<DTAsistente> resultado = new HashSet<>();
        for (Usuario u : manejadorUsuario.listar()) {
            if (u.obtenerTipoUsuario() == TipoUsuario.ASISTENTE) {
                resultado.add((DTAsistente) u.obtenerDT());
            }
        }
        return resultado;
    }



    // ===== Consulta de Usuario =====
    public Set<DTEdicionEvento> listarEdiciones() throws ReglaNegocioException {
        exigirOrganizadorSeleccionado();
        Set<DTEdicionEvento> resultado = new HashSet<>();
        for (EdicionEvento ed : this.organizadorSeleccionado.getEdiciones()) {
            resultado.add(ed.obtenerDT());
        }
        return resultado;
    }

    public DTEdicionCompleto seleccionarEdicion(String nombreEdicion)
            throws ReglaNegocioException {
        exigirOrganizadorSeleccionado();
        EdicionEvento ed = organizadorSeleccionado.buscarEdicion(nombreEdicion);
        if (ed == null) {
            throw new ReglaNegocioException("El organizador "
                    + organizadorSeleccionado.getNickname()
                    + " no organiza la edicion \"" + nombreEdicion + "\".");
        }
        return ed.obtenerDTCompleto();
    }

    public Set<DTRegistro> listarRegistroUsuario(String nickname)
            throws ReglaNegocioException {
        Set<DTRegistro> resultado = new HashSet<>();
        Asistente asistente = exigirAsistente(nickname);
        for (Registro reg : asistente.getRegistros()) {
            resultado.add(reg.obtenerDT());
        }
        return resultado;
    }

    public DTRegistro obtenerRegistro(String nombreEdicion)
            throws ReglaNegocioException {
        if (asistenteSeleccionado == null) {
            throw new ReglaNegocioException("Primero hay que seleccionar un asistente.");
        }
        DTRegistro registro = asistenteSeleccionado.darRegistro(nombreEdicion);
        if (registro == null) {
            throw new ReglaNegocioException("El asistente "
                    + asistenteSeleccionado.getNickname()
                    + " no tiene un registro en la edicion \"" + nombreEdicion + "\".");
        }
        return registro;
    }

    // ===== Consulta de Registro =====

    @Override
    public DTRegistro obtenerRegistro(String nickname, String nombre)
            throws ReglaNegocioException {
        // 1: u := find(nickname)  /  2: darRegistro(nombre) : DTRegistro
        Asistente asistente = exigirAsistente(nickname);
        DTRegistro registro = asistente.darRegistro(nombre);
        if (registro == null) {
            throw new ReglaNegocioException("El asistente " + nickname
                    + " no tiene un registro en la edicion \"" + nombre + "\".");
        }
        return registro;
    }

    // ===== Alta de Categoria =====

    @Override
    public Set<DTCategoria> listarCategorias() {
        // Lista PLANA de todas las categorias (por nombre), para selectores como
        // Alta de Evento. Cada DT va sin hijas: aqui no interesa la jerarquia.
        Set<DTCategoria> resultado = new HashSet<>();
        for (Categoria cat : manejadorCategoria.listar()) {
            resultado.add(new DTCategoria(cat.getNombre()));
        }
        return resultado;
    }

    @Override
    public Set<DTCategoria> listarCategoriasArbol() {
        // Solo las raices; cada una arma su DT de forma recursiva (con sus hijas
        // anidadas), reflejando la jerarquia completa.
        Set<DTCategoria> raices = new HashSet<>();
        for (Categoria cat : manejadorCategoria.listar()) {
            if (cat.esRaiz()) {
                raices.add(cat.obtenerDT());
            }
        }
        return raices;
    }

    @Override
    public void altaCategoria(String nombre, String nombrePadre) throws ReglaNegocioException {
        if (findCategoria(nombre) != null) {
            throw new ReglaNegocioException(
                    "Ya existe una categoria con el nombre \"" + nombre + "\".");
        }
        Categoria nueva = new Categoria(nombre);
        if (nombrePadre != null && !nombrePadre.isBlank()) {
            Categoria padre = findCategoria(nombrePadre);
            if (padre == null) {
                throw new ReglaNegocioException(
                        "No existe una categoria con el nombre \"" + nombrePadre + "\".");
            }
            padre.agregarHija(nueva);
        }
        manejadorCategoria.agregar(nueva);
    }

    /** Busqueda de Categoria por nombre (delega en ManejadorCategoria). */
    private Categoria findCategoria(String nombre) {
        return manejadorCategoria.buscar(nombre);
    }

    // ===== Consulta de Tipo de Registro =====

    @Override
    public Set<DTTipoRegistro> listarTiposRegistroDeEdicion(String nombreEdicion)
            throws ReglaNegocioException {
        EdicionEvento edicion = buscarEdicionDelEvento(nombreEdicion);
        this.edicionSeleccionada = edicion;
        return edicion.obtenerTiposRegistro();
    }

    @Override
    public DTTipoRegistro seleccionarTipoRegistro(String nombreTipoRegistro)
            throws ReglaNegocioException {
        if (edicionSeleccionada == null) {
            throw new ReglaNegocioException("Primero hay que seleccionar una edicion.");
        }
        TipoRegistro tipo = edicionSeleccionada.buscarTipoRegistro(nombreTipoRegistro);
        if (tipo == null) {
            throw new ReglaNegocioException("La edicion " + edicionSeleccionada.getNombre()
                    + " no tiene un tipo de registro \"" + nombreTipoRegistro + "\".");
        }
        return tipo.obtenerDT();
    }

    // ===== Alta institucion =====
    @Override
    public void altaInstitucion(String nombre, String descripcion, String sitioWeb)
            throws ReglaNegocioException {
        if (findInstitucion(nombre) != null) {
            throw new ReglaNegocioException(
                    "Ya existe una institucion con el nombre \"" + nombre + "\".");
        }
        Institucion institucion = new Institucion(nombre, descripcion, sitioWeb);
        manejadorInstitucion.agregar(institucion);
    }

    // ===== Consulta de Edicion de Evento =====

    @Override
    public DTEdicionCompleto seleccionarEdicionCompleta(String nombreEdicion)
            throws ReglaNegocioException {
        EdicionEvento ed = buscarEdicionDelEvento(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerDTCompleto();
    }

    // ===== Alta de Patrocinio =====

    @Override
    public void altaPatrocinio(DTPatrocinio dt) throws ReglaNegocioException {
        if (edicionSeleccionada == null) {
            throw new ReglaNegocioException("Primero hay que seleccionar una edicion.");
        }
        Institucion institucion = findInstitucion(dt.getInstitucion());
        if (institucion == null) {
            throw new ReglaNegocioException("No existe una institucion con el nombre \""
                    + dt.getInstitucion() + "\".");
        }
        TipoRegistro tipo = edicionSeleccionada.buscarTipoRegistro(dt.getTipoRegistro());
        if (tipo == null) {
            throw new ReglaNegocioException("La edicion " + edicionSeleccionada.getNombre()
                    + " no tiene un tipo de registro \"" + dt.getTipoRegistro() + "\".");
        }

        // Regla 1: una institucion no puede patrocinar dos veces la misma edicion.
        if (edicionSeleccionada.tienePatrocinioDe(dt.getInstitucion())) {
            throw new ReglaNegocioException("La institucion " + dt.getInstitucion()
                    + " ya tiene un patrocinio para la edicion "
                    + edicionSeleccionada.getNombre() + ".");
        }

        // Regla 2: el valor de los registros gratuitos no puede superar
        // el 20% del aporte economico.
        double valorGratis = tipo.getCosto() * dt.getCantRegistrosGratis();
        double tope = dt.getMonto() * 0.20;
        if (valorGratis > tope) {
            throw new ReglaNegocioException(String.format(
                    "El valor de los registros gratuitos ($%.2f = %d x $%.2f) supera "
                            + "el 20%% del aporte ($%.2f). Maximo permitido: $%.2f.",
                    valorGratis, dt.getCantRegistrosGratis(), tipo.getCosto(),
                    dt.getMonto(), tope));
        }

        Patrocinio p = new Patrocinio(dt.getFecha().aLocalDate(), dt.getMonto(),
                dt.getCantRegistrosGratis(), dt.getCodigoPatrocinio(),
                NivelPatrocinio.valueOf(dt.getNivel()),
                institucion, tipo);
        edicionSeleccionada.agregarPatrocinio(p);
        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorEvento.actualizar(edicionSeleccionada.getEvento());
    }
    // ===== Alta Evento =====
    @Override
    public void ingresarDatosEvento(String nombre, String descripcion, LocalDate fechaAlta,
                                    String sigla, List<String> nombresCategorias)
            throws ReglaNegocioException {

        if (findEvento(nombre) != null) {
            throw new ReglaNegocioException(
                    "Ya existe un evento con el nombre \"" + nombre + "\".");
        }

        // La letra pide al menos una categoria por evento.
        if (nombresCategorias == null || nombresCategorias.isEmpty()) {
            throw new ReglaNegocioException(
                    "Hay que seleccionar al menos una categoria para el evento.");
        }

        List<Categoria> categoriasEvento = new ArrayList<>();
        for (String nombreCat : nombresCategorias) {
            Categoria cat = findCategoria(nombreCat);
            if (cat == null) {
                throw new ReglaNegocioException(
                        "La categoria \"" + nombreCat + "\" no existe en la plataforma.");
            }
            categoriasEvento.add(cat);
        }

        Evento evento = new Evento(nombre, descripcion, fechaAlta, sigla, categoriasEvento);
        manejadorEvento.agregar(evento);
    }

    // ===== Chequeos que se repiten en varios casos de uso =====

    /** Busca una edicion dentro del evento seleccionado, o falla con un mensaje claro. */
    private EdicionEvento buscarEdicionDelEvento(String nombreEdicion)
            throws ReglaNegocioException {
        if (eventoSeleccionado == null) {
            throw new ReglaNegocioException("Primero hay que seleccionar un evento.");
        }
        EdicionEvento ed = eventoSeleccionado.buscarEdicion(nombreEdicion);
        if (ed == null) {
            throw new ReglaNegocioException("El evento " + eventoSeleccionado.getNombre()
                    + " no tiene una edicion \"" + nombreEdicion + "\".");
        }
        return ed;
    }

    /** Devuelve el Asistente con ese nickname, o falla si no existe o no es asistente. */
    private Asistente exigirAsistente(String nickname) throws ReglaNegocioException {
        Usuario u = find(nickname);
        if (!(u instanceof Asistente)) {
            throw new ReglaNegocioException(
                    "No existe un asistente con el nickname \"" + nickname + "\".");
        }
        return (Asistente) u;
    }

    private void exigirOrganizadorSeleccionado() throws ReglaNegocioException {
        if (organizadorSeleccionado == null) {
            throw new ReglaNegocioException("Primero hay que seleccionar un organizador.");
        }
    }

    private void exigirDatosDeUsuarioEnCurso() throws ReglaNegocioException {
        if (datosUsuarioRecordados == null) {
            throw new ReglaNegocioException(
                    "Primero hay que ingresar los datos comunes del usuario.");
        }
    }

    // =================================================================
    //  OPERACIONES PARA EL SITIO WEB
    // =================================================================
    //  Todavia sin implementar: estan para que el modulo web y la Estacion
    //  de Trabajo puedan compilar contra el contrato desde el primer dia.
    //  Cada una se implementa junto con su caso de uso.

    // ----- Sesion y usuarios (CU 1 a 5) -----

    @Override
    public DTUsuario iniciarSesion(String identificador, String password)
            throws ReglaNegocioException {

        if (identificador == null || identificador.isBlank()
                || password == null || password.isEmpty()) {
            throw new ReglaNegocioException("Ingresa tu usuario y tu contraseña.");
        }

        // La letra acepta el nickname O el correo. Se prueba primero por
        // nickname y solo si no aparece se busca por correo, para no hacer dos
        // consultas a la base cuando con una alcanza.
        String buscado = identificador.trim();
        Usuario usuario = manejadorUsuario.buscar(buscado);
        if (usuario == null) {
            usuario = manejadorUsuario.buscarPorCorreo(buscado);
        }

        if (usuario == null || !usuario.verificarPassword(password)) {
            throw new ReglaNegocioException("El usuario o la contraseña no son correctos.");
        }

        // El DT que vuelve es un DTAsistente o un DTOrganizador, y cada uno se
        // declara su propio tipo en el constructor. Por eso el cabezal puede
        // preguntar esOrganizador() sin que nadie se haya acordado de setearlo.
        return usuario.obtenerDT();
    }

    @Override
    public void altaUsuario(DTUsuario datos, TipoUsuario tipo, String password,
                            String confirmacion, String nombreInstitucion, DTImagen imagen)
            throws ReglaNegocioException {

        if (datos == null || tipo == null) {
            throw new ReglaNegocioException("Faltan los datos del usuario.");
        }

        // 1. Lo obligatorio. El formulario web ya lo pide con "required", pero
        //    eso vive en el navegador y se puede saltear: la regla tiene que
        //    estar aca, que es por donde pasa todo el mundo si o si.
        String nickname = limpiar(datos.getNickname());
        String nombre = limpiar(datos.getNombre());
        String correo = limpiar(datos.getCorreo());
        if (nickname.isEmpty() || nombre.isEmpty() || correo.isEmpty()) {
            throw new ReglaNegocioException("El nickname, el nombre y el correo son obligatorios.");
        }

        // 2. La contrasena y su confirmacion.
        if (password == null || password.isEmpty()) {
            throw new ReglaNegocioException("Hay que ingresar una contraseña.");
        }
        if (!password.equals(confirmacion)) {
            throw new ReglaNegocioException("Las dos contraseñas no coinciden.");
        }

        // 3. Nickname y correo son unicos en todo el sistema. Aca el mensaje SI
        //    dice cual de los dos esta repetido, al reves del inicio de sesion:
        //    quien se esta registrando necesita saber que corregir, y ademas no
        //    revela nada que no revele el propio formulario.
        if (manejadorUsuario.buscar(nickname) != null) {
            throw new ReglaNegocioException(
                    "Ya existe un usuario con el nickname \"" + nickname + "\".");
        }
        if (manejadorUsuario.buscarPorCorreo(correo) != null) {
            throw new ReglaNegocioException(
                    "Ya existe un usuario con el correo \"" + correo + "\".");
        }

        // 4. Cada tipo trae sus propios datos, asi que se arma la entidad que
        //    corresponde. El DT que llega tiene que ser el del tipo pedido.
        Usuario usuario;
        if (tipo == TipoUsuario.ASISTENTE) {
            if (!(datos instanceof DTAsistente asistente)) {
                throw new ReglaNegocioException("Faltan los datos del asistente.");
            }
            if (limpiar(asistente.getApellido()).isEmpty()
                    || asistente.getFechaNacimiento() == null) {
                throw new ReglaNegocioException(
                        "El apellido y la fecha de nacimiento son obligatorios.");
            }
            Asistente nuevo = new Asistente(nickname, nombre, correo,
                    asistente.getApellido().trim(),
                    asistente.getFechaNacimiento().aLocalDate(),
                    password);

            // La institucion es opcional, pero si la nombran tiene que existir.
            if (nombreInstitucion != null && !nombreInstitucion.isBlank()) {
                Institucion institucion = manejadorInstitucion.buscar(nombreInstitucion.trim());
                if (institucion == null) {
                    throw new ReglaNegocioException(
                            "No existe una institucion con el nombre \"" + nombreInstitucion + "\".");
                }
                nuevo.setInstitucion(institucion);
            }
            usuario = nuevo;

        } else {
            if (!(datos instanceof DTOrganizador organizador)) {
                throw new ReglaNegocioException("Faltan los datos del organizador.");
            }
            if (limpiar(organizador.getDescripcion()).isEmpty()) {
                throw new ReglaNegocioException("La descripcion es obligatoria.");
            }
            usuario = new Organizador(nickname, nombre, correo,
                    organizador.getDescripcion().trim(),
                    organizador.getSitioWeb(),
                    password);
        }

        // 5. La imagen es opcional y se asigna antes de guardar, asi el alta
        //    entera es una sola escritura en la base.
        if (imagen != null && imagen.getContenido() != null && imagen.getContenido().length > 0) {
            usuario.asignarImagen(new Imagen(imagen.getContenido(), imagen.getTipo()));
        }

        manejadorUsuario.agregar(usuario);
    }

    /** null y los espacios de los costados molestan en todas las validaciones. */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    @Override
    public DTUsuario consultarUsuario(String nickname, String solicitante)
            throws ReglaNegocioException {
        // El perfil en si es publico: nickname, nombre, correo, tipo y si
        // tiene imagen. La contrasena no esta en el DT, asi que no hay nada
        // que esconder aca. Lo que depende de quien pregunta son los dos
        // listados de abajo, no estos datos.
        return exigirUsuario(nickname).obtenerDT();
    }

    @Override
    public Set<DTEdicionEvento> listarEdicionesDeOrganizador(String nickname, String solicitante)
            throws ReglaNegocioException {

        Usuario usuario = exigirUsuario(nickname);
        if (!(usuario instanceof Organizador organizador)) {
            throw new ReglaNegocioException(
                    "El usuario \"" + usuario.getNickname() + "\" no es un organizador.");
        }
        return organizador.obtenerEdicionesPara(solicitante);
    }

    @Override
    public Set<DTRegistro> listarRegistrosDeAsistente(String nickname, String solicitante)
            throws ReglaNegocioException {

        Usuario usuario = exigirUsuario(nickname);
        if (!(usuario instanceof Asistente asistente)) {
            throw new ReglaNegocioException(
                    "El usuario \"" + usuario.getNickname() + "\" no es un asistente.");
        }
        return asistente.obtenerRegistrosPara(solicitante);
    }

    /** El usuario que se pide, o un error si no existe. */
    private Usuario exigirUsuario(String nickname) throws ReglaNegocioException {
        Usuario usuario = (nickname == null || nickname.isBlank())
                ? null
                : manejadorUsuario.buscar(nickname.trim());
        if (usuario == null) {
            throw new ReglaNegocioException(
                    "No existe un usuario con el nickname \"" + nickname + "\".");
        }
        return usuario;
    }


    @Override
    public void modificarUsuario(String solicitante, DTUsuario datos, String nuevaPassword,
                                 String confirmacion, DTImagen imagen)
            throws ReglaNegocioException {

        if (solicitante == null || solicitante.isBlank()) {
            throw new ReglaNegocioException("Hay que iniciar sesion para modificar los datos.");
        }
        if (datos == null) {
            throw new ReglaNegocioException("Faltan los datos del usuario.");
        }

        // Se modifica SIEMPRE el usuario que inicio sesion, nunca el que venga
        // nombrado en el formulario. Si alguien manda el nickname de otro, el
        // pedido se rechaza en vez de ignorarse en silencio.
        Usuario usuario = exigirUsuario(solicitante);
        String pedido = datos.getNickname();
        if (pedido != null && !pedido.isBlank() && !usuario.esSuPropioPerfil(pedido)) {
            throw new ReglaNegocioException("Solo se pueden modificar los datos propios.");
        }

        if (limpiar(datos.getNombre()).isEmpty()) {
            throw new ReglaNegocioException("El nombre es obligatorio.");
        }

        // La contrasena es opcional: los dos campos vacios significan "dejala
        // como esta". Si se escribe una, tiene que coincidir con su
        // confirmacion, aunque el pedido no haya pasado por el navegador.
        boolean cambiaPassword = nuevaPassword != null && !nuevaPassword.isEmpty();
        if (cambiaPassword && !nuevaPassword.equals(confirmacion)) {
            throw new ReglaNegocioException("Las dos contraseñas no coinciden.");
        }

        // Lo propio de cada tipo, antes de tocar nada: si algo falla, el
        // usuario tiene que quedar como estaba.
        Institucion institucion = null;
        boolean cambiaInstitucion = false;

        if (usuario instanceof Asistente) {
            if (!(datos instanceof DTAsistente asistente)) {
                throw new ReglaNegocioException("Faltan los datos del asistente.");
            }
            if (limpiar(asistente.getApellido()).isEmpty()
                    || asistente.getFechaNacimiento() == null) {
                throw new ReglaNegocioException(
                        "El apellido y la fecha de nacimiento son obligatorios.");
            }
            // Sin institucion es una opcion valida, asi que se distingue entre
            // "no la nombraron" y "la sacaron": las dos llegan como vacio y
            // las dos significan lo mismo, dejar al asistente sin institucion.
            cambiaInstitucion = true;
            String nombreInstitucion = limpiar(asistente.getInstitucion());
            if (!nombreInstitucion.isEmpty()) {
                institucion = manejadorInstitucion.buscar(nombreInstitucion);
                if (institucion == null) {
                    throw new ReglaNegocioException(
                            "No existe una institucion con el nombre \"" + nombreInstitucion + "\".");
                }
            }
        } else {
            if (!(datos instanceof DTOrganizador organizador)) {
                throw new ReglaNegocioException("Faltan los datos del organizador.");
            }
            if (limpiar(organizador.getDescripcion()).isEmpty()) {
                throw new ReglaNegocioException("La descripcion es obligatoria.");
            }
        }

        // El nickname y el correo no se tocan: modificarDatos() solo cambia el
        // nombre y lo propio de cada tipo, asi que si el formulario los manda
        // igual no pasa nada.
        usuario.modificarDatos(datos);

        if (cambiaInstitucion) {
            ((Asistente) usuario).setInstitucion(institucion);
        }
        if (cambiaPassword) {
            usuario.cambiarPassword(nuevaPassword);
        }
        // La imagen solo se reemplaza cuando eligieron una nueva. Un
        // formulario enviado sin elegir archivo deja la que ya estaba.
        if (imagen != null && imagen.getContenido() != null && imagen.getContenido().length > 0) {
            usuario.asignarImagen(new Imagen(imagen.getContenido(), imagen.getTipo()));
        }

        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorUsuario.actualizar(usuario);
    }

    @Override
    public DTImagen obtenerImagenUsuario(String nickname) throws ReglaNegocioException {
        Usuario usuario = find(limpiar(nickname));
        if (usuario == null) {
            throw new ReglaNegocioException("No existe un usuario con el nickname \""
                    + nickname + "\".");
        }
        return obtenerDTImagen(usuario.getImagen());
    }

    @Override
    public void asignarImagenUsuario(String nickname, DTImagen imagen)
            throws ReglaNegocioException {
        Usuario usuario = find(limpiar(nickname));
        if (usuario == null) {
            throw new ReglaNegocioException("No existe un usuario con el nickname \""
                    + nickname + "\".");
        }
        usuario.asignarImagen(crearImagen(imagen));
        manejadorUsuario.actualizar(usuario);
    }

    // ----- Eventos y ediciones (CU 6 a 9) -----

    @Override
    public Set<DTEvento> listarEventos(String nombreCategoria) {
        // Sin categoria es la cartelera completa: el servlet llama siempre a esta.
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            return listarEventos();
        }
        // Una categoria que no existe no es un error: simplemente no tiene eventos.
        Set<DTEvento> resultado = new HashSet<>();
        for (Evento e : manejadorEvento.listar()) {
            if (e.tieneCategoria(nombreCategoria)) {
                resultado.add(e.obtenerDT());
            }
        }
        return resultado;
    }

    @Override
    public DTEvento consultarEvento(String nombre) throws ReglaNegocioException {
        return buscarEventoExistente(nombre).obtenerDT();
    }

    @Override
    public Set<DTEdicionEvento> listarEdicionesAceptadas(String nombreEvento)
            throws ReglaNegocioException {
        return buscarEventoExistente(nombreEvento).obtenerEdicionesAceptadas();
    }

    /**
     * Busca un evento por nombre y avisa si no esta. A diferencia de
     * seleccionarEvento(), NO lo recuerda: las operaciones de la web no
     * dependen de una llamada anterior.
     */
    private Evento buscarEventoExistente(String nombre) throws ReglaNegocioException {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("Falta el nombre del evento.");
        }
        Evento e = findEvento(nombre);
        if (e == null) {
            throw new ReglaNegocioException(
                    "No existe un evento con el nombre \"" + nombre + "\".");
        }
        return e;
    }

    @Override
    public void altaEvento(String solicitante, String nombre, String descripcion, String sigla,
                           List<String> nombresCategorias, DTImagen imagen)
            throws ReglaNegocioException {
        throw new UnsupportedOperationException("Alta de Evento: todavia no implementado.");
    }

    @Override
    public void altaEdicion(String solicitante, String nombreEvento, DTEdicionEvento datos,
                            DTImagen imagen)
            throws ReglaNegocioException {
        // Solo un organizador con sesion: y el organizador de la edicion es
        // siempre quien la da de alta, no se elige.
        Usuario usuario = find(limpiar(solicitante));
        if (!(usuario instanceof Organizador organizador)) {
            throw new ReglaNegocioException("Solo un organizador puede dar de alta ediciones.");
        }
        Evento evento = buscarEventoExistente(nombreEvento);
        if (datos == null) {
            throw new ReglaNegocioException("Faltan los datos de la edicion.");
        }

        String nombre = limpiar(datos.getNombre());
        LocalDate inicio = datos.getFechaInicio() == null ? null : datos.getFechaInicio().aLocalDate();
        LocalDate fin = datos.getFechaFin() == null ? null : datos.getFechaFin().aLocalDate();
        EdicionEvento.validarDatos(nombre, datos.getSigla(), datos.getCiudad(), datos.getPais(),
                inicio, fin);

        // Una edicion no puede repetir nombre en NINGUN evento (regla del Lab 1).
        if (manejadorEvento.buscarEdicion(nombre) != null) {
            throw new ReglaNegocioException(
                    "Ya existe una edicion con el nombre \"" + nombre + "\".");
        }

        // La fecha de alta la pone el sistema: se ignora la del DT si viniera.
        // El estado no se pasa: toda edicion nace INGRESADA.
        DTEdicionEvento limpio = new DTEdicionEvento(nombre, limpiar(datos.getSigla()),
                limpiar(datos.getCiudad()), limpiar(datos.getPais()),
                DTFecha.desde(inicio), DTFecha.desde(fin), DTFecha.desde(LocalDate.now()));
        EdicionEvento nueva = evento.altaEdicion(limpio, organizador);

        // La imagen es opcional: si no viene, la edicion queda sin imagen.
        if (imagen != null && imagen.getContenido() != null && imagen.getContenido().length > 0) {
            nueva.asignarImagen(crearImagen(imagen));
        }
        // JPA: la edicion nueva viaja por el cascade del @OneToMany de Evento.
        manejadorEvento.actualizar(evento);
    }

    @Override
    public DTEdicionCompleto consultarEdicion(String nombreEdicion, String solicitante)
            throws ReglaNegocioException {
        EdicionEvento edicion = buscarEdicionExistente(nombreEdicion);
        if (!edicion.puedeVerla(solicitante)) {
            throw new ReglaNegocioException("La edicion \"" + edicion.getNombre()
                    + "\" todavia no esta publicada. Solo puede verla su organizador.");
        }
        return edicion.obtenerDTCompletoPara(solicitante);
    }

    /**
     * Busca una edicion por nombre en TODOS los eventos, sin depender de un
     * evento "seleccionado" antes: las operaciones de la web no recuerdan nada
     * entre llamadas. Los nombres de edicion son unicos en la plataforma.
     */
    private EdicionEvento buscarEdicionExistente(String nombreEdicion)
            throws ReglaNegocioException {
        if (nombreEdicion == null || nombreEdicion.isBlank()) {
            throw new ReglaNegocioException("Falta el nombre de la edicion.");
        }
        EdicionEvento edicion = manejadorEvento.buscarEdicion(nombreEdicion.trim());
        if (edicion == null) {
            throw new ReglaNegocioException(
                    "No existe una edicion con el nombre \"" + nombreEdicion + "\".");
        }
        return edicion;
    }

    @Override
    public DTImagen obtenerImagenEvento(String nombreEvento) throws ReglaNegocioException {
        return obtenerDTImagen(buscarEventoExistente(nombreEvento).getImagen());
    }

    @Override
    public DTImagen obtenerImagenEdicion(String nombreEdicion) throws ReglaNegocioException {
        // Sin estado: buscarEdicionDelEvento exigia un evento seleccionado antes
        // y desde la web fallaba siempre.
        return obtenerDTImagen(buscarEdicionExistente(nombreEdicion).getImagen());
    }

    @Override
    public void asignarImagenEvento(String nombreEvento, DTImagen imagen)
            throws ReglaNegocioException {
        Evento evento = buscarEventoExistente(nombreEvento);
        evento.asignarImagen(crearImagen(imagen));
        manejadorEvento.actualizar(evento);
    }

    @Override
    public void asignarImagenEdicion(String nombreEdicion, DTImagen imagen)
            throws ReglaNegocioException {
        EdicionEvento edicion = buscarEdicionExistente(nombreEdicion);
        edicion.asignarImagen(crearImagen(imagen));
        manejadorEvento.actualizar(edicion.getEvento());
    }

    // ----- Tipos de registro e instituciones (CU 10, 11 y 14) -----

    @Override
    public void altaTipoRegistro(String solicitante, String nombreEdicion, String nombre,
                                 String descripcion, double costo, int cupo)
            throws ReglaNegocioException {
        Usuario usuario = find(limpiar(solicitante));
        if (!(usuario instanceof Organizador)) {
            throw new ReglaNegocioException(
                    "Solo un organizador puede dar de alta tipos de registro.");
        }

        EdicionEvento edicion = manejadorEvento.buscarEdicion(limpiar(nombreEdicion));
        if (edicion == null) {
            throw new ReglaNegocioException(
                    "No existe una edicion con el nombre \"" + nombreEdicion + "\".");
        }
        if (!edicion.getOrganizador().getNickname().equals(usuario.getNickname())) {
            throw new ReglaNegocioException(
                    "El organizador no es responsable de la edicion \""
                            + edicion.getNombre() + "\".");
        }

        String nombreLimpio = limpiar(nombre);
        if (nombreLimpio.isEmpty()) {
            throw new ReglaNegocioException("El nombre del tipo de registro es obligatorio.");
        }
        if (edicion.buscarTipoRegistro(nombreLimpio) != null) {
            throw new ReglaNegocioException("Ya existe un tipo de registro con el nombre \""
                    + nombreLimpio + "\" en la edicion " + edicion.getNombre() + ".");
        }
        if (costo < 0) {
            throw new ReglaNegocioException("El costo no puede ser negativo.");
        }
        if (cupo <= 0) {
            throw new ReglaNegocioException("El cupo debe ser mayor que cero.");
        }

        edicion.crearTipoRegistro(nombreLimpio, limpiar(descripcion), costo, cupo);
        // El tipo pertenece a la edicion y esta al evento: al actualizar la
        // raiz del agregado se persiste por cascade, como en el flujo Swing.
        manejadorEvento.actualizar(edicion.getEvento());
    }

    @Override
    public DTTipoRegistro consultarTipoRegistro(String nombreEdicion, String nombreTipo)
            throws ReglaNegocioException {
        EdicionEvento edicion = manejadorEvento.buscarEdicion(limpiar(nombreEdicion));
        if (edicion == null) {
            throw new ReglaNegocioException(
                    "No existe una edicion con el nombre \"" + nombreEdicion + "\".");
        }
        if (!edicion.estaAceptada()) {
            throw new ReglaNegocioException(
                    "La edicion \"" + edicion.getNombre()
                            + "\" no esta disponible para consulta publica.");
        }

        TipoRegistro tipo = edicion.buscarTipoRegistro(limpiar(nombreTipo));
        if (tipo == null) {
            throw new ReglaNegocioException("La edicion " + edicion.getNombre()
                    + " no tiene un tipo de registro con el nombre \""
                    + nombreTipo + "\".");
        }
        return tipo.obtenerDT();
    }

    @Override
    public void altaInstitucion(String solicitante, String nombre, String descripcion,
                                String sitioWeb, DTImagen imagen)
            throws ReglaNegocioException {
        Usuario usuario = find(limpiar(solicitante));
        if (!(usuario instanceof Organizador)) {
            throw new ReglaNegocioException(
                    "Solo un organizador puede dar de alta instituciones.");
        }

        String nombreLimpio = limpiar(nombre);
        String descripcionLimpia = limpiar(descripcion);
        String sitioWebLimpio = limpiar(sitioWeb);
        if (nombreLimpio.isEmpty() || descripcionLimpia.isEmpty() || sitioWebLimpio.isEmpty()) {
            throw new ReglaNegocioException(
                    "El nombre, la descripcion y el sitio web son obligatorios.");
        }
        if (findInstitucion(nombreLimpio) != null) {
            throw new ReglaNegocioException(
                    "Ya existe una institucion con el nombre \"" + nombreLimpio + "\".");
        }

        Institucion institucion = new Institucion(nombreLimpio, descripcionLimpia, sitioWebLimpio);
        if (imagen != null && imagen.getContenido() != null && imagen.getContenido().length > 0) {
            institucion.asignarImagen(new Imagen(imagen.getContenido(), imagen.getTipo()));
        }
        manejadorInstitucion.agregar(institucion);
    }

    @Override
    public Set<DTInstitucion> listarInstituciones() {
        Set<DTInstitucion> resultado = new HashSet<>();
        for (Institucion institucion : manejadorInstitucion.listar()) {
            resultado.add(institucion.obtenerDT());
        }
        return resultado;
    }

    @Override
    public DTInstitucion consultarInstitucion(String nombre) throws ReglaNegocioException {
        Institucion institucion = findInstitucion(limpiar(nombre));
        if (institucion == null) {
            throw new ReglaNegocioException(
                    "No existe una institucion con el nombre \"" + nombre + "\".");
        }
        return institucion.obtenerDT();
    }

    @Override
    public DTImagen obtenerImagenInstitucion(String nombre) throws ReglaNegocioException {
        Institucion institucion = findInstitucion(limpiar(nombre));
        if (institucion == null) {
            throw new ReglaNegocioException(
                    "No existe una institucion con el nombre \"" + nombre + "\".");
        }
        return obtenerDTImagen(institucion.getImagen());
    }

    @Override
    public void asignarImagenInstitucion(String nombre, DTImagen imagen)
            throws ReglaNegocioException {
        Institucion institucion = findInstitucion(limpiar(nombre));
        if (institucion == null) {
            throw new ReglaNegocioException(
                    "No existe una institucion con el nombre \"" + nombre + "\".");
        }
        institucion.asignarImagen(crearImagen(imagen));
        manejadorInstitucion.actualizar(institucion);
    }

    private static DTImagen obtenerDTImagen(Imagen imagen) {
        return imagen == null ? null
                : new DTImagen(imagen.getContenido(), imagen.getTipo());
    }

    private static Imagen crearImagen(DTImagen imagen) throws ReglaNegocioException {
        if (imagen == null || imagen.getContenido() == null || imagen.getContenido().length == 0
                || imagen.getTipo() == null || imagen.getTipo().isBlank()) {
            throw new ReglaNegocioException("La imagen es obligatoria y debe tener contenido.");
        }
        return new Imagen(imagen.getContenido(), imagen.getTipo());
    }

    // ----- Consulta de Registro (CU 13) -----

    @Override
    public Set<DTRegistro> listarRegistrosDeEdicion(String nombreEdicion, String solicitante)
            throws ReglaNegocioException {
        throw new UnsupportedOperationException("Registros de una edicion: todavia no implementado.");
    }

    @Override
    public DTRegistro consultarRegistro(String nombreEdicion, String nickname, String solicitante)
            throws ReglaNegocioException {
        throw new UnsupportedOperationException("Consulta de Registro: todavia no implementado.");
    }

    // ----- Patrocinios (CU 15 y 16) -----

    @Override
    public void altaPatrocinio(String solicitante, String nombreEdicion, DTPatrocinio datos)
            throws ReglaNegocioException {

        if (solicitante == null || solicitante.isBlank()) {
            throw new ReglaNegocioException("Hay que iniciar sesion para dar de alta un patrocinio.");
        }
        if (datos == null || nombreEdicion == null || nombreEdicion.isBlank()) {
            throw new ReglaNegocioException("Faltan los datos del patrocinio.");
        }

        EdicionEvento edicion = manejadorEvento.buscarEdicion(nombreEdicion.trim());
        if (edicion == null) {
            throw new ReglaNegocioException(
                    "No existe una edicion con el nombre \"" + nombreEdicion + "\".");
        }

        // La letra: el organizador ve "las ediciones de ese evento que EL
        // organiza". En cualquier estado, eso si: se puede patrocinar una
        // edicion todavia Ingresada.
        if (!edicion.loOrganiza(solicitante)) {
            throw new ReglaNegocioException(
                    "Solo el organizador de la edicion " + edicion.getNombre()
                            + " puede darle un patrocinio.");
        }

        NivelPatrocinio nivel = nivelDe(datos.getNivel());

        if (datos.getMonto() <= 0) {
            throw new ReglaNegocioException("El aporte economico tiene que ser mayor que cero.");
        }
        if (datos.getCantRegistrosGratis() < 0) {
            throw new ReglaNegocioException(
                    "La cantidad de registros gratuitos no puede ser negativa.");
        }

        String codigo = limpiar(datos.getCodigoPatrocinio());
        if (codigo.isEmpty()) {
            throw new ReglaNegocioException("El codigo de patrocinio es obligatorio.");
        }
        // El codigo es unico en toda la plataforma. Hasta ahora esto solo lo
        // frenaba la restriccion de la base, que corta con un error de
        // Hibernate en vez de con un mensaje que se pueda mostrar.
        if (manejadorEvento.buscarPatrocinioPorCodigo(codigo) != null) {
            throw new ReglaNegocioException(
                    "Ya existe un patrocinio con el codigo \"" + codigo + "\".");
        }

        Institucion institucion = manejadorInstitucion.buscar(limpiar(datos.getInstitucion()));
        if (institucion == null) {
            throw new ReglaNegocioException("No existe una institucion con el nombre \""
                    + datos.getInstitucion() + "\".");
        }
        // Una institucion patrocina una edicion una sola vez.
        if (edicion.tienePatrocinioDe(institucion.getNombre())) {
            throw new ReglaNegocioException("La institucion " + institucion.getNombre()
                    + " ya tiene un patrocinio para la edicion " + edicion.getNombre() + ".");
        }

        TipoRegistro tipo = edicion.buscarTipoRegistro(limpiar(datos.getTipoRegistro()));
        if (tipo == null) {
            throw new ReglaNegocioException("La edicion " + edicion.getNombre()
                    + " no tiene un tipo de registro \"" + datos.getTipoRegistro() + "\".");
        }

        // El tope del 20%: la cuenta y la comparacion viven en Patrocinio.
        if (Patrocinio.superaElTope(datos.getMonto(), tipo.getCosto(),
                datos.getCantRegistrosGratis())) {
            throw new ReglaNegocioException(String.format(
                    "El valor de los registros gratuitos ($%.2f = %d x $%.2f) supera "
                            + "el 20%% del aporte ($%.2f). Maximo permitido: $%.2f.",
                    Patrocinio.valorDeGratis(tipo.getCosto(), datos.getCantRegistrosGratis()),
                    datos.getCantRegistrosGratis(), tipo.getCosto(),
                    datos.getMonto(), Patrocinio.topeDeGratis(datos.getMonto())));
        }

        // La fecha la pone el sistema, no el formulario (letra, seccion 6).
        Patrocinio patrocinio = new Patrocinio(LocalDate.now(), datos.getMonto(),
                datos.getCantRegistrosGratis(), codigo, nivel, institucion, tipo);

        edicion.agregarPatrocinio(patrocinio);
        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorEvento.actualizar(edicion.getEvento());
    }

    @Override
    public Set<DTTipoRegistro> listarTiposDeRegistro(String nombreEdicion)
            throws ReglaNegocioException {

        if (nombreEdicion == null || nombreEdicion.isBlank()) {
            throw new ReglaNegocioException("Hay que indicar una edicion.");
        }
        EdicionEvento edicion = manejadorEvento.buscarEdicion(nombreEdicion.trim());
        if (edicion == null) {
            throw new ReglaNegocioException(
                    "No existe una edicion con el nombre \"" + nombreEdicion + "\".");
        }
        // A diferencia de listarTiposRegistroDeEdicion(), esta NO se guarda la
        // edicion en un campo: no deja estado que la proxima llamada pueda
        // arrastrar, que es lo que el sitio necesita.
        return edicion.obtenerTiposRegistro();
    }

    /** El nivel que llega como texto desde la pantalla, o un error entendible. */
    private static NivelPatrocinio nivelDe(String texto) throws ReglaNegocioException {
        try {
            return NivelPatrocinio.valueOf(limpiar(texto).toUpperCase());
        } catch (IllegalArgumentException noEsUnNivel) {
            // valueOf tira IllegalArgumentException, que es un error de
            // programa; se traduce a ReglaNegocioException, que es lo que la
            // pantalla sabe mostrar.
            throw new ReglaNegocioException(
                    "El nivel de patrocinio tiene que ser Platino, Oro, Plata o Bronce.");
        }
    }

    @Override
    public Set<DTPatrocinio> listarPatrociniosDeEdicion(String nombreEdicion, String solicitante)
            throws ReglaNegocioException {
        throw new UnsupportedOperationException("Patrocinios de una edicion: todavia no implementado.");
    }

    @Override
    public DTPatrocinio consultarPatrocinio(String nombreEdicion, String nombreInstitucion,
                                            String solicitante)
            throws ReglaNegocioException {
        throw new UnsupportedOperationException("Consulta de Patrocinio: todavia no implementado.");
    }

    // ----- Aceptar/Rechazar Edicion de Evento (Estacion de Trabajo) -----

    /**
     * Las ediciones Ingresada de un evento, para que el administrador elija
     * cual aceptar o rechazar. No recuerda nada: la operacion siguiente recibe
     * el nombre de la edicion.
     */
    @Override
    public Set<DTEdicionEvento> listarEdicionesIngresadas(String nombreEvento)
            throws ReglaNegocioException {
        return buscarEventoExistente(nombreEvento).obtenerEdicionesIngresadas();
    }

    /**
     * Acepta una edicion: pasa a ACEPTADA y desde ese momento el sitio la
     * muestra al publico. La regla de que solo se puede desde INGRESADA vive
     * en la entidad (EdicionEvento.aceptar), que es la unica que cambia el estado.
     */
    @Override
    public void aceptarEdicion(String nombreEdicion) throws ReglaNegocioException {
        EdicionEvento edicion = buscarEdicionPorNombre(nombreEdicion);
        edicion.aceptar();
        manejadorEvento.actualizar(edicion.getEvento());
    }

    /** Rechaza una edicion. Solo se puede desde INGRESADA, igual que aceptar. */
    @Override
    public void rechazarEdicion(String nombreEdicion) throws ReglaNegocioException {
        EdicionEvento edicion = buscarEdicionPorNombre(nombreEdicion);
        edicion.rechazar();
        manejadorEvento.actualizar(edicion.getEvento());
    }

    /**
     * Busca una edicion por su nombre, que es unico en todo el sistema, sin
     * depender de un evento seleccionado antes.
     */
    private EdicionEvento buscarEdicionPorNombre(String nombreEdicion)
            throws ReglaNegocioException {
        if (nombreEdicion == null || nombreEdicion.isBlank()) {
            throw new ReglaNegocioException("Falta el nombre de la edicion.");
        }
        EdicionEvento edicion = manejadorEvento.buscarEdicion(nombreEdicion.trim());
        if (edicion == null) {
            throw new ReglaNegocioException(
                    "No existe una edicion con el nombre \"" + nombreEdicion + "\".");
        }
        return edicion;
    }

    // ----- Mantenimiento -----

    @Override
    public void refrescarDatos() {
        Persistencia.refrescar();
    }

    @Override
    public void abrirUnidadDeTrabajo() {
        Persistencia.abrirUnidadDeTrabajo();
    }

    @Override
    public void cerrarUnidadDeTrabajo() {
        Persistencia.cerrarUnidadDeTrabajo();
    }
}
