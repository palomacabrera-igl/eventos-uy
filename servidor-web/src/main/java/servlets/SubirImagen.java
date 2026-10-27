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
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

@WebServlet("/spike-imagen")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class SubirImagen extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

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
        try {
            asignarImagen(tipoEntidad, clave.trim(), new DTImagen(contenido, tipoImagen));
        } catch (ReglaNegocioException e) {
            mostrarError(
                    request,
                    response,
                    e.getMessage()
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

    private void asignarImagen(
            String tipoEntidad,
            String clave,
            DTImagen imagen
    ) throws ReglaNegocioException {
        switch (tipoEntidad) {
            case "usuario" -> controlador.asignarImagenUsuario(clave, imagen);
            case "evento" -> controlador.asignarImagenEvento(clave, imagen);
            case "edicion" -> controlador.asignarImagenEdicion(clave, imagen);
            case "institucion" -> controlador.asignarImagenInstitucion(clave, imagen);
            default -> throw new ReglaNegocioException("Tipo de entidad invalido.");
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
