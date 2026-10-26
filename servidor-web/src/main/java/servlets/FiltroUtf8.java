package servlets;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

/**
 * Un filtro corre antes y despues de CADA peticion, para todas las
 * direcciones del sitio.
 *
 * Este se asegura de que los formularios lleguen en UTF-8. Nuestros datos
 * tienen tildes y enies, y sin esto llegan rotos.
 */
@WebFilter("/*")
public class FiltroUtf8 implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain cadena)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        cadena.doFilter(request, response);
    }
}
