<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.Set" %>
<%@ page import="logica.DTCategoria" %>
<%@ page import="logica.DTUsuario" %>
<%@ page import="servlets.SesionWeb" %>
<%@ page import="servlets.Texto" %>
<%
    // Lo que antes inyectaba cabezal.js con JavaScript, ahora llega ya armado.
    DTUsuario actual = SesionWeb.actual(request);
    String ctx = request.getContextPath();
    Set<DTCategoria> categorias =
            (Set<DTCategoria>) request.getAttribute("categorias");
%>
<header class="ev-top">
    <div class="container-fluid d-flex align-items-center justify-content-between py-2 px-3 px-lg-4">
        <a class="ev-brand" href="<%= ctx %>/eventos">eventos.uy</a>
        <nav class="d-flex align-items-center gap-3">
        <% if (actual == null) { %>
            <a href="<%= ctx %>/iniciar-sesion">Iniciar sesión</a>
            <a href="<%= ctx %>/registrarse">Registrarse</a>
        <% } else { %>
            <a href="<%= ctx %>/perfil"><%= Texto.escapar(actual.getNombre()) %></a>
            <form action="<%= ctx %>/cerrar-sesion" method="post" class="d-inline">
                <button class="btn btn-link p-0" type="submit">Salir</button>
            </form>
        <% } %>
        </nav>
    </div>
    <% if (categorias != null && !categorias.isEmpty()) { %>
    <div class="container-fluid px-3 px-lg-4 pb-2 d-flex flex-wrap gap-2">
        <span class="ev-kicker">Categorías</span>
        <% for (DTCategoria c : categorias) { %>
            <a href="<%= ctx %>/eventos?cat=<%= java.net.URLEncoder.encode(c.getNombre(), "UTF-8") %>"><%= Texto.escapar(c.getNombre()) %></a>
        <% } %>
    </div>
    <% } %>
</header>
