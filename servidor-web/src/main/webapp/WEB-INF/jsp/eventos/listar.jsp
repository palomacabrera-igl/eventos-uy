<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEvento" %>
<%@ page import="servlets.Texto" %>
<%
    // La cartelera de la Parte 1 (index.html), ahora con los datos de la base.
    // La JSP solo dibuja: el filtro por categoria y el orden ya vienen hechos.
    @SuppressWarnings("unchecked")
    List<DTEvento> eventos = (List<DTEvento>) request.getAttribute("eventos");
    String titulo = (String) request.getAttribute("titulo");
    String categoriaActiva = (String) request.getAttribute("categoriaActiva");
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
    <main class="container pt-4">
        <h1 class="visually-hidden">Cartelera de eventos de Uruguay</h1>
        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <% if (categoriaActiva != null) { %>
        <p class="ev-fnote"><%= eventos.size() %> evento(s) en <b><%= Texto.escapar(categoriaActiva) %></b></p>
        <% } %>

        <% if (eventos.isEmpty()) { %>
        <p class="text-secondary">
            <%= categoriaActiva == null
                    ? "Todavía no hay eventos publicados."
                    : "No hay eventos en esta categoría." %>
        </p>
        <% } else { %>
        <div class="row g-3">
            <% for (int i = 0; i < eventos.size(); i++) {
                   DTEvento e = eventos.get(i);
                   // Sin imagen, el poster de color alterna entre los dos tonos, como en la Parte 1.
                   String clase = e.getTieneImagen() ? "ev-poster--foto" : (i % 2 == 0 ? "ev-poster--ink" : "ev-poster--paper");
                   String urlEvento = ctx + "/evento?nombre=" + Texto.paraUrl(e.getNombre()); %>
            <div class="col-12 col-sm-6 col-lg-4 ev-card-wrap">
                <article class="ev-card">
                    <div class="ev-poster <%= clase %>">
                        <% if (e.getTieneImagen()) { %>
                        <img class="ev-poster-img" src="<%= ctx %>/imagen?tipoEntidad=evento&amp;clave=<%= Texto.paraUrl(e.getNombre()) %>"
                             alt="Imagen del evento <%= Texto.escapar(e.getNombre()) %>">
                        <% } %>
                        <div class="ev-poster-top"><span><%= Texto.escapar(String.join(" · ", e.getCategorias())) %></span></div>
                        <div class="ev-poster-title"><%= Texto.escapar(e.getNombre()) %></div>
                    </div>
                    <div class="ev-cardbody">
                        <p class="ev-carddesc"><%= Texto.escapar(e.getDescripcion()) %></p>
                        <a class="btn btn-outline-secondary btn-sm" href="<%= urlEvento %>">Ver evento</a>
                    </div>
                </article>
            </div>
            <% } %>
        </div>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
