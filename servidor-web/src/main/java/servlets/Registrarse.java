package servlets;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import logica.DTAsistente;
import logica.DTFecha;
import logica.DTImagen;
import logica.DTOrganizador;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;
import logica.TipoUsuario;

/**
 * Alta de Usuario (CU 3) desde el sitio.
 *
 * El formulario lleva un archivo, asi que viaja como multipart/form-data y el
 * servlet necesita @MultipartConfig para poder leerlo: sin eso getPart() no
 * funciona y getParameter() devuelve null hasta para los campos de texto.
 */
@WebServlet("/registrarse")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class Registrarse extends HttpServlet {

    private static final String TITULO = "Alta de usuario";

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SesionWeb.actual(request) != null) {
            Paginas.redirect(request, response, "/eventos");
            return;
        }
        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Tomcat arma las partes del multipart la primera vez que se pide un
        // parametro, y si el archivo pasa el maximo tira una excepcion ahi
        // mismo. Se la provoca aca a proposito para poder avisar con un
        // mensaje en vez de que salga la pagina de error 500.
        try {
            request.getParts();
        } catch (IllegalStateException grande) {
            request.setAttribute("error",
                    "La imagen es demasiado grande. El maximo son 5 MB.");
            mostrarFormulario(request, response);
            return;
        }

        String tipoElegido = texto(request, "tipo");
        boolean organizador = "organizador".equals(tipoElegido);

        try {
            DTUsuario datos;
            if (organizador) {
                datos = new DTOrganizador(
                        texto(request, "nick"),
                        texto(request, "nombre"),
                        texto(request, "correo"),
                        texto(request, "desc"),
                        texto(request, "web"));
            } else {
                datos = new DTAsistente(
                        texto(request, "nick"),
                        texto(request, "nombre"),
                        texto(request, "correo"),
                        texto(request, "apellido"),
                        fecha(request.getParameter("fnac")),
                        null);
            }

            controlador.altaUsuario(
                    datos,
                    organizador ? TipoUsuario.ORGANIZADOR : TipoUsuario.ASISTENTE,
                    request.getParameter("pass"),
                    request.getParameter("pass2"),
                    organizador ? null : texto(request, "inst"),
                    imagen(request));

            // La cuenta quedo creada: se inicia la sesion sola, asi la persona
            // no tiene que escribir de nuevo lo que acaba de escribir.
            // El redirect es por lo mismo que en el login: que F5 no repita el alta.
            SesionWeb.iniciar(request, controlador.iniciarSesion(
                    texto(request, "nick"), request.getParameter("pass")));
            Paginas.redirect(request, response, "/eventos");

        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            // Se devuelve todo lo escrito menos las dos contrasenas, que se
            // vuelven a pedir siempre.
            request.setAttribute("tipo", tipoElegido);
            for (String campo : new String[]{"nick", "nombre", "correo",
                    "apellido", "fnac", "inst", "desc", "web"}) {
                request.setAttribute(campo, texto(request, campo));
            }
            mostrarFormulario(request, response);
        }
    }

    /** El formulario, con la lista de instituciones para el desplegable. */
    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<String> instituciones = new ArrayList<>(controlador.listarNombresInstituciones());
        instituciones.sort(String.CASE_INSENSITIVE_ORDER);
        request.setAttribute("instituciones", instituciones);
        request.setAttribute("titulo", TITULO);
        Paginas.forward(request, response, Paginas.REGISTRO);
    }

    /** Un parametro de texto, nunca null, sin los espacios de los costados. */
    private static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

    /**
     * La fecha que manda <input type="date">, que siempre viene aaaa-mm-dd.
     *
     * Que una fecha tenga forma de fecha es formato, no una regla de negocio:
     * por eso se valida aca y no en la logica.
     */
    private static DTFecha fecha(String valor) throws ReglaNegocioException {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return DTFecha.desde(LocalDate.parse(valor));
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("La fecha de nacimiento no es valida.");
        }
    }

    /** La imagen del formulario, o null si no eligieron ninguna. */
    private static DTImagen imagen(HttpServletRequest request)
            throws ServletException, IOException {

        Part parte = request.getPart("imagen");
        if (parte == null || parte.getSize() == 0) {
            return null;
        }
        try (var entrada = parte.getInputStream()) {
            return new DTImagen(entrada.readAllBytes(), parte.getContentType());
        }
    }
}
