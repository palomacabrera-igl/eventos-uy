<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTUsuario" %>
<%@ page import="servlets.SesionWeb" %>
<%@ page import="servlets.Texto" %>
<%
    // Lo que en la Parte 1 armaba cabezal.js con JavaScript, ahora llega armado
    // desde el servidor. El rol sale de la sesion (SesionWeb), no del navegador.
    DTUsuario actual = SesionWeb.actual(request);
    String ctx = request.getContextPath();

    // La tira de categorias solo aparece donde sirve para filtrar: la cartelera.
    // Es la unica pagina cuyo servlet deja "categorias" en el request.
    @SuppressWarnings("unchecked")
    List<String> categorias = (List<String>) request.getAttribute("categorias");
    String categoriaActiva = (String) request.getAttribute("categoriaActiva");
%>
<header class="ev-header">
    <div class="container">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 py-2">
            <a href="<%= ctx %>/eventos" class="ev-brand">eventos<span>.uy</span></a>
            <nav class="d-flex align-items-center gap-2 flex-wrap" aria-label="Sesión">
            <% if (actual == null) { %>
                <a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="<%= ctx %>/iniciar-sesion">Iniciar sesión</a>
                <a class="btn btn-accent btn-sm" href="<%= ctx %>/registrarse">Registrarse</a>
            <% } else { %>
                <% if (actual.esOrganizador()) { %>
                <a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="<%= ctx %>/evento/alta">Alta de evento</a>
                <a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="<%= ctx %>/edicion/alta">Alta de edición</a>
                <a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="<%= ctx %>/institucion/alta">Alta de institución</a>
                <% } %>
                <a class="btn btn-link btn-sm text-decoration-none text-secondary px-1" href="<%= ctx %>/registro"><%= actual.esOrganizador() ? "Registros" : "Mis registros" %></a>
                <span class="ev-who"><%= Texto.escapar(actual.getNickname()) %><small><%= actual.esOrganizador() ? "Organizador" : "Asistente" %></small></span>
                <a class="btn btn-outline-secondary btn-sm" href="<%= ctx %>/perfil?nickname=<%= Texto.paraUrl(actual.getNickname()) %>">Mi perfil</a>
                <%-- Salir va por POST y no por un enlace, porque cambia algo: cierra la sesion. --%>
                <form action="<%= ctx %>/cerrar-sesion" method="post" class="d-inline">
                    <button class="btn btn-link btn-sm text-decoration-none text-secondary px-1" type="submit">Salir</button>
                </form>
            <% } %>
            </nav>
        </div>
    </div>
</header>
<% if (categorias != null && !categorias.isEmpty()) { %>
<nav class="ev-catbar" aria-label="Categorías">
    <div class="container">
        <a class="ev-chip<%= categoriaActiva == null ? " active" : "" %>" href="<%= ctx %>/eventos">Todos</a>
        <% for (String c : categorias) { %>
        <a class="ev-chip<%= c.equals(categoriaActiva) ? " active" : "" %>" href="<%= ctx %>/eventos?cat=<%= Texto.paraUrl(c) %>"><%= Texto.escapar(c) %></a>
        <% } %>
    </div>
</nav>
<% } %>
