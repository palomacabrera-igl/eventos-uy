package servlets;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import logica.DTImagen;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/** Alta de Institucion (CU 14), exclusiva para organizadores autenticados. */
@WebServlet("/institucion/alta")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class AltaInstitucion extends HttpServlet {

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
            // Fuerza la lectura del multipart para detectar un archivo que
            // excede el limite y mostrar un mensaje entendible.
            request.getParts();
            String nombre = texto(request, "nombre");
            controlador.altaInstitucion(SesionWeb.solicitante(request), nombre,
                    texto(request, "descripcion"), texto(request, "sitioWeb"), imagen(request));
            Paginas.redirect(request, response, "/institucion/alta?creada="
                    + Texto.paraUrl(nombre));
        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            conservarCampos(request);
            mostrarFormulario(request, response);
        } catch (IllegalStateException error) {
            request.setAttribute("error", "La imagen es demasiado grande. El maximo son 5 MB.");
            conservarCampos(request);
            mostrarFormulario(request, response);
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Paginas.forward(request, response, Paginas.ALTA_INSTITUCION);
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
            Paginas.prohibida(request, response, "El alta de institución es para organizadores.");
            return false;
        }
        return true;
    }

    private static String texto(HttpServletRequest request, String campo) {
        String valor = request.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }

    private static void conservarCampos(HttpServletRequest request) {
        for (String campo : new String[]{"nombre", "descripcion", "sitioWeb"}) {
            request.setAttribute(campo, texto(request, campo));
        }
    }

    private static DTImagen imagen(HttpServletRequest request)
            throws IOException, ServletException, ReglaNegocioException {
        Part parte = request.getPart("imagen");
        if (parte == null || parte.getSize() == 0) {
            return null;
        }
        String tipo = parte.getContentType();
        if (!Set.of("image/jpeg", "image/png", "image/webp").contains(tipo)) {
            throw new ReglaNegocioException("Solo se permiten imagenes JPEG, PNG o WebP.");
        }
        try (var entrada = parte.getInputStream()) {
            return new DTImagen(entrada.readAllBytes(), tipo);
        }
    }
}
