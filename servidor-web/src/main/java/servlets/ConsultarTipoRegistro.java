package servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logica.DTTipoRegistro;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Consulta de Tipo de Registro (CU 11).
 *
 * La URL identifica al tipo con la clave compuesta que usa el modelo:
 * /tipo-registro?edicion=Nombre+edicion&nombre=General.
 */
@WebServlet("/tipo-registro")
public class ConsultarTipoRegistro extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombreEdicion = request.getParameter("edicion");
        String nombreTipo = request.getParameter("nombre");

        try {
            DTTipoRegistro tipo = controlador.consultarTipoRegistro(nombreEdicion, nombreTipo);
            request.setAttribute("tipo", tipo);
            request.setAttribute("edicion", nombreEdicion);
            Paginas.forward(request, response, Paginas.CONSULTA_TIPO_REGISTRO);
        } catch (ReglaNegocioException error) {
            Paginas.noEncontrada(request, response, error.getMessage());
        }
    }
}
