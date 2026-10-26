package servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import logica.DTUsuario;

/**
 * La sesion del sitio. Guarda el DT de quien entro, nunca una entidad.
 *
 * En la Parte 1 esto vivia en el navegador (localStorage, demoRol y demoNick)
 * y cualquiera podia cambiarlo desde las herramientas del navegador. Ahora
 * vive en el servidor: la letra (seccion 7.4) pide justamente eso.
 */
public final class SesionWeb {

    public static final String ATRIBUTO = "usuario";

    private SesionWeb() {
    }

    /** Quien tiene la sesion iniciada, o null si es un visitante. */
    public static DTUsuario actual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return null;
        }
        Object valor = sesion.getAttribute(ATRIBUTO);
        return (valor instanceof DTUsuario dt) ? dt : null;
    }

    /** El nickname de quien pregunta, o null: es el "solicitante" del contrato. */
    public static String solicitante(HttpServletRequest request) {
        DTUsuario dt = actual(request);
        return dt == null ? null : dt.getNickname();
    }

    public static void iniciar(HttpServletRequest request, DTUsuario usuario) {
        request.getSession(true).setAttribute(ATRIBUTO, usuario);
    }

    public static void cerrar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
