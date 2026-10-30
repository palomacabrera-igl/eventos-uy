package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import logica.DTCategoria;
import logica.DTImagen;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Alta de Evento (CU 6), solo para organizadores con sesion.
 *
 *   GET  /evento/alta   el formulario, con una casilla por categoria
 *   POST /evento/alta   crea el evento y lleva a su pagina
 *
 * Las reglas (nombre unico, al menos una categoria que exista, fecha de alta
 * de hoy) las aplica altaEvento() en la logica, no este servlet.
 */
@WebServlet("/evento/alta")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class AltaEvento extends HttpServlet {

    private static final String[] CAMPOS = {"nombre", "descripcion", "sigla"};

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirOrganizador(request, response)) {
            return;
        }
        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirOrganizador(request, response)) {
            return;
        }

        try {
            // Fuerza la lectura del formulario multipart: si la imagen pasa el
            // limite, salta aca y se avisa con un mensaje en vez de un error 500.
            request.getParts();

            String nombre = texto(request, "nombre");
            controlador.altaEvento(SesionWeb.solicitante(request), nombre,
                    texto(request, "descripcion"), texto(request, "sigla"),
                    categoriasElegidas(request), imagen(request));

            // Post/Redirect/Get: F5 en la pagina siguiente no vuelve a crearlo.
            Paginas.redirect(request, response, "/evento?nombre=" + Texto.paraUrl(nombre) + "&creado=1");

        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            conservarCampos(request);
            mostrarFormulario(request, response);
        } catch (IllegalStateException error) {
            request.setAttribute("error", "La imagen es demasiado grande. El máximo son 5 MB.");
            conservarCampos(request);
            mostrarFormulario(request, response);
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Las categorias para las casillas: la JSP no las busca, se las dejamos.
        List<String> categorias = new ArrayList<>();
        for (DTCategoria c : controlador.listarCategorias()) {
            categorias.add(c.getNombre());
        }
        categorias.sort(String.CASE_INSENSITIVE_ORDER);
        request.setAttribute("categoriasDisponibles", categorias);
        Paginas.forward(request, response, Paginas.ALTA_EVENTO);
    }

    /**
     * true cuando la persona puede seguir. Si no, esta respuesta ya quedo
     * contestada: sin sesion se la manda a iniciarla, y con sesion pero sin
     * ser organizador se le dice por que no.
     *
     * Antes los dos casos iban a /iniciar-sesion, y como esa pantalla rebota
     * a la cartelera cuando ya hay sesion, un asistente terminaba en la
     * cartelera sin entender que habia pasado.
     */
    private static boolean exigirOrganizador(HttpServletRequest request,
                                             HttpServletResponse response)
            throws ServletException, IOException {
        DTUsuario usuario = SesionWeb.actual(request);
        if (usuario == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return false;
        }
        if (!usuario.esOrganizador()) {
            Paginas.prohibida(request, response, "El alta de evento es para organizadores.");
            return false;
        }
        return true;
    }

    private static String texto(HttpServletRequest request, String campo) {
        String valor = request.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }

    /** Las casillas marcadas. Si no marcaron ninguna, la logica lo rechaza. */
    private static List<String> categoriasElegidas(HttpServletRequest request) {
        String[] valores = request.getParameterValues("categorias");
        return valores == null ? List.of() : Arrays.asList(valores);
    }

    private static void conservarCampos(HttpServletRequest request) {
        for (String campo : CAMPOS) {
            request.setAttribute(campo, texto(request, campo));
        }
        request.setAttribute("categoriasElegidas", categoriasElegidas(request));
    }

    /** Mismo criterio que el resto de las altas del sitio: JPEG, PNG o WebP, hasta 5 MB. */
    private static DTImagen imagen(HttpServletRequest request)
            throws IOException, ServletException, ReglaNegocioException {
        Part parte = request.getPart("imagen");
        if (parte == null || parte.getSize() == 0) {
            return null;
        }
        String tipo = parte.getContentType();
        if (!Set.of("image/jpeg", "image/png", "image/webp").contains(tipo)) {
            throw new ReglaNegocioException("Solo se permiten imágenes JPEG, PNG o WebP.");
        }
        try (var entrada = parte.getInputStream()) {
            return new DTImagen(entrada.readAllBytes(), tipo);
        }
    }
}
