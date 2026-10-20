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
import logica.Evento;
import logica.Imagen;
import logica.Institucion;
import logica.ManejadorEvento;
import logica.ManejadorInstitucion;
import logica.ManejadorUsuario;
import logica.Usuario;

@WebServlet("/spike-imagen")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class SubirImagen extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/spike-imagen.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String tipoEntidad = request.getParameter("tipoEntidad");
        String clave = request.getParameter("clave");
        Part archivo = request.getPart("imagen");

        if (tipoEntidad == null || clave == null || clave.isBlank()) {
            mostrarError(
                    request,
                    response,
                    "Seleccioná el tipo e ingresá el identificador."
            );
            return;
        }

        if (archivo == null || archivo.getSize() == 0) {
            mostrarError(
                    request,
                    response,
                    "Seleccioná una imagen."
            );
            return;
        }

        String tipoImagen = archivo.getContentType();

        if (!Set.of("image/jpeg", "image/png", "image/webp")
                .contains(tipoImagen)) {
            mostrarError(
                    request,
                    response,
                    "Solo se permiten imágenes JPEG, PNG o WebP."
            );
            return;
        }

        if (archivo.getSize() > 5 * 1024 * 1024) {
            mostrarError(
                    request,
                    response,
                    "La imagen no puede superar los 5 MB."
            );
            return;
        }

        byte[] contenido = archivo.getInputStream().readAllBytes();
        Imagen imagen = new Imagen(contenido, tipoImagen);

        boolean actualizada = asignarImagen(
                tipoEntidad,
                clave.trim(),
                imagen
        );

        if (!actualizada) {
            mostrarError(
                    request,
                    response,
                    "No existe " + tipoEntidad
                            + " con identificador: " + clave
            );
            return;
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/imagen?tipoEntidad=" + tipoEntidad
                        + "&clave=" + java.net.URLEncoder.encode(
                        clave.trim(),
                        java.nio.charset.StandardCharsets.UTF_8
                )
        );
    }

    private boolean asignarImagen(
            String tipoEntidad,
            String clave,
            Imagen imagen
    ) {
        switch (tipoEntidad) {
            case "usuario" -> {
                Usuario usuario = ManejadorUsuario.getInstancia()
                        .buscar(clave);

                if (usuario == null) {
                    return false;
                }

                usuario.asignarImagen(imagen);
                ManejadorUsuario.getInstancia().actualizar(usuario);
                return true;
            }

            case "evento" -> {
                Evento evento = ManejadorEvento.getInstancia()
                        .buscar(clave);

                if (evento == null) {
                    return false;
                }

                evento.asignarImagen(imagen);
                ManejadorEvento.getInstancia().actualizar(evento);
                return true;
            }

            case "edicion" -> {
                var edicion = ManejadorEvento.getInstancia()
                        .buscarEdicion(clave);

                if (edicion == null) {
                    return false;
                }

                edicion.asignarImagen(imagen);

                /*
                 * La edición se actualiza al actualizar el evento al que
                 * pertenece, porque Evento tiene cascade MERGE sobre ediciones.
                 */
                ManejadorEvento.getInstancia()
                        .actualizar(edicion.getEvento());

                return true;
            }

            case "institucion" -> {
                Institucion institucion =
                        ManejadorInstitucion.getInstancia()
                                .buscar(clave);

                if (institucion == null) {
                    return false;
                }

                institucion.asignarImagen(imagen);
                ManejadorInstitucion.getInstancia()
                        .actualizar(institucion);

                return true;
            }

            default -> {
                return false;
            }
        }
    }

    private void mostrarError(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensaje
    ) throws ServletException, IOException {
        request.setAttribute("error", mensaje);

        request.getRequestDispatcher("/WEB-INF/jsp/spike-imagen.jsp")
                .forward(request, response);
    }
}