package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

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
 * Consulta de Registro (CU 13). Solo con sesion iniciada.
 *
 *   /registro                                   asistente: sus registros
 *                                               organizador: sus ediciones
 *   /registro?edicion=JIAP%202026               los registros de esa edicion
 *                                               (solo su organizador)
 *   /registro?edicion=JIAP%202026&asistente=x   el detalle de un registro
 *                                               (el asistente o el organizador)
 *
 * Un asistente tiene a lo sumo un registro por edicion, asi que la edicion y
 * el asistente lo identifican.
 *
 * Quien puede ver que lo decide la logica con el solicitante. Este servlet
 * no filtra nada: si la logica dice que no, se muestra su mensaje.
 */
@WebServlet("/registro")
public class ConsultarRegistro extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Los registros son privados: sin sesion, primero iniciar sesion.
        DTUsuario sesion = SesionWeb.actual(request);
        if (sesion == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }
        String yo = sesion.getNickname();
        String nombreEdicion = texto(request, "edicion");
        String asistente = texto(request, "asistente");

        // Un asistente solo puede ver el suyo: si no dice de quien, es el propio.
        if (!nombreEdicion.isEmpty() && asistente.isEmpty() && sesion.esAsistente()) {
            asistente = yo;
        }

        try {
            if (nombreEdicion.isEmpty()) {
                // 2a. Sin edicion: el punto de partida segun quien es.
                if (sesion.esAsistente()) {
                    request.setAttribute("registros",
                            ordenados(controlador.listarRegistrosDeAsistente(yo, yo),
                                    DTRegistro::getNombreEdicion));
                } else {
                    List<DTEdicionEvento> ediciones = new ArrayList<>(
                            controlador.listarEdicionesDeOrganizador(yo, yo));
                    ediciones.sort(Comparator.comparing(DTEdicionEvento::getNombre,
                            String.CASE_INSENSITIVE_ORDER));
                    request.setAttribute("ediciones", ediciones);
                }

            } else if (asistente.isEmpty()) {
                // 2b. Una edicion: todos sus registros (la logica exige que sea su organizador).
                request.setAttribute("registros",
                        ordenados(controlador.listarRegistrosDeEdicion(nombreEdicion, yo),
                                DTRegistro::getNickname));

            } else {
                // 2c. Una edicion y un asistente: el detalle de ese registro.
                request.setAttribute("registro",
                        controlador.consultarRegistro(nombreEdicion, asistente, yo));
            }

            // 3. Todo al request y a la vista.
            request.setAttribute("edicion", nombreEdicion);
            Paginas.forward(request, response, Paginas.CONSULTA_REGISTRO);

        } catch (ReglaNegocioException e) {
            // No existe, o no le corresponde verlo: la logica no distingue, a proposito.
            Paginas.noEncontrada(request, response, e.getMessage());
        }
    }

    /** Los Set no tienen orden; para mostrarlos se ordenan por la clave dada. */
    private static List<DTRegistro> ordenados(Collection<DTRegistro> registros,
                                              Function<DTRegistro, String> clave) {
        List<DTRegistro> lista = new ArrayList<>(registros);
        lista.sort(Comparator.comparing(clave, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    private static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }
}
