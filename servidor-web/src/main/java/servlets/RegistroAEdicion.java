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
import logica.DTAsistente;
import logica.DTEdicionEvento;
import logica.DTEvento;
import logica.DTTipoRegistro;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Registro a Edicion de Evento (CU 12).
 *
 * La letra lo describe en tres pasos, y la direccion dice en cual se esta:
 *
 *   /registro/alta                        elegir evento
 *   /registro/alta?evento=JIAP            elegir una edicion Aceptada
 *   /registro/alta?edicion=JIAP%202026    elegir el tipo y como se registra
 *
 * Solo las ediciones <b>Aceptada</b> se pueden elegir. Eso no lo decide este
 * servlet: se le pide a la logica listarEdicionesAceptadas(), y no
 * listarEdicionesDeEvento(), que es la del Laboratorio 1 y devuelve los tres
 * estados porque el administrador si los ve desde la Estacion de Trabajo.
 */
@WebServlet("/registro/alta")
public class RegistroAEdicion extends HttpServlet {

    private static final String TITULO = "Registro a edición";

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = exigirAsistente(request, response);
        if (sesion == null) {
            return;
        }
        mostrarPaso(request, response, sesion);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = exigirAsistente(request, response);
        if (sesion == null) {
            return;
        }

        String nombreEdicion = texto(request, "edicion");
        String tipo = texto(request, "tipo");

        // El codigo solo viaja si eligieron registrarse con patrocinio. Si
        // se manda null, la logica hace el registro general y cobra el tipo.
        String codigo = "codigo".equals(texto(request, "modo"))
                ? texto(request, "codigo")
                : null;

        try {
            controlador.altaRegistro(sesion.getNickname(), nombreEdicion, tipo, codigo);

            Paginas.redirect(request, response,
                    "/registro/alta?hecho=" + Texto.paraUrl(nombreEdicion));

        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            // Vuelve lo elegido, asi la persona corrige en vez de empezar
            // de nuevo, que es lo que pide la letra: «el asistente puede
            // modificar la seleccion o cancelar».
            request.setAttribute("tipo", tipo);
            request.setAttribute("modo", texto(request, "modo"));
            request.setAttribute("codigo", texto(request, "codigo"));
            mostrarPaso(request, response, sesion);
        }
    }

    /** Arma el paso que corresponde, segun lo que diga la direccion. */
    private void mostrarPaso(HttpServletRequest request, HttpServletResponse response,
                             DTUsuario sesion) throws ServletException, IOException {

        String nombreEdicion = texto(request, "edicion");
        String nombreEvento = texto(request, "evento");

        try {
            if (!nombreEdicion.isEmpty()) {
                // Paso 3: los tipos de registro de esa edicion.
                List<DTTipoRegistro> tipos =
                        new ArrayList<>(controlador.listarTiposDeRegistro(nombreEdicion));
                tipos.sort(Comparator.comparing(DTTipoRegistro::getNombre,
                        String.CASE_INSENSITIVE_ORDER));

                request.setAttribute("edicion", nombreEdicion);
                request.setAttribute("tipos", tipos);

                // Un asistente sin institucion solo puede registrarse de
                // forma general (letra, CU 12): la pantalla ni le ofrece el
                // codigo. La logica lo rechaza igual si lo fuerza.
                DTUsuario perfil = controlador.consultarUsuario(
                        sesion.getNickname(), sesion.getNickname());
                String institucion = (perfil instanceof DTAsistente a)
                        ? a.getInstitucion() : null;
                request.setAttribute("institucion", institucion);

                request.setAttribute("paso", "datos");

            } else if (!nombreEvento.isEmpty()) {
                // Paso 2: solo las Aceptada. A una edicion Ingresada o
                // Rechazada no se puede uno registrar.
                List<DTEdicionEvento> ediciones =
                        new ArrayList<>(controlador.listarEdicionesAceptadas(nombreEvento));
                ediciones.sort(Comparator.comparing(DTEdicionEvento::getNombre,
                        String.CASE_INSENSITIVE_ORDER));

                request.setAttribute("evento", nombreEvento);
                request.setAttribute("ediciones", ediciones);
                request.setAttribute("paso", "edicion");

            } else {
                // Paso 1: todos los eventos.
                List<DTEvento> eventos = new ArrayList<>(controlador.listarEventos(null));
                eventos.sort(Comparator.comparing(DTEvento::getNombre,
                        String.CASE_INSENSITIVE_ORDER));

                request.setAttribute("eventos", eventos);
                request.setAttribute("paso", "evento");
            }

            String hecho = texto(request, "hecho");
            if (!hecho.isEmpty()) {
                request.setAttribute("aviso", "Quedaste registrado en " + hecho + ".");
            }

            request.setAttribute("titulo", TITULO);
            Paginas.forward(request, response, Paginas.REGISTRO_ALTA);

        } catch (ReglaNegocioException e) {
            Paginas.noEncontrada(request, response, e.getMessage());
        }
    }

    /** Solo un asistente con sesion se registra a una edicion. */
    private DTUsuario exigirAsistente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DTUsuario sesion = SesionWeb.actual(request);
        if (sesion == null) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return null;
        }
        if (!sesion.esAsistente()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("mensaje",
                    "El registro a una edición es para asistentes.");
            Paginas.forward(request, response, Paginas.NO_ENCONTRADA);
            return null;
        }
        return sesion;
    }

    private static String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }
}
