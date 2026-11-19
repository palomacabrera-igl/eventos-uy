package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "edicion_evento")
public class EdicionEvento extends EntidadBase {

    /** Nombre unico en la plataforma */
    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(length = 20)
    private String sigla;

    private LocalDate fechaIni;
    private LocalDate fechaFin;
    private LocalDate fechaAlta;

    /** Dia en que el organizador archivo la edicion; null mientras siga activa. */
    private LocalDate fechaArchivado;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String pais;

    /**
     * @Enumerated(STRING) guarda el nombre ('INGRESADA'), no el numero de
     * orden.
     *
     * El valor inicial va aqui y no en el constructor: asi toda edicion nace
     * INGRESADA sin tocar ninguna firma.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEdicion estado = EstadoEdicion.INGRESADA;

    /** Imagen opcional (letra, seccion 7.2). Queda null cuando no tiene. */
    @Embedded
    private Imagen imagen;

    /** Video de YouTube opcional (letra, 7.4), guardado como https://www.youtube.com/watch?v=ID. */
    @Column(length = 100)
    private String urlVideo;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "evento_id",
                foreignKey = @ForeignKey(name = "fk_edicion_evento"))
    private Evento evento;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "organizador_id",
                foreignKey = @ForeignKey(name = "fk_edicion_organizador"))
    private Organizador organizador;

    /* Los tres lados INVERSOS: la FK edicion_id vive en cada tabla hija. */

    @OneToMany(mappedBy = "edicion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Patrocinio> patrocinios = new ArrayList<>();

    @OneToMany(mappedBy = "edicion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TipoRegistro> tipoRegistros = new ArrayList<>();

    @OneToMany(mappedBy = "edicion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Registro> registros = new ArrayList<>();

    protected EdicionEvento() {}

    public EdicionEvento(String nombre, String sigla, LocalDate fechaIni,
                         LocalDate fechaFin, LocalDate fechaAlta, String ciudad,
                         String pais, Organizador organizador) {
        this.nombre = nombre;
        this.sigla = sigla;
        this.fechaIni = fechaIni;
        this.fechaFin = fechaFin;
        this.fechaAlta = fechaAlta;
        this.ciudad = ciudad;
        this.pais = pais;
        this.organizador = organizador;
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public LocalDate getfechaIni() {return fechaIni;}
    public LocalDate getfechaFin() {return fechaFin;}
    public LocalDate getfechaAlta() {return fechaAlta;}
    public LocalDate getFechaArchivado() {return fechaArchivado;}
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}
    public EstadoEdicion getEstado() {return estado;}
    public Organizador getOrganizador() {return organizador;}
    public Evento getEvento() {return evento;}

    void setEvento(Evento evento) {this.evento = evento;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setfechaIni(LocalDate fechaIni) {this.fechaIni = fechaIni;}
    public void setfechaFin(LocalDate fechaFin) {this.fechaFin = fechaFin;}
    public void setfechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
    public void setCiudad(String ciudad) {this.ciudad = ciudad;}
    public void setPais(String pais) {this.pais = pais;}

    public Imagen getImagen() {return imagen;}

    public boolean tieneImagen() {
        return imagen != null && imagen.getContenido() != null;
    }

    /** La imagen se agrega o se reemplaza despues del alta, no en el constructor. */
    public void asignarImagen(Imagen imagen) {this.imagen = imagen;}

    public String getUrlVideo() {return urlVideo;}

    /** Recibe la direccion que devuelve VideoYoutube.normalizar, o null si no tiene video. */
    public void asignarVideo(String urlVideo) {this.urlVideo = urlVideo;}

    // ===== Estados de la edicion (letra, Tabla 1) =====

    /** true si la edicion se puede listar al publico (letra, seccion 4). */
    public boolean estaAceptada() {return estado == EstadoEdicion.ACEPTADA;}

    public void aceptar() throws ReglaNegocioException {
        cambiarEstado(EstadoEdicion.ACEPTADA);
    }

    public void rechazar() throws ReglaNegocioException {
        cambiarEstado(EstadoEdicion.RECHAZADA);
    }

    /**
     * Una vez terminada, una edicion aceptada deja de estar disponible al
     * publico. La fecha se recibe para mantener la regla testeable y para que
     * la operacion del controlador no dependa de campos compartidos.
     */
    public void archivar(LocalDate hoy) throws ReglaNegocioException {
        if (!estaAceptada()) {
            throw new ReglaNegocioException("La edición \"" + nombre
                    + "\" debe estar aceptada para archivarse.");
        }
        if (fechaFin == null || !fechaFin.isBefore(hoy)) {
            throw new ReglaNegocioException("La edición \"" + nombre
                    + "\" solo se puede archivar después de su fecha de fin.");
        }
        estado = EstadoEdicion.ARCHIVADA;
        fechaArchivado = hoy;
    }

    /** La misma condicion que archivar(), sin cambiar la entidad. */
    public boolean sePuedeArchivar(LocalDate hoy) {
        return estaAceptada() && fechaFin != null && fechaFin.isBefore(hoy);
    }

    /**
     * Las transiciones validas son solo las de la Tabla 1: desde INGRESADA se
     * puede pasar a ACEPTADA o a RECHAZADA, y de ahi no se vuelve. No hay un
     * setEstado() a proposito: estas dos son las unicas puertas.
     */
    private void cambiarEstado(EstadoEdicion nuevo) throws ReglaNegocioException {
        if (estado != EstadoEdicion.INGRESADA) {
            throw new ReglaNegocioException("La edicion '" + nombre + "' ya esta "
                    + estado.name().toLowerCase() + ": su estado no se puede cambiar.");
        }
        estado = nuevo;
    }


    void agregarPatrocinio(Patrocinio patrocinio) {
        patrocinios.add(patrocinio);
        patrocinio.setEdicion(this);
    }


    void agregarTipoRegistro(TipoRegistro tipoRegistro) {
        tipoRegistros.add(tipoRegistro);
        tipoRegistro.setEdicion(this);
    }

    public DTEdicionEvento obtenerDT() {
        DTEdicionEvento dt = new DTEdicionEvento(nombre, sigla, ciudad, pais,
                DTFecha.desde(fechaIni), DTFecha.desde(fechaFin), DTFecha.desdeOpcional(fechaAlta),
                estado, tieneImagen(),
                evento == null ? null : evento.getNombre());
        dt.setUrlVideo(urlVideo);
        return dt;
    }

    public Set<DTPatrocinio> obtenerPatrocinios() {
        // 2.1*[foreach]: p := next()  /  2.2*: dt := obtenerDT()
        Set<DTPatrocinio> resultado = new HashSet<>();
        for (Patrocinio p : patrocinios) {
            resultado.add(p.obtenerDT());
        }
        return resultado;
    }

    /**
     * true si esta edicion la organiza esa persona.
     *
     * La letra pide, para el Alta de Patrocinio, que el organizador vea "las
     * ediciones de ese evento que el organiza": nadie patrocina la edicion de
     * otro.
     */
    public boolean loOrganiza(String nickname) {
        return nickname != null && organizador != null
                && organizador.getNickname().equals(nickname.trim());
    }

    public Patrocinio buscarPatrocinio(String codigo) {
        for (Patrocinio p : patrocinios) {
            // equals y no ==: con texto, == compara si son el MISMO objeto
            // en memoria, no si dicen lo mismo.
            if (p.getCodigo().equals(codigo)) {
                return p;
            }
        }
        return null;
    }

    /**
     * true si la institucion ya tiene un patrocinio en esta edicion.
     * Una institucion puede tener como maximo un patrocinio para una misma edicion de evento.
     */
    public boolean tienePatrocinioDe(String nombreInstitucion) {
        for (Patrocinio p : patrocinios) {
            if (p.getInstitucion().getNombre().equals(nombreInstitucion)) {
                return true;
            }
        }
        return false;
    }

    public TipoRegistro buscarTipoRegistro(String nombre) {
        for (TipoRegistro t : tipoRegistros) {
            if (t.getNombre().equals(nombre)) {
                return t;
            }
        }
        return null;
    }

    /** Crea un TipoRegistro y lo agrega a esta edicion (Creator: EdicionEvento contiene la coleccion). */
    public TipoRegistro crearTipoRegistro(String nombre, String descripcion, double costo, int cupo) {
        TipoRegistro tr = new TipoRegistro(nombre, descripcion, costo, cupo);
        agregarTipoRegistro(tr);
        return tr;
    }

    // ===== Registro a Edicion de Evento =====

    public Set<DTTipoRegistro> obtenerTiposRegistro() {
        Set<DTTipoRegistro> resultado = new HashSet<>();
        for (TipoRegistro t : tipoRegistros) {
            resultado.add(t.obtenerDT());
        }
        return resultado;
    }

    /**
     * Los registros de la edicion (CU 13). Son solo para su organizador: a
     * cualquier otro le llega un error, no una lista vacia, para que la
     * pagina no muestre "nadie se registro" cuando en realidad no le toca verlo.
     */
    public Set<DTRegistro> obtenerRegistrosPara(String solicitante)
            throws ReglaNegocioException {
        if (!loOrganiza(solicitante)) {
            throw new ReglaNegocioException(
                    "Solo el organizador de la edición puede ver sus registros.");
        }
        Set<DTRegistro> resultado = new HashSet<>();
        for (Registro r : registros) {
            resultado.add(r.obtenerDT());
        }
        return resultado;
    }

    /**
     * El registro de un asistente en esta edicion (CU 13). Lo pueden ver el
     * asistente duenio del registro y el organizador de la edicion, nadie mas.
     *
     * El permiso se mira ANTES de buscar, y el mensaje es el mismo si no
     * existe o si no le corresponde: asi nadie averigua si otra persona esta
     * registrada probando nicknames.
     */
    public DTRegistro obtenerRegistroPara(String nickname, String solicitante)
            throws ReglaNegocioException {
        String noEncontrado = "No encontramos ese registro, o no te corresponde verlo.";
        boolean esElAsistente = solicitante != null && nickname != null
                && solicitante.trim().equals(nickname.trim());
        if (!esElAsistente && !loOrganiza(solicitante)) {
            throw new ReglaNegocioException(noEncontrado);
        }
        for (Registro r : registros) {
            if (r.getAsistente().getNickname().equals(nickname.trim())) {
                return r.obtenerDT();
            }
        }
        throw new ReglaNegocioException(noEncontrado);
    }

    /**
     * true si el asistente (identificado por su nickname) ya tiene un registro
     * en esta edicion.
     */
    public boolean estaRegistrado(String nickname) {
        for (Registro r : registros) {
            if (r.getAsistente().getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    /**
     * true si el tipo de registro todavia tiene cupo, es decir, si la cantidad
     * de registros de ese tipo en esta edicion es menor a su cupo.
     */
    public boolean hayCupo(TipoRegistro tipoRegistro) {
        int cantidad = 0;
        for (Registro r : registros) {
            if (r.getTipoRegistro() == tipoRegistro) {
                cantidad++;
            }
        }
        return cantidad < tipoRegistro.getCupo();
    }

    /**
     * Crea un Registro con la fecha y el costo indicados (costo = t.costo) y lo
     * vincula con esta edicion y con el asistente. Creator: EdicionEvento
     * contiene la coleccion de Registro.
     */
    public Registro altaRegistro(Asistente asistente, TipoRegistro tipoRegistro, LocalDate fecha) {
        return altaRegistro(asistente, tipoRegistro, fecha, null);
    }

    /**
     * Da de alta el registro. Si viene un patrocinio, el registro es uno de los
     * gratuitos que esa institucion otorga y su costo es cero (ver letra, seccion 4).
     */
    public Registro altaRegistro(Asistente asistente, TipoRegistro tipoRegistro,
                                 LocalDate fecha, Patrocinio patrocinio) {
        double costo = (patrocinio == null) ? tipoRegistro.getCosto() : 0.0;
        Registro r = new Registro(asistente, this, tipoRegistro, costo, fecha, patrocinio);
        registros.add(r);
        asistente.agregarRegistro(r);
        return r;
    }

    /** Cuantos de los registros gratuitos de ese patrocinio ya se usaron. */
    public int registrosGratisUsados(Patrocinio patrocinio) {
        int cantidad = 0;
        for (Registro r : registros) {
            if (r.getPatrocinio() == patrocinio) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // ===== Rol 4: asistencia =====

    /**
     * Registra la asistencia de un asistente a esta edicion (letra, 6.3).
     *
     * <ul>
     *   <li>Solo puede hacerlo quien esta registrado en ella: a cualquier otro
     *       (un organizador, un asistente que no se registro, un visitante) le
     *       llega el mismo mensaje.</li>
     *   <li>Una sola vez: la segunda es un error.</li>
     *   <li>La edicion tiene que estar Aceptada (una Archivada ya no admite
     *       cambios) y tiene que haber empezado: no se puede haber asistido a algo
     *       que todavia no paso. Despues de su fecha de fin si se puede, hasta
     *       que el organizador la archive.</li>
     * </ul>
     *
     * "hoy" llega como parametro, como en altaRegistro, para poder probar la
     * regla de la fecha sin depender del reloj.
     */
    public void registrarAsistencia(String nickname, LocalDate hoy) throws ReglaNegocioException {
        Registro registro = buscarRegistroDe(nickname);
        if (registro == null) {
            throw new ReglaNegocioException(
                    "No estás registrado en la edición \"" + nombre + "\".");
        }
        // Si ya la habia registrado, lo que tiene que saber es eso, aunque la
        // edicion se haya archivado despues: lo dice Registro.registrarAsistencia().
        if (!registro.getAsistio()) {
            exigirQueSePuedaAsistir(hoy);
        }
        registro.registrarAsistencia();
    }

    /**
     * La constancia de asistencia en PDF (letra, 6.2 y 7.5). Solo la recibe quien
     * esta registrado en la edicion y ya registro su asistencia; a cualquier otro
     * le llega un error, no un PDF vacio.
     *
     * No mira el estado de la edicion: una edicion archivada conserva sus
     * registros y cada asistente sigue viendo el suyo, constancia incluida.
     * "hoy" es la fecha de emision y llega como parametro, como en
     * registrarAsistencia, para poder probarla.
     */
    public byte[] obtenerConstancia(String nickname, LocalDate hoy) throws ReglaNegocioException {
        Registro registro = buscarRegistroDe(nickname);
        if (registro == null) {
            throw new ReglaNegocioException(
                    "No estás registrado en la edición \"" + nombre + "\".");
        }
        if (!registro.getAsistio()) {
            throw new ReglaNegocioException("Todavía no registraste tu asistencia a la edición \""
                    + nombre + "\": la constancia es solo para quien asistió.");
        }
        Asistente asistente = registro.getAsistente();
        return ConstanciaAsistencia.generar(new ConstanciaAsistencia.Datos(
                asistente.getNombre(), asistente.getApellido(),
                evento == null ? null : evento.getNombre(), nombre, ciudad,
                fechaIni, fechaFin, hoy));
    }

    private void exigirQueSePuedaAsistir(LocalDate hoy) throws ReglaNegocioException {
        if (estado == EstadoEdicion.ARCHIVADA) {
            throw new ReglaNegocioException("La edición \"" + nombre
                    + "\" está archivada: ya no admite registrar asistencia.");
        }
        if (!estaAceptada()) {
            throw new ReglaNegocioException("La edición \"" + nombre
                    + "\" no está aceptada: no admite registrar asistencia.");
        }
        if (fechaIni != null && hoy.isBefore(fechaIni)) {
            throw new ReglaNegocioException("La edición \"" + nombre
                    + "\" todavía no empezó (empieza el "
                    + fechaIni.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + "): no se puede registrar la asistencia.");
        }
    }

    /** El registro de ese asistente en esta edicion, o null si no esta registrado. */
    private Registro buscarRegistroDe(String nickname) {
        if (nickname == null) {
            return null;
        }
        for (Registro r : registros) {
            if (r.getAsistente().getNickname().equals(nickname.trim())) {
                return r;
            }
        }
        return null;
    }

    public DTEdicionCompleto obtenerDTCompleto() {
        List<DTPatrocinio> dtPatrocinios = new ArrayList<>();
        for (Patrocinio p : patrocinios) {
            dtPatrocinios.add(p.obtenerDT());
        }

        List<DTTipoRegistro> dtTipos = new ArrayList<>();
        for (TipoRegistro t : tipoRegistros) {
            dtTipos.add(t.obtenerDT());
        }

        List<DTRegistro> dtRegistros = new ArrayList<>();
        for (Registro r : registros) {
            dtRegistros.add(r.obtenerDT());
        }

        DTOrganizador dtOrg = organizador.obtenerDT();

        DTEdicionCompleto dt = new DTEdicionCompleto(nombre, sigla, DTFecha.desdeOpcional(fechaAlta),
                DTFecha.desdeOpcional(fechaIni), DTFecha.desdeOpcional(fechaFin),
                pais, ciudad, dtOrg, dtTipos, dtRegistros, dtPatrocinios,
                estado, tieneImagen());
        dt.setUrlVideo(urlVideo);
        return dt;
    }

    // ===== Consulta de Edicion (CU 9) =====

    /**
     * Quien puede ver esta edicion (letra, seccion 4): una Aceptada la ve
     * cualquiera, incluso un visitante; una Ingresada, Rechazada o Archivada,
     * solo su organizador.
     */
    public boolean puedeVerla(String solicitante) {
        return estaAceptada() || loOrganiza(solicitante);
    }

    /**
     * La edicion completa, recortada segun quien pregunta (requisito R14):
     * el listado de registros y los codigos de patrocinio son solo para su
     * organizador. A cualquier otro le llegan sin registros y con los
     * patrocinios sin codigo. Que la regla este aca, y no en la JSP, es lo
     * que impide que una pagina la olvide.
     */
    /**
     * Los patrocinios de la edicion (CU 16). El codigo de patrocinio viaja
     * solo si quien pregunta es su organizador; para el resto llega en null.
     */
    public Set<DTPatrocinio> obtenerPatrociniosPara(String solicitante) {
        boolean conCodigo = loOrganiza(solicitante);
        Set<DTPatrocinio> resultado = new HashSet<>();
        for (Patrocinio p : patrocinios) {
            resultado.add(p.obtenerDTPara(conCodigo));
        }
        return resultado;
    }

    /**
     * El patrocinio de una institucion en esta edicion (CU 16), con la misma
     * regla del codigo. Una institucion tiene a lo sumo un patrocinio por
     * edicion, asi que la institucion lo identifica.
     */
    public DTPatrocinio obtenerPatrocinioPara(String nombreInstitucion, String solicitante)
            throws ReglaNegocioException {
        for (Patrocinio p : patrocinios) {
            if (p.getInstitucion().getNombre().equals(nombreInstitucion)) {
                return p.obtenerDTPara(loOrganiza(solicitante));
            }
        }
        throw new ReglaNegocioException("La institución \"" + nombreInstitucion
                + "\" no patrocina la edición \"" + nombre + "\".");
    }

    public DTEdicionCompleto obtenerDTCompletoPara(String solicitante) {
        boolean esSuOrganizador = loOrganiza(solicitante);

        List<DTTipoRegistro> dtTipos = new ArrayList<>();
        for (TipoRegistro t : tipoRegistros) {
            dtTipos.add(t.obtenerDT());
        }

        // Mismos patrocinios, pero el codigo solo viaja para su organizador.
        List<DTPatrocinio> dtPatrocinios = new ArrayList<>(obtenerPatrociniosPara(solicitante));

        List<DTRegistro> dtRegistros = new ArrayList<>();
        if (esSuOrganizador) {
            for (Registro r : registros) {
                dtRegistros.add(r.obtenerDT());
            }
        }

        DTEdicionCompleto dt = new DTEdicionCompleto(nombre, sigla, DTFecha.desdeOpcional(fechaAlta),
                DTFecha.desdeOpcional(fechaIni), DTFecha.desdeOpcional(fechaFin),
                pais, ciudad, organizador.obtenerDT(), dtTipos, dtRegistros, dtPatrocinios,
                estado, tieneImagen(), evento == null ? null : evento.getNombre());
        dt.setUrlVideo(urlVideo);
        return dt;
    }

    // ===== Alta de Edicion (CU 8) =====

    /**
     * Revisa los datos de una edicion nueva antes de crearla. Todos los campos
     * son obligatorios y la fecha de fin no puede ser anterior a la de inicio
     * (puede ser el mismo dia: una edicion de un dia). El nombre repetido lo
     * controla Sistema, porque hay que buscar en la base.
     */
    public static void validarDatos(String nombre, String sigla, String ciudad, String pais,
                                    LocalDate fechaIni, LocalDate fechaFin)
            throws ReglaNegocioException {
        if (estaVacio(nombre) || estaVacio(sigla) || estaVacio(ciudad) || estaVacio(pais)) {
            throw new ReglaNegocioException(
                    "El nombre, la sigla, la ciudad y el país son obligatorios.");
        }
        if (fechaIni == null || fechaFin == null) {
            throw new ReglaNegocioException("Las fechas de inicio y de fin son obligatorias.");
        }
        if (fechaFin.isBefore(fechaIni)) {
            throw new ReglaNegocioException(
                    "La fecha de fin no puede ser anterior a la de inicio.");
        }
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}