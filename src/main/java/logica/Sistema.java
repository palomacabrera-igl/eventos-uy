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
        // 1: u := find(nickname)  /  2: dt := obtenerDT()
        Usuario u = find(nickname);
        this.usuarioSeleccionado = u;
        return u.obtenerDT();
    }

    @Override
    public void modificarDatosUsuario(DTUsuario dt) {
        // 1: usuarioSeleccionado.modificarDatos(dt)
        usuarioSeleccionado.modificarDatos(dt);
    }

    /** Busqueda interna de Sistema sobre su propia coleccion de Usuario. */
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
        // 1*[foreach]: e := next()  /  2*: dt := obtenerDT()
        Set<DTEvento> resultado = new HashSet<>();
        for (Evento e : eventos) {
            resultado.add(e.obtenerDT());
        }
        return resultado;
    }

    @Override
    public Set<DTEdicionEvento> listarEdicionesDeEvento(String nombreEvento) {
        // 1: eventoSeleccionado := find(nombreEvento)
        // 2: dts := eventoSeleccionado.obtenerEdiciones()
        Evento e = findEvento(nombreEvento);
        this.eventoSeleccionado = e;
        return e.obtenerEdiciones();
    }

    @Override
    public Set<DTPatrocinio> listarPatrociniosDeEdicion(String nombreEdicion) {
        // 1: edicionSeleccionada := eventoSeleccionado.buscarEdicion(nombreEdicion)
        // 2: dts := edicionSeleccionada.obtenerPatrocinios()
        EdicionEvento ed = eventoSeleccionado.buscarEdicion(nombreEdicion);
        this.edicionSeleccionada = ed;
        return ed.obtenerPatrocinios();
    }

    @Override
    public DTPatrocinio mostrarPatrocinio(int codigoPatrocinio) {
        // 1: p := edicionSeleccionada.buscarPatrocinio(codigoPatrocinio)
        // 2: dt := obtenerDT()
        Patrocinio p = edicionSeleccionada.buscarPatrocinio(codigoPatrocinio);
        return p.obtenerDT();
    }

    /** Busqueda interna de Sistema sobre su propia coleccion de Evento. */
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

        TipoRegistro entradaGeneral = new TipoRegistro("General", "Entrada general",
                50.0, 200);

        Patrocinio patrocinioUtec = new Patrocinio(LocalDate.of(2026, 2, 1), 5000.0,
                10, 1001, NivelPatrocinio.ORO, utec, entradaGeneral);
        jiap2026.agregarPatrocinio(patrocinioUtec);
    }

}