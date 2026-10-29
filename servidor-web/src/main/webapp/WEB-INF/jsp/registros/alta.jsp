<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEdicionCompleto" %>
<%@ page import="logica.DTTipoRegistro" %>
<%@ page import="logica.DTUsuario" %>
<%@ page import="servlets.SesionWeb" %>
<%@ page import="servlets.Texto" %>
<%
    DTEdicionCompleto edicion = (DTEdicionCompleto) request.getAttribute("edicion");
    @SuppressWarnings("unchecked")
    List<DTTipoRegistro> tipos = (List<DTTipoRegistro>) request.getAttribute("tipos");
    String ctx = request.getContextPath();
    String seleccion = (String) request.getAttribute("tipoSeleccionado");
    String codigo = (String) request.getAttribute("codigo");
    DTUsuario asistente = SesionWeb.actual(request);
    boolean registroCreado = Boolean.TRUE.equals(request.getAttribute("registroCreado"));
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Registro a edición · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <a class="ev-back" href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(edicion.getNombre()) %>">
                &larr; <%= Texto.escapar(edicion.getNombre()) %>
            </a>
            <p class="ev-kicker mt-2">Registro a edición</p>
            <h1 class="ev-pagetitle">Registrarme</h1>
            <p>Edición: <strong><%= Texto.escapar(edicion.getNombre()) %></strong></p>
        </section>

        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <% if (registroCreado) { %>
        <a class="btn btn-outline-secondary"
           href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(edicion.getNombre()) %>">
            Volver a la edición
        </a>
        <% } else if (tipos.isEmpty()) { %>
        <div class="alert alert-info" role="alert">
            Esta edición todavía no tiene tipos de registro.
        </div>
        <% } else { %>
        <form class="ev-form" method="post" action="<%= ctx %>/registro">
            <input type="hidden" name="edicion" value="<%= Texto.escapar(edicion.getNombre()) %>">
            <p class="form-text">El registro se realizará con tu cuenta <strong>@<%= Texto.escapar(asistente.getNickname()) %></strong>.</p>
            <div class="mb-3">
                <label class="form-label" for="tipo">Tipo de registro</label>
                <select class="form-select" id="tipo" name="tipo" required>
                    <option value="">Elegí un tipo</option>
                    <% for (DTTipoRegistro tipo : tipos) {
                        boolean elegido = tipo.getNombre().equals(seleccion); %>
                    <option value="<%= Texto.escapar(tipo.getNombre()) %>" <%= elegido ? "selected" : "" %>>
                        <%= Texto.escapar(tipo.getNombre()) %> — <%= Texto.importe(tipo.getCosto()) %>
                        — cupo <%= tipo.getCupo() %>
                    </option>
                    <% } %>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label" for="codigo">Código de patrocinio (opcional)</label>
                <input class="form-control" id="codigo" name="codigo" type="text"
                       value="<%= Texto.escapar(codigo) %>">
                <div class="form-text">
                    Dejalo vacío para realizar el registro con el costo normal del tipo elegido.
                </div>
            </div>
            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-accent" type="submit">Confirmar registro</button>
                <a class="btn btn-outline-secondary"
                   href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(edicion.getNombre()) %>">Cancelar</a>
            </div>
        </form>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
