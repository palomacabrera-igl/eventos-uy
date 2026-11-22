package logica;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Arma la constancia de asistencia en PDF (letra, 7.5): nombre y apellido del
 * asistente, evento, edicion, ciudad, fechas de la edicion y fecha de emision.
 *
 * Recibe los datos y devuelve los bytes del PDF, sin guardar nada: la constancia
 * se genera en el momento. Quien decide si corresponde emitirla es
 * {@link EdicionEvento#obtenerConstancia}; aca no hay reglas de negocio. El PDF se
 * genera en el Servidor Central y viaja como byte[]; el sitio solo lo descarga.
 * Usa Helvetica, que trae el propio formato PDF: cubre el castellano, y un
 * caracter que no tiene (un emoji) no sale pero tampoco rompe la generacion.
 */
public final class ConstanciaAsistencia {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ConstanciaAsistencia() {
    }

    /**
     * Lo que lleva la constancia. Los textos en null se imprimen vacios, para
     * que un dato que falte no impida emitir el documento.
     */
    public record Datos(String nombre, String apellido, String evento, String edicion,
                        String ciudad, LocalDate inicio, LocalDate fin, LocalDate emision) {
    }

    /** Los bytes de un PDF de una pagina A4 con la constancia. */
    public static byte[] generar(Datos datos) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 64, 64, 80, 64);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Font marca = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, java.awt.Color.GRAY);
            Font texto = FontFactory.getFont(FontFactory.HELVETICA, 13);
            Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);

            Paragraph encabezado = new Paragraph("Constancia de asistencia", titulo);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            documento.add(encabezado);

            Paragraph sitio = new Paragraph("eventos.uy", marca);
            sitio.setAlignment(Element.ALIGN_CENTER);
            sitio.setSpacingAfter(36);
            documento.add(sitio);

            Paragraph introduccion = new Paragraph(
                    "Se deja constancia de que " + texto(datos.nombre()) + " " + texto(datos.apellido())
                            + " asistió a la edición \"" + texto(datos.edicion())
                            + "\" del evento \"" + texto(datos.evento()) + "\".", texto);
            introduccion.setAlignment(Element.ALIGN_JUSTIFIED);
            introduccion.setSpacingAfter(28);
            documento.add(introduccion);

            documento.add(linea("Asistente: ", texto(datos.nombre()) + " " + texto(datos.apellido()),
                    etiqueta, texto));
            documento.add(linea("Evento: ", texto(datos.evento()), etiqueta, texto));
            documento.add(linea("Edición: ", texto(datos.edicion()), etiqueta, texto));
            documento.add(linea("Ciudad: ", texto(datos.ciudad()), etiqueta, texto));
            documento.add(linea("Fechas: ", fechas(datos.inicio(), datos.fin()), etiqueta, texto));

            Paragraph emision = linea("Fecha de emisión: ", fecha(datos.emision()), etiqueta, texto);
            emision.setSpacingBefore(24);
            documento.add(emision);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar la constancia de asistencia.", e);
        } finally {
            // Cerrar vacia lo que falta al flujo; si fallo antes de abrir, no hay nada que cerrar.
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return salida.toByteArray();
    }

    private static Paragraph linea(String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor) {
        Phrase frase = new Phrase();
        frase.add(new Chunk(etiqueta, fuenteEtiqueta));
        frase.add(new Chunk(valor, fuenteValor));
        Paragraph parrafo = new Paragraph(frase);
        parrafo.setSpacingAfter(8);
        return parrafo;
    }

    /** "del 01/03/2027 al 05/03/2027", o "el 15/11/2026" si dura un solo dia. */
    private static String fechas(LocalDate inicio, LocalDate fin) {
        if (inicio != null && inicio.equals(fin)) {
            return "el " + fecha(inicio);
        }
        return "del " + fecha(inicio) + " al " + fecha(fin);
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    private static String texto(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
