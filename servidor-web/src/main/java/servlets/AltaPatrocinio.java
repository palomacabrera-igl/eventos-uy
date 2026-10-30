package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logica.DTEdicionEvento;
import logica.DTFecha;
import logica.DTPatrocinio;
import logica.DTTipoRegistro;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Alta de Patrocinio (CU 15).
 *
 * La letra lo describe en tres pasos, y la direccion dice en cual se esta:
 *
 *   /patrocinio/alta                       elegir evento
 *   /patrocinio/alta?evento=JIAP           elegir una edicion de ese evento
 *   /patrocinio/alta?edicion=JIAP%202026   cargar los datos
 *
 * Que cada paso tenga su propia direccion permite volver atras con el boton
 * del navegador y compartir el enlace de un paso.
 */
@WebServlet("/patrocinio/alta")
public class AltaPatrocinio extends HttpServlet {

    private static final String TITULO = "Alta de patrocinio";

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = exigirOrganizador(request, response);
        if (sesion == null) {
            return;
        }
        mostrarPaso(request, response, sesion);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = exigirOrganizador(request, response);
        if (sesion == null) {
            return;
        }

        String nombreEdicion = texto(request, "edicion");

        try {
            DTPatrocinio datos = new DTPatrocinio(
                    texto(request, "codigo"),
                    // La fecha la pone la logica con el dia de hoy; se manda
                    // null para dejar claro que el formulario no la decide.
                    (DTFecha) null,
                    numero(request, "monto", "El aporte economico"),
                    texto(request, "nivel"),
                    (int) numero(request, "gratis", "La cantidad de registros gratuitos"),
                    texto(request, "institucion"),
                    texto(request, "tipo"));

            controlador.altaPatrocinio(sesion.getNickname(), nombreEdicion, datos);

            Paginas.redirect(request, response,
                    "/patrocinio/alta?creado=" + Texto.paraUrl(nombreEdicion));

        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            for (String campo : new String[]{"codigo", "monto", "nivel", "gratis",
                    "institucion", "tipo"}) {
                request.setAttribute(campo, texto(request, campo));
            }
            mostrarPaso(request, response, sesion);
        }
    }

    /** Arma el paso que corresponde, segun lo que diga la direccion. */
    private void mostrarPaso(HttpServletRequest request, HttpServletResponse response,
                             DTUsuario sesion) throws ServletException, IOException {

        String nickname = sesion.getNickname();
        String nombreEdicion = texto(request, "edicion");
        String nombreEvento = texto(request, "evento");

        try {
            // Las ediciones del organizador, en los tres estados: la letra
            // permite patrocinar una edicion que todavia no fue aceptada.
            List<DTEdicionEvento> suyas =
                    new ArrayList<>(controlador.listarEdicionesDeOrganizador(nickname, nickname));
            suyas.sort(Comparator.comparing(DTEdicionEvento::getNombre,
                    String.CASE_INSENSITIVE_ORDER));

            if (!nombreEdicion.isEmpty()) {
                // Paso 3. La edicion elegida tiene que ser suya. La logica lo
                // rechazaria igual, pero asi no se muestra un formulario que
                // no se va a poder enviar.
                DTEdicionEvento elegida = buscar(suyas, nombreEdicion);
                if (elegida == null) {
                    Paginas.noEncontrada(request, response,
                            "No organizas ninguna edicion con ese nombre.");
                    return;
                }
                request.setAttribute("edicion", elegida);
                request.setAttribute("tipos", ordenados(nombreEdicion));
                request.setAttribute("instituciones", instituciones());
                request.setAttribute("paso", "datos");

            } else if (!nombreEvento.isEmpty()) {
                // Paso 2: las ediciones de ESE evento que organiza.
                List<DTEdicionEvento> delEvento = new ArrayList<>();
                for (DTEdicionEvento ed : suyas) {
                    if (nombreEvento.equals(ed.getNombreEvento())) {
                        delEvento.add(ed);
                    }
                }
                request.setAttribute("evento", nombreEvento);
                request.setAttribute("ediciones", delEvento);
                request.setAttribute("paso", "edicion");

            } else {
                // Paso 1: los eventos donde tiene alguna edicion. Listar los
                // demas seria ofrecer pantallas sin salida.
                request.setAttribute("eventos", eventosDe(suyas));
                request.setAttribute("paso", "evento");
            }

            String creada = texto(request, "creado");
            if (!creada.isEmpty()) {
                request.setAttribute("aviso",
                        "El patrocinio de la edición " + creada + " quedó registrado.");
            }

            request.setAttribute("titulo", TITULO);
            Paginas.forward(request, response, Paginas.PATROCINIO_ALTA);

        } catch (ReglaNegocioException e) {
            Paginas.noEncontrada(request, response, e.getMessage());
        }
    }

    /** Solo un organizador con sesion llega a este caso de uso. */
    private DTUsuario exigirOrganizador(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = SesionWeb.actual(request);
        if (sesion == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return null;
        }
        if (!sesion.esOrganizador()) {
            Paginas.prohibida(request, response,
                    "El alta de patrocinio es para organizadores.");
            return null;
        }
        return sesion;
    }

    private static DTEdicionEvento buscar(List<DTEdicionEvento> ediciones, String nombre) {
        for (DTEdicionEvento ed : ediciones) {
            if (ed.getNombre().equals(nombre)) {
                return ed;
            }
        }
        return null;
    }

    /** Los nombres de evento distintos, en orden y sin repetir. */
    private static List<String> eventosDe(List<DTEdicionEvento> ediciones) {
        Set<String> nombres = new LinkedHashSet<>();
        for (DTEdicionEvento ed : ediciones) {
            if (ed.getNombreEvento() != null) {
                nombres.add(ed.getNombreEvento());
            }
        }
        List<String> resultado = new ArrayList<>(nombres);
        resultado.sort(String.CASE_INSENSITIVE_ORDER);
        return resultado;
    }

    private List<DTTipoRegistro> ordenados(String nombreEdicion) throws ReglaNegocioException {
        List<DTTipoRegistro> tipos =
                new ArrayList<>(controlador.listarTiposDeRegistro(nombreEdicion));
        tipos.sort(Comparator.comparing(DTTipoRegistro::getNombre, String.CASE_INSENSITIVE_ORDER));
        return tipos;
    }

    private List<String> instituciones() {
        List<String> nombres = new ArrayList<>(controlador.listarNombresInstituciones());
        nombres.sort(String.CASE_INSENSITIVE_ORDER);
        return nombres;
    }

    private static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    /**
     * Un numero del formulario. Que sea un numero es formato, no una regla de
     * negocio: por eso se valida aca. Que sea mayor que cero lo decide la
     * logica.
     */
    private static double numero(HttpServletRequest request, String campo, String comoSeLlama)
            throws ReglaNegocioException {

        String valor = texto(request, campo);
        if (valor.isEmpty()) {
            throw new ReglaNegocioException(comoSeLlama + " es obligatorio.");
        }
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException noEsNumero) {
            throw new ReglaNegocioException(comoSeLlama + " tiene que ser un numero.");
        }
    }
}
