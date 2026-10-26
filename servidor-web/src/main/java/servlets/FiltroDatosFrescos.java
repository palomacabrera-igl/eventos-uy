package servlets;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import logica.Fabrica;

/**
 * Antes de cada pagina, le pide a la logica que vuelva a leer de la base.
 *
 * El sitio y la Estacion de Trabajo son dos programas distintos sobre la misma
 * base, y cada uno se guarda en memoria lo que ya leyo. Sin esto, una edicion
 * aceptada desde la Estacion de Trabajo se seguiria viendo Ingresada en el
 * sitio hasta reiniciar Tomcat.
 *
 * Es el mismo refresco que en la Estacion de Trabajo hace el boton
 * "Actualizar datos", solo que aca es automatico en cada pagina: asi F5
 * alcanza y nadie tiene que acordarse de apretar nada.
 */
@WebFilter("/*")
public class FiltroDatosFrescos implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain cadena)
            throws IOException, ServletException {
        Fabrica.getInstancia().getControladorSistema().refrescarDatos();
        cadena.doFilter(request, response);
    }
}
