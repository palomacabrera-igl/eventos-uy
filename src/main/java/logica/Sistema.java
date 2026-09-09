package logica;

import java.time.LocalDate;
import java.util.*;

/**
 * Controlador del sistema (patron GRASP Controller, mapea a :Sistema en
 * los diagramas).
 *
 * Ya NO guarda las colecciones directamente: delega en los Manejadores
 * (ManejadorCategoria, ManejadorUsuario, ManejadorInstitucion,
 * ManejadorEvento), que son los "collection objects" de cada entidad raiz.
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
    public DTUsuario seleccionarUsuario(String nickname) {
        Usuario u = find(nickname);
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
    public void modificarDatosUsuario(DTUsuario dt) {
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
    public Set<DTEdicionEvento> listarEdicionesDeEvento(String nombreEvento) {
        Evento e = findEvento(nombreEvento);
        this.eventoSeleccionado = e;
        return e.obtenerEdiciones();
    }

    @Override
    public Set<DTPatrocinio> listarPatrociniosDeEdicion(String nombreEdicion) {
        EdicionEvento ed = eventoSeleccionado.buscarEdicion(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerPatrocinios();
    }

    @Override
    public DTPatrocinio mostrarPatrocinio(int codigoPatrocinio) {
        Patrocinio p = edicionSeleccionada.buscarPatrocinio(codigoPatrocinio);
        return p.obtenerDT();
    }

    /** Busqueda de Evento por nombre (delega en ManejadorEvento). */
    private Evento findEvento(String nombre) {
        return manejadorEvento.buscar(nombre);
    }

    // ===== Alta de Edicion de Evento =====

    @Override
    public DTEvento seleccionarEvento(String nombre) {
        Evento e = findEvento(nombre);
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
    public DTOrganizador seleccionarOrganizador(String nickname) {
        Usuario u = find(nickname);
        this.organizadorSeleccionado = (Organizador) u;
        return (DTOrganizador) u.obtenerDT();
    }

    @Override
    public boolean ingresarDatosEdicion(DTEdicionEvento dt) {
        // Una edicion no puede repetir nombre en NINGUN evento.
        if (manejadorEvento.buscarEdicion(dt.getNombre()) != null) {
            return false;
        }
        eventoSeleccionado.altaEdicion(dt, organizadorSeleccionado);
        // JPA: confirma el cambio en la base (sin transaccion se perderia). La edicion nueva
        // viaja por el cascade del @OneToMany de Evento.
        manejadorEvento.actualizar(eventoSeleccionado);
        return true;
    }

    // ===== Alta de Usuario =====

    @Override
    public boolean ingresarDatosUsuario(DTUsuario datos, TipoUsuario tipo) {
        Usuario uN = find(datos.getNickname());
        Usuario uC = findPorCorreo(datos.getCorreo());
        if (uN != null || uC != null) {
            return false;
        }
        this.datosUsuarioRecordados = datos;
        this.tipoUsuarioRecordado = tipo;
        return true;
    }

    @Override
    public void ingresarDatosAsistente(String apellido, DTFecha fechaNac) {
        Asistente a = new Asistente(datosUsuarioRecordados.getNickname(), datosUsuarioRecordados.getNombre(),
                datosUsuarioRecordados.getCorreo(), apellido, fechaNac.aLocalDate());
        manejadorUsuario.agregar(a);
        this.asistenteRecordado = a;
    }

    @Override
    public void seleccionarInstitucion(String nombreInstitucion) {
        Institucion i = findInstitucion(nombreInstitucion);
        asistenteRecordado.setInstitucion(i);
        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorUsuario.actualizar(asistenteRecordado);
    }

    @Override
    public void ingresarDatosOrganizador(String descripcion, String sitioWeb) {
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
    public DTEdicionEvento seleccionarEdicionEvento(String nombreEdicion) {
        // ed := buscarEdicion(nombreEdicion)
        EdicionEvento ed = eventoSeleccionado.buscarEdicion(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerDT();
    }

    @Override
    public boolean ingresarDatosTipoRegistro(String nombre, String descripcion, double costo, int cupo) {
        TipoRegistro tr = edicionSeleccionada.buscarTipoRegistro(nombre);
        if (tr != null) {
            return false;
        }
        edicionSeleccionada.crearTipoRegistro(nombre, descripcion, costo, cupo);
        // JPA: confirma el cambio en la base (sin transaccion se perderia). Se actualiza por el
        // Evento, que es la raiz del agregado: se llega con la referencia
        // inversa edicion -> evento.
        manejadorEvento.actualizar(edicionSeleccionada.getEvento());
        return true;
    }

    // ===== Registro a Edicion de Evento =====

    @Override
    public DTDatosRegistro listarDatosRegistro(String nombreEdicion) {
        // 1.1: ed := find(nombreEdicion)  (dentro del evento recordado)
        EdicionEvento ed = eventoSeleccionado.buscarEdicion(nombreEdicion);
        this.edicionSeleccionada = ed;
        // 1.2: registros := obtenerTiposRegistro() : Set<DTTipoRegistro>
        Set<DTTipoRegistro> tiposRegistro = ed.obtenerTiposRegistro();
        // 2*/4*: dt := obtenerDT() : DTAsistente  (todos los asistentes existentes)
        Set<DTAsistente> asistentes = listarAsistentes();
        return new DTDatosRegistro(tiposRegistro, asistentes);
    }

    @Override
    public Status altaRegistro(String nickname, String nombreEdicion, String nombreTipo) {
        // La Edicion es la recordada por listarDatosRegistro().
        EdicionEvento ed = edicionSeleccionada;
        TipoRegistro tr = ed.buscarTipoRegistro(nombreTipo);
        // 1: yaRegistrado := estaRegistrado(nickname)  /  2: hayCupo := hayCupo()
        if (ed.estaRegistrado(nickname) || !ed.hayCupo(tr)) {
            return Status.ERROR;
        }
        // 2. [!yaRegistrado y hayCupo] R := create(nickname, nombreTipo)
        Asistente a = (Asistente) find(nickname);
        ed.altaRegistro(a, tr, LocalDate.now());
        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorEvento.actualizar(ed.getEvento());
        return Status.OK;
    }

    /** Todos los asistentes existentes como DTs. La usa listarDatosRegistro(). */
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
    public Set<DTEdicionEvento> listarEdiciones() {
        Set<DTEdicionEvento> resultado = new HashSet<>();
        for (EdicionEvento ed : this.organizadorSeleccionado.getEdiciones()) {
            resultado.add(ed.obtenerDT());
        }
        return resultado;
    }

    public DTEdicionCompleto seleccionarEdicion(String nombreEdicion) {
        EdicionEvento ed = organizadorSeleccionado.buscarEdicion(nombreEdicion);
        return ed.obtenerDTCompleto();
    }

    public Set<DTRegistro> listarRegistroUsuario(String nickname) {
        Set<DTRegistro> resultado = new HashSet<>();
        Asistente asistente = (Asistente) find(nickname);
        for (Registro reg : asistente.getRegistros()) {
            resultado.add(reg.obtenerDT());
        }
        return resultado;
    }

    public DTRegistro obtenerRegistro(String nombreEdicion) {
        Asistente asistente = this.asistenteSeleccionado;
        return asistente.darRegistro(nombreEdicion);
    }

    // ===== Consulta de Registro =====

    @Override
    public DTRegistro obtenerRegistro(String nickname, String nombre) {
        // 1: u := find(nickname)  /  2: darRegistro(nombre) : DTRegistro
        Usuario u = find(nickname);
        return ((Asistente) u).darRegistro(nombre);
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
        // anidadas), reflejando la jerarquia completa. 2*: dt := obtenerDT().
        Set<DTCategoria> raices = new HashSet<>();
        for (Categoria cat : manejadorCategoria.listar()) {
            if (cat.esRaiz()) {
                raices.add(cat.obtenerDT());
            }
        }
        return raices;
    }

    @Override
    public Status altaCategoria(String nombre, String nombrePadre) {
        // 1: existente := find(nombre)  (el nombre es unico en toda la plataforma)
        if (findCategoria(nombre) != null) {
            return Status.ERROR;
        }
        // 2: [existente == null] cat := create(nombre)
        Categoria nueva = new Categoria(nombre);
        // Si se indico un padre, la cuelga de el (queda como hija); si no, es raiz.
        if (nombrePadre != null && !nombrePadre.isBlank()) {
            Categoria padre = findCategoria(nombrePadre);
            padre.agregarHija(nueva);
        }
        // 3: add(cat)  -- igual se registra por nombre (unicidad global + busqueda)
        manejadorCategoria.agregar(nueva);
        return Status.OK;
    }

    /** Busqueda de Categoria por nombre (delega en ManejadorCategoria). */
    private Categoria findCategoria(String nombre) {
        return manejadorCategoria.buscar(nombre);
    }

    // ===== Consulta de Tipo de Registro =====

    @Override
    public Set<DTTipoRegistro> listarTiposRegistroDeEdicion(String nombreEdicion) {
        EdicionEvento edicion = eventoSeleccionado.buscarEdicion(nombreEdicion);
        this.edicionSeleccionada = edicion;
        return edicion.obtenerTiposRegistro();
    }

    @Override
    public DTTipoRegistro seleccionarTipoRegistro(String nombreTipoRegistro) {
        TipoRegistro tipo = edicionSeleccionada.buscarTipoRegistro(nombreTipoRegistro);
        return tipo.obtenerDT();
    }

    // ===== Alta institucion =====
    public Status altaInstitucion(String nombre, String descripcion, String sitioWeb) {
        if (findInstitucion(nombre) != null) {
            return Status.ERROR;
        }
        Institucion institucion = new Institucion(
                nombre,
                descripcion,
                sitioWeb
        );
        manejadorInstitucion.agregar(institucion);
        return Status.OK;
    }

    // ===== Consulta de Edicion de Evento =====

    @Override
    public DTEdicionCompleto seleccionarEdicionCompleta(String nombreEdicion) {
        EdicionEvento ed = eventoSeleccionado.buscarEdicion(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerDTCompleto();
    }

    // ===== Alta de Patrocinio =====

    @Override
    public void altaPatrocinio(DTPatrocinio dt) throws ReglaNegocioException {
        Institucion institucion = findInstitucion(dt.getInstitucion());
        TipoRegistro tipo = edicionSeleccionada.buscarTipoRegistro(dt.getTipoRegistro());

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
                dt.getCantRegistrosGratis(), dt.getCodigoPatrocinio(), dt.getNivel(),
                institucion, tipo);
        edicionSeleccionada.agregarPatrocinio(p);
        // JPA: confirma el cambio en la base (sin transaccion se perderia).
        manejadorEvento.actualizar(edicionSeleccionada.getEvento());
    }
    // ===== Alta Evento =====
    public Status ingresarDatosEvento(String nombre, String descripcion, LocalDate fechaAlta, String sigla, List<String> nombresCategorias) {
        // Validar unicidad del evento
        if (findEvento(nombre) != null) {
            return Status.ERROR;
        }

        // Convertir nombres en objetos Categoria
        List<Categoria> categoriasEvento = new ArrayList<>();
        for (String nombreCat : nombresCategorias) {
            Categoria cat = findCategoria(nombreCat);
            if (cat != null) {
                categoriasEvento.add(cat);
            } else {
                // Si alguna categoría no existe, podés decidir si abortar o ignorar
                return Status.ERROR;
            }
        }

        // Validar que haya al menos una categoría
        if (categoriasEvento.isEmpty()) {
            return Status.ERROR;
        }

        // Crear y agregar el evento
        Evento evento = new Evento(nombre, descripcion, fechaAlta, sigla, categoriasEvento);
        manejadorEvento.agregar(evento);

        return Status.OK;
    }

}
