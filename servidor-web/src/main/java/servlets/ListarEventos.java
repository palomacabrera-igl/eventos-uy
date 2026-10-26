package servlets;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logica.DTEvento;
import logica.Fabrica;
import logica.IControladorSistema;

/**
 * La cartelera de eventos. Es el primer caso de uso completo del sitio y sirve
 * de molde para los demas: recibe la peticion, le pide los datos a la logica,
 * los deja en el request y elige la JSP. No escribe ni una linea de HTML.
 */
@WebServlet("/eventos")
public class ListarEventos extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Set<DTEvento> eventos = controlador.listarEventos();

        request.setAttribute("eventos", eventos);
        request.setAttribute("titulo", "Cartelera");

        Paginas.forward(request, response, Paginas.EVENTOS);
    }
}
