package servlets;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cierre de sesion (la otra mitad del CU 1).
 *
 * Solo doPost: el boton "Salir" del cabezal es un formulario, no un enlace.
 * Si fuera un enlace, cualquier pagina ajena podria cerrarle la sesion a quien
 * la visite con un simple <img src=".../cerrar-sesion">, porque el navegador
 * pide las imagenes con GET sin preguntar nada.
 */
@WebServlet("/cerrar-sesion")
public class CerrarSesion extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        SesionWeb.cerrar(request);
        Paginas.redirect(request, response, "/eventos");
    }
}
