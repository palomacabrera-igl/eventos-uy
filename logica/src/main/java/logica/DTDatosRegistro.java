package logica;

import java.util.Set;

/**
 * <<DataType>> DTDatosRegistro del DSS de Registro a Edicion de Evento.
 * Empaqueta, para una edicion, sus tipos de registro y todos los asistentes
 * existentes, de modo que la GUI llene los combos de "Asistentes" y
 * "Tipos de Registro" con una sola llamada (listarDatosRegistro()).
 */
public class DTDatosRegistro {

    private Set<DTTipoRegistro> tipoRegistro;
    private Set<DTAsistente> asistentes;

    public DTDatosRegistro(Set<DTTipoRegistro> tipoRegistro, Set<DTAsistente> asistentes) {
        this.tipoRegistro = tipoRegistro;
        this.asistentes = asistentes;
    }

    public Set<DTTipoRegistro> getTipoRegistro() { return tipoRegistro; }
    public Set<DTAsistente> getAsistentes() { return asistentes; }
}
