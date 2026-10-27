package servlets;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import logica.Fabrica;
import logica.IControladorSistema;

/**
 * Marca el principio y el fin de cada pagina del sitio.
 *
 * Hace dos cosas, y las dos son por el mismo motivo: la logica tiene UN SOLO
 * EntityManager y Tomcat atiende cada peticion en un hilo distinto.
 *
 * 1. REFRESCA. El sitio y la Estacion de Trabajo son dos programas sobre la
 *    misma base, y cada uno se guarda en memoria lo que ya leyo. Sin esto, una
 *    edicion aceptada desde la Estacion de Trabajo se seguiria viendo
 *    Ingresada en el sitio hasta reiniciar Tomcat.
 *
 * 2. DA EL TURNO. Un EntityManager no se puede usar desde dos hilos a la vez,
 *    lo dice la especificacion de JPA. La pagina entera corre adentro de un
 *    candado, asi que las peticiones que tocan la base se atienden de a una.
 *    Es la misma idea de la demo del curso, donde los metodos del Manejador
 *    son synchronized.
 *
 * Los archivos estaticos (CSS, imagenes, tipografias) se saltean las dos
 * cosas: no necesitan datos de la base y no tienen por que hacer cola. Es lo
 * que mas descongestiona, porque abrir una sola pagina pide ademas su CSS,
 * sus dos tipografias y una imagen por cada evento.
 */
@WebFilter("/*")
public class FiltroDatosFrescos implements Filter {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain cadena)
            throws IOException, ServletException {

        if (esArchivo(request)) {
            cadena.doFilter(request, response);
            return;
        }

        controlador.abrirUnidadDeTrabajo();
        try {
            cadena.doFilter(request, response);
        } finally {
            // SIEMPRE en un finally: si la pagina falla y el candado no se
            // suelta, el sitio queda colgado para todos hasta reiniciar Tomcat.
            controlador.cerrarUnidadDeTrabajo();
        }
    }

    /**
     * true si lo que se pidio parece un archivo y no una pagina.
     *
     * Las direcciones del sitio no llevan punto (/eventos, /evento, /perfil);
     * los archivos si (/assets/css/custom.css, /public/images/jiap.png).
     */
    private static boolean esArchivo(ServletRequest request) {
        if (!(request instanceof HttpServletRequest pedido)) {
            return false;
        }
        String ruta = pedido.getRequestURI();
        String ultimo = ruta.substring(ruta.lastIndexOf('/') + 1);
        return ultimo.indexOf('.') >= 0;
    }
}
