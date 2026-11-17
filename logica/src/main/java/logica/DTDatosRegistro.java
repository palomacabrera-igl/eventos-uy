package logica;

import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * <<DataType>> DTDatosRegistro del DSS de Registro a Edicion de Evento.
 * Empaqueta, para una edicion, sus tipos de registro y todos los asistentes
 * existentes, de modo que la GUI llene los combos de "Asistentes" y
 * "Tipos de Registro" con una sola llamada (listarDatosRegistro()).
 *
 * Las colecciones son List y no Set porque JAXB trabaja con List.
 */
@XmlType(name = "DTDatosRegistro")
public class DTDatosRegistro {

    private List<DTTipoRegistro> tipoRegistro = new ArrayList<>();
    private List<DTAsistente> asistentes = new ArrayList<>();

    /** Lo usa JAXB: crea el objeto vacio y despues completa cada campo con su setter. */
    public DTDatosRegistro() {
    }

    public DTDatosRegistro(List<DTTipoRegistro> tipoRegistro, List<DTAsistente> asistentes) {
        this.tipoRegistro = tipoRegistro;
        this.asistentes = asistentes;
    }

    public List<DTTipoRegistro> getTipoRegistro() { return tipoRegistro; }
    public List<DTAsistente> getAsistentes() { return asistentes; }

    public void setTipoRegistro(List<DTTipoRegistro> tipoRegistro) { this.tipoRegistro = tipoRegistro; }
    public void setAsistentes(List<DTAsistente> asistentes) { this.asistentes = asistentes; }
}
