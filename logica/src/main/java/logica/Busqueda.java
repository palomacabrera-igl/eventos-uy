package logica;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * La busqueda del cabezal del sitio (letra, 7.8). Sistema le pasa los eventos
 * que lee de la base y esta clase filtra y ordena. Al no tocar la base, se
 * puede probar con entidades armadas en la prueba.
 */
final class Busqueda {

    private static final Locale ESPANOL = Locale.of("es");

    private Busqueda() {}

    /** Eventos no finalizados cuyo nombre o descripcion contienen el texto. */
    static List<DTEvento> eventos(List<Evento> eventos, String texto, OrdenBusqueda orden) {
        String buscado = minusculas(texto);
        List<Evento> encontrados = new ArrayList<>();
        for (Evento e : eventos) {
            if (!e.estaFinalizado()
                    && (contiene(e.getNombre(), buscado) || contiene(e.getDescripcion(), buscado))) {
                encontrados.add(e);
            }
        }
        encontrados.sort(comparador(orden, Evento::getNombre, Evento::getFechaAlta));

        List<DTEvento> resultado = new ArrayList<>();
        for (Evento e : encontrados) {
            resultado.add(e.obtenerDT());
        }
        return resultado;
    }

    /** Ediciones Aceptadas, de eventos no finalizados, cuyo nombre contiene el texto. */
    static List<DTEdicionEvento> ediciones(List<Evento> eventos, String texto, OrdenBusqueda orden) {
        String buscado = minusculas(texto);
        List<EdicionEvento> encontradas = new ArrayList<>();
        for (Evento e : eventos) {
            if (e.estaFinalizado()) {
                continue;
            }
            for (EdicionEvento ed : e.getEdiciones()) {
                if (ed.estaAceptada() && contiene(ed.getNombre(), buscado)) {
                    encontradas.add(ed);
                }
            }
        }
        encontradas.sort(comparador(orden, EdicionEvento::getNombre, EdicionEvento::getfechaAlta));

        List<DTEdicionEvento> resultado = new ArrayList<>();
        for (EdicionEvento ed : encontradas) {
            resultado.add(ed.obtenerDT());
        }
        return resultado;
    }

    private static String minusculas(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(ESPANOL);
    }

    /** Con el texto vacio coincide todo: la letra pide listar todos. */
    private static boolean contiene(String campo, String buscado) {
        return buscado.isEmpty()
                || (campo != null && campo.toLowerCase(ESPANOL).contains(buscado));
    }

    /**
     * El orden pedido. Por fecha de alta van primero los mas nuevos y, si dos
     * tienen la misma fecha, por nombre. Los nombres se comparan con un
     * Collator en espanol, para que "Ñ" y las tildes queden donde corresponde.
     */
    private static <T> Comparator<T> comparador(OrdenBusqueda orden, Function<T, String> nombre,
                                                Function<T, LocalDate> fechaAlta) {
        Comparator<T> porNombre = Comparator.comparing(nombre, Collator.getInstance(ESPANOL));
        if (orden == OrdenBusqueda.ALFABETICO_ASC) {
            return porNombre;
        }
        if (orden == OrdenBusqueda.ALFABETICO_DESC) {
            return porNombre.reversed();
        }
        return Comparator.comparing(fechaAlta, Comparator.<LocalDate>reverseOrder())
                .thenComparing(porNombre);
    }
}
