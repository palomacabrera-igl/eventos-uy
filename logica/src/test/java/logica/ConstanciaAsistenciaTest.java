package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Constancia de asistencia en PDF (letra, 6.2 y 7.5) sin base de datos.
 *
 * <pre>
 *  ConstanciaAsistencia.generar(datos)
 *  CV  un PDF valido, de una pagina, con nombre y apellido, evento, edicion,
 *      ciudad, fechas y fecha de emision
 *  CV  una edicion de un solo dia dice "el ..." y no "del ... al ..."
 *  CV  los acentos y la enie se leen bien
 *  Valor limite: datos en null, caracteres que la fuente no tiene, textos largos
 *
 *  EdicionEvento.obtenerConstancia(nickname, hoy)
 *  CV  el asistente registrado que asistio              -&gt; el PDF
 *  CI  registrado pero sin asistencia                   -&gt; ReglaNegocioException
 *  CI  no registrado, el organizador, visitante         -&gt; ReglaNegocioException
 * </pre>
 */
@DisplayName("Constancia de asistencia en PDF")
class ConstanciaAsistenciaTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 3, 1);
    private static final LocalDate FIN = LocalDate.of(2027, 3, 5);
    private static final LocalDate EMISION = LocalDate.of(2027, 3, 6);

    private EdicionEvento edicion;
    private TipoRegistro general;

    @BeforeEach
    void armarLaEdicion() throws ReglaNegocioException {
        Organizador duenio = new Organizador("imm", "Intendencia", "eventos@imm.gub.uy",
                "Organiza eventos", null, "Imm2026!");
        Evento evento = new Evento("JIAP", "Jornadas de ingenieria", LocalDate.of(2020, 1, 1),
                "JIAP", List.of());
        edicion = new EdicionEvento("JIAP 2027", "JIAP", INICIO, FIN,
                LocalDate.of(2026, 10, 27), "Montevideo", "Uruguay", duenio);
        evento.agregarEdicion(edicion);
        general = edicion.crearTipoRegistro("General", "Entrada general", 500.0, 100);
        edicion.aceptar();
    }

    private void registrar(String nickname, String nombre, String apellido) {
        Asistente asistente = new Asistente(nickname, nombre, nickname + "@mail.com",
                apellido, LocalDate.of(2000, 1, 1));
        edicion.altaRegistro(asistente, general, LocalDate.of(2026, 10, 28));
    }

    private static String textoDe(byte[] pdf) throws IOException {
        PdfReader lector = new PdfReader(pdf);
        try {
            StringBuilder todo = new StringBuilder();
            for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
                todo.append(new PdfTextExtractor(lector).getTextFromPage(pagina)).append('\n');
            }
            return todo.toString();
        } finally {
            lector.close();
        }
    }

    private static ConstanciaAsistencia.Datos datos(String nombre, String apellido) {
        return new ConstanciaAsistencia.Datos(nombre, apellido, "JIAP", "JIAP 2027",
                "Montevideo", INICIO, FIN, EMISION);
    }

    // ----- El documento -----

    @Test
    @DisplayName("es un PDF de verdad: empieza con %PDF y termina con %%EOF")
    void esUnPdf() {
        byte[] pdf = ConstanciaAsistencia.generar(datos("Ana", "Perez"));

        String cabecera = new String(pdf, 0, 5, StandardCharsets.ISO_8859_1);
        String cola = new String(pdf, StandardCharsets.ISO_8859_1).trim();
        assertEquals("%PDF-", cabecera);
        assertTrue(cola.endsWith("%%EOF"));
    }

    @Test
    @DisplayName("tiene una sola pagina")
    void unaPagina() throws IOException {
        PdfReader lector = new PdfReader(ConstanciaAsistencia.generar(datos("Ana", "Perez")));
        try {
            assertEquals(1, lector.getNumberOfPages());
        } finally {
            lector.close();
        }
    }

    @Test
    @DisplayName("lleva nombre y apellido, evento, edicion, ciudad, fechas y fecha de emision")
    void llevaTodoLoQuePideLaLetra() throws IOException {
        String texto = textoDe(ConstanciaAsistencia.generar(datos("Ana", "Perez")));

        assertTrue(texto.contains("Constancia de asistencia"), texto);
        assertTrue(texto.contains("Ana Perez"), texto);
        assertTrue(texto.contains("JIAP 2027"), texto);
        assertTrue(texto.contains("Evento: JIAP"), texto);
        assertTrue(texto.contains("Montevideo"), texto);
        assertTrue(texto.contains("del 01/03/2027 al 05/03/2027"), texto);
        assertTrue(texto.contains("06/03/2027"), texto);
    }

    @Test
    @DisplayName("una edicion de un solo dia dice \"el ...\" y no \"del ... al ...\"")
    void edicionDeUnDia() throws IOException {
        LocalDate dia = LocalDate.of(2026, 11, 15);
        String texto = textoDe(ConstanciaAsistencia.generar(new ConstanciaAsistencia.Datos(
                "Ana", "Perez", "Maraton", "Maraton 2026", "Montevideo", dia, dia, EMISION)));

        assertTrue(texto.contains("el 15/11/2026"), texto);
        assertFalse(texto.contains("del 15/11/2026"), texto);
    }

    @Test
    @DisplayName("los acentos y la enie salen bien")
    void acentosYEnie() throws IOException {
        String texto = textoDe(ConstanciaAsistencia.generar(new ConstanciaAsistencia.Datos(
                "María José", "Núñez Peña", "Maratón de Montevideo", "Maratón 2026",
                "Montevideo", INICIO, FIN, EMISION)));

        assertTrue(texto.contains("María José Núñez Peña"), texto);
        assertTrue(texto.contains("Maratón de Montevideo"), texto);
        assertTrue(texto.contains("Maratón 2026"), texto);
        assertTrue(texto.contains("asistió"), texto);
    }

    @Test
    @DisplayName("se genera en el momento: dos pedidos dan dos documentos con su propia fecha de emision")
    void cadaPedidoEsNuevo() throws IOException {
        String uno = textoDe(ConstanciaAsistencia.generar(datos("Ana", "Perez")));
        String otro = textoDe(ConstanciaAsistencia.generar(new ConstanciaAsistencia.Datos(
                "Ana", "Perez", "JIAP", "JIAP 2027", "Montevideo", INICIO, FIN, EMISION.plusDays(30))));

        assertTrue(uno.contains("06/03/2027"));
        assertTrue(otro.contains("05/04/2027"));
        assertNotEquals(uno, otro);
    }

    // ----- Valores limite -----

    @Test
    @DisplayName("valor limite: todos los datos en null igual dan un PDF valido")
    void todoNull() throws IOException {
        byte[] pdf = ConstanciaAsistencia.generar(
                new ConstanciaAsistencia.Datos(null, null, null, null, null, null, null, null));

        assertTrue(textoDe(pdf).contains("Constancia de asistencia"));
    }

    @Test
    @DisplayName("valor limite: un caracter que la fuente no tiene (emoji, chino) no rompe la generacion")
    void caracteresRaros() throws IOException {
        byte[] pdf = ConstanciaAsistencia.generar(datos("Zoë 🎉 中文", "Perez"));

        assertTrue(textoDe(pdf).contains("Perez"));
    }

    @Test
    @DisplayName("valor limite: un nombre de edicion muy largo sigue siendo un PDF de una pagina")
    void textoLargo() throws IOException {
        String largo = "Jornada ".repeat(40).trim();
        byte[] pdf = ConstanciaAsistencia.generar(new ConstanciaAsistencia.Datos(
                "Ana", "Perez", largo, largo, "Montevideo", INICIO, FIN, EMISION));

        PdfReader lector = new PdfReader(pdf);
        try {
            assertEquals(1, lector.getNumberOfPages());
        } finally {
            lector.close();
        }
    }

    // ----- Quien la recibe -----

    @Test
    @DisplayName("el asistente que registro su asistencia recibe su constancia, con su nombre")
    void laRecibeQuienAsistio() throws ReglaNegocioException, IOException {
        registrar("ana", "Ana", "Perez");
        registrar("beto", "Beto", "Gomez");
        edicion.registrarAsistencia("ana", INICIO);

        String texto = textoDe(edicion.obtenerConstancia("ana", EMISION));

        assertTrue(texto.contains("Ana Perez"), texto);
        assertFalse(texto.contains("Beto"), texto);
        assertTrue(texto.contains("JIAP 2027"), texto);
        assertTrue(texto.contains("Evento: JIAP"), texto);
        assertTrue(texto.contains("06/03/2027"), texto);
    }

    @Test
    @DisplayName("registrado pero sin asistencia: no hay constancia, y el mensaje dice por que")
    void sinAsistenciaNo() {
        registrar("ana", "Ana", "Perez");

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> edicion.obtenerConstancia("ana", EMISION));

        assertTrue(error.getMessage().contains("Todavía no registraste tu asistencia"));
    }

    @ParameterizedTest(name = "\"{0}\" no recibe constancia")
    @DisplayName("quien no esta registrado en la edicion no la recibe, ni el organizador")
    @ValueSource(strings = {"carlos", "imm", "ANA"})
    void sinRegistroNo(String nickname) throws ReglaNegocioException {
        registrar("ana", "Ana", "Perez");
        edicion.registrarAsistencia("ana", INICIO);

        assertThrows(ReglaNegocioException.class, () -> edicion.obtenerConstancia(nickname, EMISION));
    }

    @ParameterizedTest(name = "nickname \"{0}\"")
    @DisplayName("valor limite: un visitante o un nickname vacio no la recibe")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void visitanteNo(String nickname) {
        assertThrows(ReglaNegocioException.class, () -> edicion.obtenerConstancia(nickname, EMISION));
    }
}
