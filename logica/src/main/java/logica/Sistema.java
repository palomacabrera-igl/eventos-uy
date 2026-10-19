package logica;

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
    public DTPatrocinio mostrarPatrocinio(int codigoPatrocinio)
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
                             Integer codigoPatrocinio) throws ReglaNegocioException {
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
                                               Asistente asistente, int codigo)
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
}
