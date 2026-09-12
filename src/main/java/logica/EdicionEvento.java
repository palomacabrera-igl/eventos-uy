package logica;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
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

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String pais;

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
    public String getCiudad() {return ciudad;}
    public String getPais() {return pais;}
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


    void agregarPatrocinio(Patrocinio patrocinio) {
        patrocinios.add(patrocinio);
        patrocinio.setEdicion(this);
    }


    void agregarTipoRegistro(TipoRegistro tipoRegistro) {
        tipoRegistros.add(tipoRegistro);
        tipoRegistro.setEdicion(this);
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