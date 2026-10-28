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
import logica.DTEdicionEvento;
import logica.DTUsuario;
import logica.Fabrica;
import logica.IControladorSistema;
import logica.ReglaNegocioException;

/**
 * Alta de Tipo de Registro (CU 10).
 *
 * El GET carga las ediciones del organizador, incluso las Ingresada y
 * Rechazada. El POST valida el formato de los numeros y delega las reglas de
 * negocio (propiedad de la edicion y nombre repetido) a la capa logica.
 */
@WebServlet("/tipo-registro/alta")
public class AltaTipoRegistro extends HttpServlet {

    private final transient IControladorSistema controlador =
            Fabrica.getInstancia().getControladorSistema();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!esOrganizador(request)) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }
        mostrarFormulario(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!esOrganizador(request)) {
            Paginas.redirect(request, response, "/iniciar-sesion");
            return;
        }

        try {
            String solicitante = SesionWeb.solicitante(request);
            String edicion = texto(request, "edicion");
            String nombre = texto(request, "nombre");
            String descripcion = texto(request, "descripcion");
            double costo = numeroDecimal(request, "costo", "El costo");
            int cupo = numeroEntero(request, "cupo", "El cupo");

            controlador.altaTipoRegistro(solicitante, edicion, nombre, descripcion, costo, cupo);
            Paginas.redirect(request, response, "/tipo-registro/alta?creado="
                    + Texto.paraUrl(nombre));

        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            for (String campo : new String[]{"edicion", "nombre", "descripcion", "costo", "cupo"}) {
                request.setAttribute(campo, texto(request, campo));
            }
            mostrarFormulario(request, response);
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String nickname = SesionWeb.solicitante(request);
            List<DTEdicionEvento> ediciones = new ArrayList<>(
                    controlador.listarEdicionesDeOrganizador(nickname, nickname));
            ediciones.sort(Comparator.comparing(DTEdicionEvento::getNombre,
                    String.CASE_INSENSITIVE_ORDER));
            request.setAttribute("ediciones", ediciones);
            Paginas.forward(request, response, Paginas.ALTA_TIPO_REGISTRO);
        } catch (ReglaNegocioException error) {
            request.setAttribute("error", error.getMessage());
            request.setAttribute("ediciones", List.of());
            Paginas.forward(request, response, Paginas.ALTA_TIPO_REGISTRO);
        }
    }

    private static boolean esOrganizador(HttpServletRequest request) {
        DTUsuario usuario = SesionWeb.actual(request);
        return usuario != null && usuario.esOrganizador();
    }

    private static String texto(HttpServletRequest request, String campo) {
        String valor = request.getParameter(campo);
        return valor == null ? "" : valor.trim();
    }

    private static double numeroDecimal(HttpServletRequest request, String campo, String etiqueta)
            throws ReglaNegocioException {
        try {
            double valor = Double.parseDouble(texto(request, campo));
            if (!Double.isFinite(valor)) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException error) {
            throw new ReglaNegocioException(etiqueta + " debe ser un numero valido.");
        }
    }

    private static int numeroEntero(HttpServletRequest request, String campo, String etiqueta)
            throws ReglaNegocioException {
        try {
            return Integer.parseInt(texto(request, campo));
        } catch (NumberFormatException error) {
            throw new ReglaNegocioException(etiqueta + " debe ser un numero entero valido.");
        }
    }
}
