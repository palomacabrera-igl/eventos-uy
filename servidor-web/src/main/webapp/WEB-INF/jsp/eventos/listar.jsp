<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.Set" %>
<%@ page import="logica.DTEvento" %>
<%@ page import="servlets.Texto" %>
<%
    Set<DTEvento> eventos = (Set<DTEvento>) request.getAttribute("eventos");
    String titulo = (String) request.getAttribute("titulo");
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title><%= Texto.escapar(titulo) %> · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container-fluid py-4 px-3 px-lg-4">
        <h1 class="ev-pagetitle"><%= Texto.escapar(titulo) %></h1>
        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <% if (eventos == null || eventos.isEmpty()) { %>
        <p>Todavía no hay eventos publicados.</p>
        <% } else { %>
        <div class="row g-3">
            <% for (DTEvento e : eventos) { %>
            <div class="col-12 col-md-6 col-lg-4">
                <article class="ev-card h-100 p-3">
                    <h2 class="h5"><%= Texto.escapar(e.getNombre()) %></h2>
                    <p class="mb-2"><%= Texto.escapar(e.getDescripcion()) %></p>
                    <p class="ev-kicker mb-0"><%= Texto.escapar(String.join(" · ", e.getCategorias())) %></p>
                </article>
            </div>
            <% } %>
        </div>
        <p class="mt-4 text-body-secondary">
            <%= eventos.size() %> evento(s), leídos de PostgreSQL.
        </p>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
