package servlets;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logica.Evento;
import logica.Imagen;
import logica.Institucion;
import logica.ManejadorEvento;
import logica.ManejadorInstitucion;
import logica.ManejadorUsuario;
import logica.Usuario;

@WebServlet("/imagen")
public class VerImagen extends HttpServlet {

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

        Imagen imagen = buscarImagen(tipoEntidad, clave);

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

    private Imagen buscarImagen(
            String tipoEntidad,
            String clave
    ) {
        return switch (tipoEntidad) {
            case "usuario" -> {
                Usuario usuario = ManejadorUsuario.getInstancia()
                        .buscar(clave);

                yield usuario == null ? null : usuario.getImagen();
            }

            case "evento" -> {
                Evento evento = ManejadorEvento.getInstancia()
                        .buscar(clave);

                yield evento == null ? null : evento.getImagen();
            }

            case "edicion" -> {
                var edicion = ManejadorEvento.getInstancia()
                        .buscarEdicion(clave);

                yield edicion == null ? null : edicion.getImagen();
            }

            case "institucion" -> {
                Institucion institucion =
                        ManejadorInstitucion.getInstancia()
                                .buscar(clave);

                yield institucion == null
                        ? null
                        : institucion.getImagen();
            }

            default -> null;
        };
    }
}