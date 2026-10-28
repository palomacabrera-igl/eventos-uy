package servlets;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTFecha;
import logica.DTImagen;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Alta de Edicion de Evento (CU 8), solo para organizadores con sesion.
 *
 *   GET  /edicion/alta            el formulario
 *   GET  /edicion/alta?evento=X   el formulario con el evento ya elegido
 *   POST /edicion/alta            crea la edicion
 *
 * La edicion nace Ingresada, su organizador es quien la da de alta y la
 * fecha de alta la pone la logica. Todo eso lo decide altaEdicion(), no este
 * servlet: aca solo se leen los campos y se elige que pagina mostrar.
 */
@WebServlet("/edicion/alta")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class AltaEdicion extends HttpServlet {

    private static final String[] CAMPOS = {"evento", "nombre", "sigla", "ciudad", "pais", "ini", "fin"};

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!esOrganizador(request)) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }
        // Si se llego desde la pagina de un evento, ese evento ya viene elegido.
        request.setAttribute("evento", texto(request, "evento"));
        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!esOrganizador(request)) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }

        try {
            // Fuerza la lectura del formulario multipart: si la imagen pasa el
            // limite, salta aca y se avisa con un mensaje en vez de un error 500.
            request.getParts();

            String nombre = texto(request, "nombre");
            DTEdicionEvento datos = new DTEdicionEvento(nombre, texto(request, "sigla"),
                    texto(request, "ciudad"), texto(request, "pais"),
                    fecha(request, "ini", "inicio"), fecha(request, "fin", "fin"), null);

            controlador.altaEdicion(SesionWeb.solicitante(request), texto(request, "evento"),
                    datos, imagen(request));

            // Post/Redirect/Get: despues de guardar se cambia la direccion, asi
            // F5 no vuelve a enviar el formulario ni duplica la edicion.
            Paginas.redirect(request, response, "/edicion?nombre=" + Texto.paraUrl(nombre) + "&creada=1");

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
        // Los eventos para el desplegable: la JSP no los busca, se los dejamos.
        List<String> eventos = new ArrayList<>();
        for (DTEvento e : controlador.listarEventos()) {
            eventos.add(e.getNombre());
        }
        eventos.sort(String.CASE_INSENSITIVE_ORDER);
        request.setAttribute("eventos", eventos);
        Paginas.forward(request, response, Paginas.ALTA_EDICION);
    }

    private static boolean esOrganizador(HttpServletRequest request) {
        DTUsuario usuario = SesionWeb.actual(request);
        return usuario != null && usuario.esOrganizador();
    }

    private static String texto(HttpServletRequest request, String campo) {
        String valor = request.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }

    /**
     * Pasa el valor de un campo de fecha (aaaa-mm-dd, como lo manda el
     * navegador) a DTFecha. Vacio queda null: que sea obligatoria lo controla
     * la logica, igual que si la peticion no pasara por el navegador.
     */
    private static DTFecha fecha(HttpServletRequest request, String campo, String nombre)
            throws ReglaNegocioException {
        String valor = texto(request, campo);
        if (valor.isEmpty()) {
            return null;
        }
        try {
            return DTFecha.desde(LocalDate.parse(valor));
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("La fecha de " + nombre + " no es valida.");
        }
    }

    private static void conservarCampos(HttpServletRequest request) {
        for (String campo : CAMPOS) {
            request.setAttribute(campo, texto(request, campo));
        }
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
            throw new ReglaNegocioException("Solo se permiten imagenes JPEG, PNG o WebP.");
        }
        try (var entrada = parte.getInputStream()) {
            return new DTImagen(entrada.readAllBytes(), tipo);
        }
    }
}
