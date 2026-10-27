package servlets;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logica.DTImagen;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

@WebServlet("/imagen")
public class VerImagen extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String tipoEntidad = request.getParameter("tipoEntidad");
        String clave = request.getParameter("clave");

        if (tipoEntidad == null
                || clave == null
                || clave.isBlank()
                || !Set.of(
                "usuario",
                "evento",
                "edicion",
                "institucion"
        ).contains(tipoEntidad)) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        DTImagen imagen;
        try {
            imagen = buscarImagen(tipoEntidad, clave);
        } catch (ReglaNegocioException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (imagen == null
                || imagen.getContenido() == null
                || imagen.getContenido().length == 0) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(imagen.getTipo());
        response.setContentLength(imagen.getContenido().length);
        response.getOutputStream().write(imagen.getContenido());
    }

    private DTImagen buscarImagen(String tipoEntidad, String clave
    ) throws ReglaNegocioException {
        return switch (tipoEntidad) {
            case "usuario" -> controlador.obtenerImagenUsuario(clave);
            case "evento" -> controlador.obtenerImagenEvento(clave);
            case "edicion" -> controlador.obtenerImagenEdicion(clave);
            case "institucion" -> controlador.obtenerImagenInstitucion(clave);
            default -> throw new ReglaNegocioException("Tipo de entidad invalido.");
        };
    }
}
