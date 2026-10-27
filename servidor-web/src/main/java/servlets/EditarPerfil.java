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

/**
 * Modificar Datos de Usuario (CU 5).
 *
 * Solo se puede editar el perfil propio, y eso no se decide aca: el servlet
 * le pasa a la logica el nickname de quien tiene la sesion y la logica
 * rechaza cualquier intento de tocar los datos de otra persona.
 */
@WebServlet("/perfil/editar")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class EditarPerfil extends HttpServlet {

    private static final String TITULO = "Editar perfil";

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SesionWeb.actual(request) == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }
        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = SesionWeb.actual(request);
        if (sesion == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }

        // Igual que en el alta: se provoca aca el armado del multipart para
        // poder avisar del tamaño en vez de que salga la pagina de error.
        try {
            request.getParts();
        } catch (IllegalStateException grande) {
            request.setAttribute("error",
                    "La imagen es demasiado grande. El maximo son 5 MB.");
            mostrarFormulario(request, response);
            return;
        }

        String nickname = sesion.getNickname();

        try {
            DTUsuario datos;
            if (sesion.esOrganizador()) {
                datos = new DTOrganizador(nickname, texto(request, "nombre"),
                        sesion.getCorreo(), texto(request, "desc"), texto(request, "web"));
            } else {
                datos = new DTAsistente(nickname, texto(request, "nombre"),
                        sesion.getCorreo(), texto(request, "apellido"),
                        fecha(request.getParameter("fnac")), texto(request, "inst"));
            }

            controlador.modificarUsuario(nickname, datos,
                    request.getParameter("pass"),
                    request.getParameter("pass2"),
                    imagen(request));

            // La sesion guarda el DT de cuando se inicio sesion: si no se
            // reemplaza, el cabezal sigue mostrando el nombre viejo hasta que
            // la persona vuelva a entrar.
            SesionWeb.iniciar(request, controlador.consultarUsuario(nickname, nickname));

            Paginas.redirect(request, response, "/perfil?guardado=1");

        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            // Vuelve lo que se habia escrito, menos las dos contrasenas.
            for (String campo : new String[]{"nombre", "apellido", "fnac",
                    "inst", "desc", "web"}) {
                request.setAttribute(campo, texto(request, campo));
            }
            mostrarFormulario(request, response);
        }
    }

    /** El formulario, con los datos actuales y la lista de instituciones. */
    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = SesionWeb.actual(request);
        try {
            // Se releen los datos en vez de usar los de la sesion: si la
            // persona los cambio desde la Estacion de Trabajo, el formulario
            // tiene que mostrar lo que hay en la base ahora.
            request.setAttribute("perfil",
                    controlador.consultarUsuario(sesion.getNickname(), sesion.getNickname()));

            List<String> instituciones = new ArrayList<>(controlador.listarNombresInstituciones());
            instituciones.sort(String.CASE_INSENSITIVE_ORDER);
            request.setAttribute("instituciones", instituciones);
            request.setAttribute("titulo", TITULO);

            Paginas.forward(request, response, Paginas.PERFIL_EDITAR);

        } catch (ReglaNegocioException noExiste) {
            // El usuario de la sesion ya no esta en la base: la sesion quedo
            // vieja, asi que se cierra y se vuelve a empezar.
            SesionWeb.cerrar(request);
            Paginas.redirect(request, response, "/iniciar-sesion");
        }
    }

    private static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }

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
