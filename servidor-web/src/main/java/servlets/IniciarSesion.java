package servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Inicio de Sesion (CU 1).
 *
 * Una misma direccion con dos comportamientos, como en la demo del curso:
 * doGet muestra el formulario y doPost procesa lo que se escribio.
 */
@WebServlet("/iniciar-sesion")
public class IniciarSesion extends HttpServlet {

    private static final String TITULO = "Inicio de sesión";

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    /** Muestra el formulario. Si ya hay sesion, no tiene sentido: a la cartelera. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SesionWeb.actual(request) != null) {
            Paginas.redirect(request, response, "/eventos");
            return;
        }
        request.setAttribute("titulo", TITULO);
        Paginas.forward(request, response, Paginas.LOGIN);
    }

    /** Procesa el formulario. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String identificador = request.getParameter("identificador");
        String password = request.getParameter("password");

        try {
            DTUsuario usuario = controlador.iniciarSesion(identificador, password);

            SesionWeb.iniciar(request, usuario);

            // Salio bien: redirect y no forward. Asi la direccion del navegador
            // cambia y, si la persona recarga con F5, no se reenvia el
            // formulario. Es el patron que usa la demo despues de cada POST.
            Paginas.redirect(request, response, "/eventos");

        } catch (ReglaNegocioException error) {
            // Salio mal: forward, para que la direccion NO cambie y el
            // formulario vuelva con su mensaje y con lo que ya se habia escrito.
            request.setAttribute("error", error.getMessage());
            request.setAttribute("titulo", TITULO);

            // Se devuelve el identificador para no obligar a escribirlo de
            // nuevo. La contrasena NUNCA vuelve a la pagina.
            request.setAttribute("identificador", identificador);

            Paginas.forward(request, response, Paginas.LOGIN);
        }
    }
}
