<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEdicionEvento" %>
<%@ page import="servlets.Texto" %>
<%!
    private static String previo(jakarta.servlet.jsp.PageContext pagina, String campo) {
        Object valor = pagina.getRequest().getAttribute(campo);
        return valor == null ? "" : Texto.escapar(valor.toString());
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<DTEdicionEvento> ediciones = (List<DTEdicionEvento>) request.getAttribute("ediciones");
    String ctx = request.getContextPath();
    String seleccionada = previo(pageContext, "edicion");
    String creado = request.getParameter("creado");
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Alta de tipo de registro · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <p class="ev-kicker">Alta de tipo de registro</p>
            <h1 class="ev-pagetitle">Crear un tipo de registro</h1>
        </section>

        <% if (creado != null && !creado.isBlank()) { %>
        <div class="alert alert-success" role="alert">
            El tipo de registro <strong><%= Texto.escapar(creado) %></strong> fue creado correctamente.
        </div>
        <% } %>
        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <% if (ediciones == null || ediciones.isEmpty()) { %>
        <div class="alert alert-info" role="alert">
            Todavia no tenes ediciones para crear un tipo de registro.
        </div>
        <% } else { %>
        <form class="ev-form" method="post" action="<%= ctx %>/tipo-registro/alta">
            <div class="mb-3">
                <label class="form-label" for="edicion">Edicion</label>
                <select class="form-select" id="edicion" name="edicion" required>
                    <option value="">Elegi una edicion</option>
                    <% for (DTEdicionEvento edicion : ediciones) {
                        String nombre = Texto.escapar(edicion.getNombre()); %>
                    <option value="<%= nombre %>" <%= nombre.equals(seleccionada) ? "selected" : "" %>>
                        <%= nombre %> (<%= edicion.getEstado() %>)
                    </option>
                    <% } %>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre</label>
                <input class="form-control" id="nombre" name="nombre" type="text" required
                       value="<%= previo(pageContext, "nombre") %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="descripcion">Descripcion</label>
                <textarea class="form-control" id="descripcion" name="descripcion" rows="2" required><%= previo(pageContext, "descripcion") %></textarea>
            </div>
            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="costo">Costo</label>
                    <input class="form-control" id="costo" name="costo" type="number" min="0" step="0.01" required
                           value="<%= previo(pageContext, "costo") %>">
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="cupo">Cupo</label>
                    <input class="form-control" id="cupo" name="cupo" type="number" min="1" step="1" required
                           value="<%= previo(pageContext, "cupo") %>">
                </div>
            </div>
            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-accent" type="submit">Crear tipo de registro</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
        </form>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
