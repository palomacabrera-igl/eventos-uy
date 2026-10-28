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
import logica.DTPatrocinio;
import logica.DTRegistro;
import logica.DTTipoRegistro;
import logica.DTUsuario;
import logica.EstadoEdicion;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Consulta de Edicion (CU 9): los datos de una edicion, sus tipos de registro
 * y sus patrocinios; y, solo para su organizador, sus registros.
 *
 *   /edicion?nombre=Maratón%202026
 *
 * Quien pregunta cambia lo que se ve, pero esa regla la aplica la logica
 * (consultarEdicion recibe el solicitante): una edicion que no esta Aceptada
 * solo la ve su organizador, y los registros y los codigos de patrocinio le
 * llegan solo a el. Este servlet no filtra nada.
 */
@WebServlet("/edicion")
public class ConsultarEdicion extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. El parametro y quien pregunta (null si es un visitante).
        String nombre = request.getParameter("nombre");
        String solicitante = SesionWeb.solicitante(request);

        try {
            // 2 y 3. La logica decide si la puede ver y que le corresponde.
            DTEdicionCompleto edicion = controlador.consultarEdicion(nombre, solicitante);

            // Orden de presentacion: tipos y patrocinios por nombre, registros por fecha.
            List<DTTipoRegistro> tipos = new ArrayList<>(edicion.getTiposRegistro());
            tipos.sort(Comparator.comparing(DTTipoRegistro::getNombre, String.CASE_INSENSITIVE_ORDER));
            List<DTPatrocinio> patrocinios = new ArrayList<>(edicion.getPatrocinios());
            patrocinios.sort(Comparator.comparing(DTPatrocinio::getInstitucion, String.CASE_INSENSITIVE_ORDER));
            List<DTRegistro> registros = new ArrayList<>(edicion.getRegistros());
            registros.sort(Comparator.comparing(DTRegistro::getFechaRegistro)
                    .thenComparing(DTRegistro::getNickname));

            // Que botones mostrar. Es solo navegacion: cada pagina de destino
            // vuelve a controlar en la logica si la persona puede hacerlo.
            DTUsuario actual = SesionWeb.actual(request);
            boolean esSuOrganizador = solicitante != null
                    && solicitante.equals(edicion.getOrganizador().getNickname());
            boolean puedeRegistrarse = actual != null && actual.esAsistente()
                    && edicion.getEstado() == EstadoEdicion.ACEPTADA;

            // 4. Todo al request.
            request.setAttribute("edicion", edicion);
            request.setAttribute("tipos", tipos);
            request.setAttribute("patrocinios", patrocinios);
            request.setAttribute("registros", registros);
            request.setAttribute("esSuOrganizador", esSuOrganizador);
            request.setAttribute("puedeRegistrarse", puedeRegistrarse);
            if (request.getParameter("creada") != null) {
                request.setAttribute("aviso", "La edicion se creo. Queda Ingresada hasta que "
                        + "el administrador la acepte; mientras tanto solo la ves vos.");
            }

            // 5 y 6. La vista.
            Paginas.forward(request, response, Paginas.EDICION);

        } catch (ReglaNegocioException e) {
            // Falta el nombre, no existe o no esta publicada: 404 con el motivo.
            Paginas.noEncontrada(request, response, e.getMessage());
        }
    }
}
