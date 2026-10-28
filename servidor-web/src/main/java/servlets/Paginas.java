package servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Donde esta cada JSP y las dos formas de pasarle el control.
 *
 * Las JSP viven bajo WEB-INF a proposito: lo que esta ahi adentro NO se puede
 * pedir por URL. Se llega a una pagina solo pasando por su servlet, que es lo
 * que garantiza que nadie se saltee los controles.
 */
public final class Paginas {

    public static final String EVENTOS = "/WEB-INF/jsp/eventos/listar.jsp";
    public static final String EVENTO = "/WEB-INF/jsp/eventos/consultar.jsp";
    public static final String ALTA_EVENTO = "/WEB-INF/jsp/eventos/alta.jsp";
    public static final String EDICION = "/WEB-INF/jsp/ediciones/consultar.jsp";
    public static final String ALTA_EDICION = "/WEB-INF/jsp/ediciones/alta.jsp";
    public static final String LOGIN = "/WEB-INF/jsp/sesion/login.jsp";
    public static final String REGISTRO = "/WEB-INF/jsp/sesion/registro.jsp";
    public static final String PERFIL = "/WEB-INF/jsp/usuarios/perfil.jsp";
    public static final String PERFIL_EDITAR = "/WEB-INF/jsp/usuarios/editar.jsp";
    public static final String PATROCINIO_ALTA = "/WEB-INF/jsp/patrocinios/alta.jsp";
    public static final String ALTA_TIPO_REGISTRO = "/WEB-INF/jsp/tipos-registro/alta.jsp";
    public static final String CONSULTA_TIPO_REGISTRO = "/WEB-INF/jsp/tipos-registro/consultar.jsp";
    public static final String ALTA_INSTITUCION = "/WEB-INF/jsp/instituciones/alta.jsp";
    public static final String NO_ENCONTRADA = "/WEB-INF/jsp/error/404.jsp";

    private Paginas() {
    }

    /**
     * La direccion del navegador NO cambia. Es para volver a mostrar un
     * formulario con su error y los datos que la persona ya habia escrito.
     */
    public static void forward(HttpServletRequest request, HttpServletResponse response,
                               String jsp) throws ServletException, IOException {
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    /**
     * La direccion SI cambia. Es para despues de un POST que guardo algo: asi
     * recargar con F5 no vuelve a enviar el formulario y no se duplica el alta.
     */
    public static void redirect(HttpServletRequest request, HttpServletResponse response,
                                String ruta) throws IOException {
        response.sendRedirect(request.getContextPath() + ruta);
    }

    public static void noEncontrada(HttpServletRequest request, HttpServletResponse response,
                                    String mensaje) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        request.setAttribute("mensaje", mensaje);
        forward(request, response, NO_ENCONTRADA);
    }
}
