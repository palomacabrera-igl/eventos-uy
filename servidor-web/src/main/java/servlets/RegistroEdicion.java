package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logica.DTEdicionCompleto;
import logica.DTTipoRegistro;
import logica.DTUsuario;
import logica.EstadoEdicion;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/** Registro del asistente autenticado a una edición aceptada (CU 12). */
@WebServlet("/registro")
public class RegistroEdicion extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (exigirAsistente(request, response) == null) {
            return;
        }
        mostrarFormulario(request, response, texto(request, "edicion"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DTUsuario asistente = exigirAsistente(request, response);
        if (asistente == null) {
            return;
        }

        String nombreEdicion = texto(request, "edicion");
        String nombreTipo = texto(request, "tipo");
        String codigo = texto(request, "codigo");

        try {
            controlador.altaRegistro(asistente.getNickname(), nombreEdicion, nombreTipo,
                    codigo.isEmpty() ? null : codigo);
            Paginas.redirect(request, response, "/registro?edicion="
                    + Texto.paraUrl(nombreEdicion) + "&creado=1");
        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            request.setAttribute("tipoSeleccionado", nombreTipo);
            request.setAttribute("codigo", codigo);
            mostrarFormulario(request, response, nombreEdicion);
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   String nombreEdicion)
            throws ServletException, IOException {
        try {
            DTEdicionCompleto edicion =
                    controlador.consultarEdicion(nombreEdicion, SesionWeb.solicitante(request));
            if (edicion.getEstado() != EstadoEdicion.ACEPTADA) {
                Paginas.noEncontrada(request, response,
                        "Solo se puede realizar el registro a una edición ACEPTADA.");
                return;
            }

            List<DTTipoRegistro> tipos = new ArrayList<>(edicion.getTiposRegistro());
            tipos.sort(Comparator.comparing(DTTipoRegistro::getNombre,
                    String.CASE_INSENSITIVE_ORDER));
            request.setAttribute("edicion", edicion);
            request.setAttribute("tipos", tipos);
            if ("1".equals(request.getParameter("creado"))) {
                request.setAttribute("registroCreado", true);
                request.setAttribute("aviso",
                        "Tu registro a " + edicion.getNombre() + " quedó confirmado.");
            }
            Paginas.forward(request, response, Paginas.REGISTRO_EDICION);
        } catch (ReglaNegocioException error) {
            Paginas.noEncontrada(request, response, error.getMessage());
        }
    }

    private static DTUsuario exigirAsistente(HttpServletRequest request,
                                             HttpServletResponse response)
            throws ServletException, IOException {
        DTUsuario usuario = SesionWeb.actual(request);
        if (usuario == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return null;
        }
        if (!usuario.esAsistente()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("mensaje", "Solo un asistente puede registrarse a una edición.");
            Paginas.forward(request, response, Paginas.NO_ENCONTRADA);
            return null;
        }
        return usuario;
    }

    private static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }
}
