package logica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controlador del sistema (patron GRASP Controller, mapea a :Sistema en
 * los diagramas). Mantiene las colecciones: Categoria, Usuario, Institucion y Evento, y las
 * referencias retenidas entre llamados.
 */
public class Sistema implements IControladorSistema {

    private final List<Categoria> categorias;
    private final List<Usuario> usuarios;
    private final List<Institucion> instituciones;
    private final List<Evento> eventos;

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
        this.categorias = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.instituciones = new ArrayList<>();
        this.eventos = new ArrayList<>();
        cargarDatosDePrueba();
    }

    // ===== Modificar Datos de Usuario =====

    @Override
    public Set<DTUsuario> listarUsuarios() {
        // 1*[foreach]: u := next()  /  2*: dt := obtenerDT()
        Set<DTUsuario> resultado = new HashSet<>();
        for (Usuario u : usuarios) {
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
    }

    /**
     * Busqueda interna de Sistema sobre su propia coleccion de Usuario.
     */
    private Usuario find(String nickname) {
        for (Usuario u : usuarios) {
            if (u.getNickname().equals(nickname)) {
                return u;
            }
        }
        return null;
    }

    // ===== Consulta de Patrocinio =====

    @Override
    public Set<DTEvento> listarEventos() {
        Set<DTEvento> resultado = new HashSet<>();
        for (Evento e : eventos) {
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

    /**
     * Busqueda interna de Sistema sobre su propia coleccion de Evento.
     */
    private Evento findEvento(String nombre) {
        for (Evento e : eventos) {
            if (e.getNombre().equals(nombre)) {
                return e;
            }
        }
        return null;
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
        for (Usuario u : usuarios) {
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
        for (Evento e : eventos) {
            EdicionEvento ed = e.buscarEdicion(dt.getNombre());
            if (ed != null) {
                return false;
            }
        }
        eventoSeleccionado.altaEdicion(dt, organizadorSeleccionado);
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
        usuarios.add(a);
        this.asistenteRecordado = a;
    }

    @Override
    public void seleccionarInstitucion(String nombreInstitucion) {
        Institucion i = findInstitucion(nombreInstitucion);
        asistenteRecordado.setInstitucion(i);
    }

    @Override
    public void ingresarDatosOrganizador(String descripcion, String sitioWeb) {
        Organizador o = new Organizador(datosUsuarioRecordados.getNickname(), datosUsuarioRecordados.getNombre(),
                datosUsuarioRecordados.getCorreo(), descripcion, sitioWeb);
        usuarios.add(o);
    }

    @Override
    public Set<String> listarNombresInstituciones() {
        Set<String> resultado = new HashSet<>();
        for (Institucion i : instituciones) {
            resultado.add(i.getNombre());
        }
        return resultado;
    }

    /** Busqueda interna de Sistema sobre su propia coleccion de Usuario. */
    private Usuario findPorCorreo(String correo) {
        for (Usuario u : usuarios) {
            if (u.getCorreoElectronico().equals(correo)) {
                return u;
            }
        }
        return null;
    }

    /** Busqueda interna de Sistema sobre su propia coleccion de Institucion. */
    private Institucion findInstitucion(String nombre) {
        for (Institucion i : instituciones) {
            if (i.getNombre().equals(nombre)) {
                return i;
            }
        }
        return null;
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
        return Status.OK;
    }

    /** Todos los asistentes existentes como DTs. La usa listarDatosRegistro(). */
    private Set<DTAsistente> listarAsistentes() {
        Set<DTAsistente> resultado = new HashSet<>();
        for (Usuario u : usuarios) {
            if (u.obtenerTipoUsuario() == TipoUsuario.ASISTENTE) {
                resultado.add((DTAsistente) u.obtenerDT());
            }
        }
        return resultado;
    }

    /**
     * TEMPORAL: hasta que Alta de Usuario, Alta de Evento y Alta de
     * Patrocinio esten implementados, precarga en memoria un Usuario, un
     * Evento con una edicion, y un patrocinio para esa edicion, para poder
     * probar los 3 casos de uso de punta a punta. No es parte de ningun
     * caso de uso.
     */
    private void cargarDatosDePrueba() {
        usuarios.add(new Asistente("pfernandez", "Paloma", "paloma@example.com",
                "Fernandez", LocalDate.of(2000, 5, 14)));
        Organizador organizadorUtec = new Organizador("utec", "UTEC Eventos", "eventos@utec.edu.uy",
                "Organizador institucional de UTEC", "https://utec.edu.uy");
        usuarios.add(organizadorUtec);

        Institucion utec = new Institucion("UTEC", "Universidad Tecnologica",
                "https://utec.edu.uy");
        instituciones.add(utec);

        Evento jiap = new Evento("JIAP", "JIAP", "Jornadas de Ingenieria y Aplicaciones",
                LocalDate.of(2025, 1, 10));
        eventos.add(jiap);

        EdicionEvento jiap2026 = new EdicionEvento("JIAP 2026", "JIAP26",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 1, 15), "Montevideo", "Uruguay", organizadorUtec);
        jiap.agregarEdicion(jiap2026);
        organizadorUtec.agregarEdicion(jiap2026);

        EdicionEvento jiap2025 = new EdicionEvento("JIAP 2025", "JIAP25",
                LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 3),
                LocalDate.of(2025, 1, 15), "Montevideo", "Uruguay", organizadorUtec);
        jiap.agregarEdicion(jiap2025);
        organizadorUtec.agregarEdicion(jiap2025);

        Evento semanaIngenieria = new Evento("Semana de la Ingenieria", "SI",
                "Charlas y talleres de ingenieria", LocalDate.of(2025, 3, 1));
        eventos.add(semanaIngenieria);

        EdicionEvento si2026 = new EdicionEvento("SI 2026", "SI26",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14),
                LocalDate.of(2026, 6, 1), "Montevideo", "Uruguay", organizadorUtec);
        semanaIngenieria.agregarEdicion(si2026);
        organizadorUtec.agregarEdicion(si2026);

        TipoRegistro entradaGeneral = new TipoRegistro("General", "Entrada general",
                50.0, 200);
        jiap2026.agregarTipoRegistro(entradaGeneral);

        Patrocinio patrocinioUtec = new Patrocinio(LocalDate.of(2026, 2, 1), 5000.0,
                10, 1001, NivelPatrocinio.ORO, utec, entradaGeneral);
        jiap2026.agregarPatrocinio(patrocinioUtec);

        Asistente paloma = (Asistente) usuarios.get(0);

        jiap2026.altaRegistro(paloma, entradaGeneral, LocalDate.of(2026, 9, 1));

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
        // 1*[foreach]: cat := next()  /  2*: dt := obtenerDT() : DTCategoria
        Set<DTCategoria> resultado = new HashSet<>();
        for (Categoria cat : categorias) {
            resultado.add(cat.obtenerDT());
        }
        return resultado;
    }

    @Override
    public Status altaCategoria(String nombre) {
        // 1: existente := find(nombre) : Categoria
        Categoria existente = findCategoria(nombre);
        if (existente != null) {
            return Status.ERROR;
        }
        // 2: [existente == null] cat := create(nombre)  /  3: add(cat)
        categorias.add(new Categoria(nombre));
        return Status.OK;
    }

    /** Busqueda interna de Sistema sobre su propia coleccion de Categoria. */
    private Categoria findCategoria(String nombre) {
        for (Categoria cat : categorias) {
            if (cat.getNombre().equals(nombre)) {
                return cat;
            }
        }
        return null;
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
        instituciones.add(institucion);
        return Status.OK;
    }

    // ===== Consulta de Edicion de Evento =====

    @Override
    public DTEdicionCompleto seleccionarEdicionCompleta(String nombreEdicion) {
        EdicionEvento ed = eventoSeleccionado.buscarEdicion(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerDTCompleto();
    }
}