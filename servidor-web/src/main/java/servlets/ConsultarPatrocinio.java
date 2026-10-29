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
import logica.DTInstitucion;
import logica.DTPatrocinio;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Consulta de Patrocinio (CU 16).
 *
 *   /patrocinio?edicion=JIAP%202026                    los patrocinios de la edicion
 *   /patrocinio?edicion=JIAP%202026&institucion=UTEC   el detalle de uno
 *
 * Una institucion tiene a lo sumo un patrocinio por edicion, asi que la
 * edicion y la institucion lo identifican; el codigo no puede ir en la
 * direccion porque es justamente lo que no se publica.
 *
 * Que se ve lo decide la logica con el solicitante: una edicion no Aceptada
 * solo la ve su organizador, y el codigo le llega solo a el. Este servlet no
 * filtra nada: si el codigo viene, es porque le corresponde verlo.
 */
@WebServlet("/patrocinio")
public class ConsultarPatrocinio extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Los parametros y quien pregunta (null si es un visitante).
        String nombreEdicion = texto(request, "edicion");
        String nombreInstitucion = texto(request, "institucion");
        String solicitante = SesionWeb.solicitante(request);

        try {
            if (nombreInstitucion.isEmpty()) {
                // 2a. Sin institucion: el listado de la edicion, por nombre.
                List<DTPatrocinio> patrocinios = new ArrayList<>(
                        controlador.listarPatrociniosDeEdicion(nombreEdicion, solicitante));
                patrocinios.sort(Comparator.comparing(DTPatrocinio::getInstitucion,
                        String.CASE_INSENSITIVE_ORDER));
                request.setAttribute("patrocinios", patrocinios);

            } else {
                // 2b. Con institucion: el detalle de su patrocinio.
                DTPatrocinio patrocinio = controlador.consultarPatrocinio(
                        nombreEdicion, nombreInstitucion, solicitante);
                request.setAttribute("patrocinio", patrocinio);
                request.setAttribute("institucion", institucion(patrocinio.getInstitucion()));
            }

            // 3. Todo al request y a la vista.
            request.setAttribute("edicion", nombreEdicion);
            Paginas.forward(request, response, Paginas.PATROCINIO);

        } catch (ReglaNegocioException e) {
            // Falta un dato, no existe, o la edicion no esta publicada: 404 con el motivo.
            Paginas.noEncontrada(request, response, e.getMessage());
        }
    }

    /**
     * La institucion, para mostrar su imagen si tiene. Si por algo no se
     * encuentra, la pagina se muestra igual, sin imagen.
     */
    private DTInstitucion institucion(String nombre) {
        try {
            return controlador.consultarInstitucion(nombre);
        } catch (ReglaNegocioException sinDatos) {
            return null;
        }
    }

    private static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }
}
