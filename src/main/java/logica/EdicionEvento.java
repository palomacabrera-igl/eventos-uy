package logica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdicionEvento {
    private String nombre;
    private String sigla;
    private LocalDate fechaIni;
    private LocalDate fechaFin;
    private LocalDate fechaAlta;
    private String ciudad;
    private String pais;
    private Organizador organizador;
    private final List<Patrocinio> patrocinios;
    private final List<TipoRegistro> tipoRegistros;
    private final List<Registro> registros;

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
        this.patrocinios = new ArrayList<>();
        this.tipoRegistros = new ArrayList<>();
        this.registros = new ArrayList<>();
    }

    public String getNombre() {return nombre;}
    public String getSigla() {return sigla;}
    public LocalDate getfechaIni() {return fechaIni;}
    public LocalDate getfechaFin() {return fechaFin;}
    public LocalDate getfechaAlta() {return fechaAlta;}
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}
    public Organizador getOrganizador() {return organizador;}

    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setSigla(String sigla) {this.sigla = sigla;}
    public void setfechaIni(LocalDate fechaIni) {this.fechaIni = fechaIni;}
    public void setfechaFin(LocalDate fechaFin) {this.fechaFin = fechaFin;}
    public void setfechaAlta(LocalDate fechaAlta) {this.fechaAlta = fechaAlta;}
    public void setCiudad(String ciudad) {this.ciudad = ciudad;}
    public void setPais(String pais) {this.pais = pais;}

    /** Paquete-visible: la usa Sistema para cargar datos de prueba (y, mas
     * adelante, Alta de Patrocinio). No es parte del contrato de la GUI. */
    void agregarPatrocinio(Patrocinio patrocinio) {
        patrocinios.add(patrocinio);
    }

    /** Paquete-visible: la usa Sistema para cargar datos de prueba. No es
     * parte del contrato de la GUI (el alta real pasa por crearTipoRegistro()). */
    void agregarTipoRegistro(TipoRegistro tipoRegistro) {
        tipoRegistros.add(tipoRegistro);
    }

    public DTEdicionEvento obtenerDT() {
        return new DTEdicionEvento(nombre, sigla, ciudad, pais,
                DTFecha.desde(fechaIni), DTFecha.desde(fechaFin), DTFecha.desde(fechaAlta));
    }

    public Set<DTPatrocinio> obtenerPatrocinios() {
        // 2.1*[foreach]: p := next()  /  2.2*: dt := obtenerDT()
        Set<DTPatrocinio> resultado = new HashSet<>();
        for (Patrocinio p : patrocinios) {
            resultado.add(p.obtenerDT());
        }
        return resultado;
    }

    public Patrocinio buscarPatrocinio(int codigo) {
        for (Patrocinio p : patrocinios) {
            if (p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    /** Busqueda interna de EdicionEvento sobre su propia coleccion de TipoRegistro. */
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
        tipoRegistros.add(tr);
        return tr;
    }

    // ===== Registro a Edicion de Evento =====

    /**
     * Tipos de registro de esta edicion como DTs.
     * 1.2.1*[foreach]: tr := next()  /  1.2.2*: dt := obtenerDT()
     */
    public Set<DTTipoRegistro> obtenerTiposRegistro() {
        Set<DTTipoRegistro> resultado = new HashSet<>();
        for (TipoRegistro t : tipoRegistros) {
            resultado.add(t.obtenerDT());
        }
        return resultado;
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
        Registro r = new Registro(asistente, this, tipoRegistro, tipoRegistro.getCosto(), fecha);
        registros.add(r);
        asistente.agregarRegistro(r);
        return r;
    }

    public DTEdicionCompleto obtenerDTCompleto() {
        Set<DTPatrocinio> dtPatrocinios = new HashSet<>();
        for (Patrocinio p : patrocinios) {
            dtPatrocinios.add(p.obtenerDT());
        }

        Set<DTTipoRegistro> dtTipos = new HashSet<>();
        for (TipoRegistro t : tipoRegistros) {
            dtTipos.add(t.obtenerDT());
        }

        Set<DTRegistro> dtRegistros = new HashSet<>();
        for (Registro r : registros) {
            dtRegistros.add(r.obtenerDT());
        }

        DTOrganizador dtOrg = organizador.obtenerDT();

        return new DTEdicionCompleto(nombre, sigla, fechaAlta, fechaIni, fechaFin,
                pais, ciudad, dtOrg, dtTipos, dtRegistros, dtPatrocinios);
    }

}