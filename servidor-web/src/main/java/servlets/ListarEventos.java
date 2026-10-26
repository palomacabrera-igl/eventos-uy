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
import logica.DTCategoria;
import logica.DTEvento;
import logica.Fabrica;
import logica.IControladorSistema;

/**
 * La cartelera de eventos, con el filtro por categoria (CU 7).
 *
 * Es el primer caso de uso completo del sitio y sirve de molde para los demas:
 * recibe la peticion, le pide los datos a la logica, los deja en el request y
 * elige la JSP. No escribe ni una linea de HTML.
 *
 *   /eventos            la cartelera completa
 *   /eventos?cat=Cultura solo los eventos de esa categoria
 */
@WebServlet("/eventos")
public class ListarEventos extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Los parametros: la categoria elegida en la tira del cabezal, si hay.
        String categoria = request.getParameter("cat");
        if (categoria != null && categoria.isBlank()) {
            categoria = null;
        }

        // 2 y 3. La logica filtra; con null devuelve todos.
        List<DTEvento> eventos = new ArrayList<>(controlador.listarEventos(categoria));

        // El orden es de presentacion, no una regla de negocio: por eso va aca.
        eventos.sort(Comparator.comparing(DTEvento::getNombre, String.CASE_INSENSITIVE_ORDER));

        // 4. Se deja todo en el request para la JSP.
        request.setAttribute("eventos", eventos);
        request.setAttribute("categoriaActiva", categoria);
        request.setAttribute("categorias", nombresDeCategorias());
        request.setAttribute("titulo", categoria == null ? "Cartelera" : categoria);

        // 5 y 6. Se elige la vista y se le pasa el control.
        Paginas.forward(request, response, Paginas.EVENTOS);
    }

    /**
     * Los nombres para la tira de categorias del cabezal. La JSP no puede
     * consultarlos ella misma: en MVC la vista solo muestra lo que le dejan.
     */
    private List<String> nombresDeCategorias() {
        List<String> nombres = new ArrayList<>();
        for (DTCategoria c : controlador.listarCategorias()) {
            nombres.add(c.getNombre());
        }
        nombres.sort(String.CASE_INSENSITIVE_ORDER);
        return nombres;
    }
}
