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
import logica.DTEvento;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Consulta de Evento (CU 7): los datos de un evento y sus ediciones publicas.
 *
 *   /evento?nombre=Maratón%20de%20Montevideo
 *
 * El evento se identifica por su nombre y no por un id (decision D5). Lo puede
 * ver cualquiera, incluso un visitante, y solo muestra las ediciones Aceptada:
 * esa regla la aplica la logica, no esta clase ni la JSP.
 */
@WebServlet("/evento")
public class ConsultarEvento extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. El parametro que identifica al evento.
        String nombre = request.getParameter("nombre");

        try {
            // 2 y 3. La logica busca el evento y filtra sus ediciones.
            DTEvento evento = controlador.consultarEvento(nombre);
            List<DTEdicionEvento> ediciones =
                    new ArrayList<>(controlador.listarEdicionesAceptadas(nombre));

            // Orden de presentacion: de la edicion mas reciente a la mas vieja.
            ediciones.sort(Comparator.comparing(
                    (DTEdicionEvento ed) -> ed.getFechaInicio().aLocalDate()).reversed());

            // 4. Todo al request.
            request.setAttribute("evento", evento);
            request.setAttribute("ediciones", ediciones);

            // 5 y 6. La vista.
            Paginas.forward(request, response, Paginas.EVENTO);

        } catch (ReglaNegocioException e) {
            // Falta el nombre o el evento no existe: la pagina 404 con el motivo.
            Paginas.noEncontrada(request, response, e.getMessage());
        }
    }
}
