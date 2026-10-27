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
import logica.DTEdicionEvento;
import logica.DTRegistro;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Consulta de Usuario (CU 4).
 *
 *   /perfil?nickname=vale23   el perfil de esa persona
 *   /perfil                   el propio, si hay sesion iniciada
 *
 * El servlet no decide que se puede ver y que no: le pasa a la logica quien
 * esta preguntando y la logica devuelve lo que corresponde. Si aca hubiera un
 * "if" eligiendo que esconder, seria una regla de negocio metida en el
 * controlador, que es justo lo que la letra no quiere.
 */
@WebServlet("/perfil")
public class Perfil extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String solicitante = SesionWeb.solicitante(request);

        // Sin nickname en la direccion se muestra el propio, que es lo que
        // hace falta para el boton "Mi perfil" del cabezal.
        String nickname = request.getParameter("nickname");
        if (nickname == null || nickname.isBlank()) {
            nickname = solicitante;
        }
        if (nickname == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }

        try {
            DTUsuario perfil = controlador.consultarUsuario(nickname, solicitante);

            request.setAttribute("perfil", perfil);
            request.setAttribute("esPropio", perfil.getNickname().equals(solicitante));

            if (perfil.esOrganizador()) {
                List<DTEdicionEvento> ediciones =
                        new ArrayList<>(controlador.listarEdicionesDeOrganizador(nickname, solicitante));
                ediciones.sort(Comparator.comparing(DTEdicionEvento::getNombre,
                        String.CASE_INSENSITIVE_ORDER));
                request.setAttribute("ediciones", ediciones);
            } else {
                List<DTRegistro> registros =
                        new ArrayList<>(controlador.listarRegistrosDeAsistente(nickname, solicitante));
                registros.sort(Comparator.comparing(DTRegistro::getNombreEdicion,
                        String.CASE_INSENSITIVE_ORDER));
                request.setAttribute("registros", registros);
            }

            // Vuelve de haber guardado los cambios: el aviso no puede viajar
            // en el request porque hubo un redirect en el medio, asi que
            // viaja como marca en la direccion.
            if (request.getParameter("guardado") != null) {
                request.setAttribute("aviso", "Tus datos quedaron guardados.");
            }

            request.setAttribute("titulo", perfil.getNombre());
            Paginas.forward(request, response, Paginas.PERFIL);

        } catch (ReglaNegocioException noExiste) {
            Paginas.noEncontrada(request, response, noExiste.getMessage());
        }
    }
}
