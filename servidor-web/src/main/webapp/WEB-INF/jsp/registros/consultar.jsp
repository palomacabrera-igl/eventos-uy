<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEdicionEvento" %>
<%@ page import="logica.DTRegistro" %>
<%@ page import="servlets.Texto" %>
<%--
    Consulta de Registro (CU 13). Cuatro vistas en la misma pagina:
      - el detalle de un registro                       (llega "registro")
      - los registros de una edicion, para su organizador (llegan "registros" y "edicion")
      - los registros propios de un asistente           (llegan "registros", sin "edicion")
      - las ediciones de un organizador, para elegir una (llegan "ediciones")
    La JSP solo dibuja: la logica ya decidio que le corresponde ver a quien mira.
--%>
<%
    String edicion = (String) request.getAttribute("edicion");
    DTRegistro registro = (DTRegistro) request.getAttribute("registro");
    @SuppressWarnings("unchecked")
    List<DTRegistro> registros = (List<DTRegistro>) request.getAttribute("registros");
    @SuppressWarnings("unchecked")
    List<DTEdicionEvento> ediciones = (List<DTEdicionEvento>) request.getAttribute("ediciones");
    String ctx = request.getContextPath();
    boolean deUnaEdicion = edicion != null && !edicion.isEmpty();
    String edicionUrl = deUnaEdicion ? Texto.paraUrl(edicion) : "";

    String titulo;
    if (registro != null) {
        titulo = "Registro de @" + registro.getNickname();
    } else if (deUnaEdicion) {
        titulo = "Registros de " + edicion;
    } else if (registros != null) {
        titulo = "Mis registros";
    } else {
        titulo = "Registros de mis ediciones";
    }
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
        <section class="ev-pagehead">
            <% if (deUnaEdicion) { %>
            <a class="ev-back" href="<%= ctx %>/edicion?nombre=<%= edicionUrl %>">&larr; <%= Texto.escapar(edicion) %></a>
            <% } %>
            <p class="ev-kicker mt-2">Registro</p>
            <h1 class="ev-pagetitle mb-0"><%= Texto.escapar(titulo) %></h1>
        </section>

        <% if (registro != null) { %>
        <%-- ---------- Detalle de un registro ---------- --%>
        <dl class="ev-detail">
            <dt>Edición</dt>
            <dd><a href="<%= ctx %>/edicion?nombre=<%= edicionUrl %>"><%= Texto.escapar(registro.getNombreEdicion()) %></a></dd>
            <dt>Asistente</dt>
            <dd><a href="<%= ctx %>/perfil?nickname=<%= Texto.paraUrl(registro.getNickname()) %>">@<%= Texto.escapar(registro.getNickname()) %></a></dd>
            <dt>Fecha de registro</dt><dd><%= Texto.fecha(registro.getFechaRegistro()) %></dd>
            <dt>Tipo de registro</dt>
            <dd><a href="<%= ctx %>/tipo-registro?edicion=<%= edicionUrl %>&amp;nombre=<%= Texto.paraUrl(registro.getTipoRegistro()) %>"><%= Texto.escapar(registro.getTipoRegistro()) %></a></dd>
            <dt>Costo</dt><dd><%= costo(registro.getCosto()) %></dd>
            <dt>Código de patrocinio</dt><dd><%= registro.getUsoCodigo() ? "Sí" : "No utilizó" %></dd>
        </dl>

        <% } else if (registros != null) { %>
        <%-- ---------- Una lista de registros ---------- --%>
        <% if (registros.isEmpty()) { %>
        <p class="text-secondary"><%= deUnaEdicion
                ? "Todavía nadie se registró a esta edición."
                : "Todavía no te registraste a ninguna edición." %></p>
        <% } %>
        <% for (DTRegistro r : registros) { %>
        <a class="ev-row" href="<%= ctx %>/registro?edicion=<%= Texto.paraUrl(r.getNombreEdicion()) %>&amp;asistente=<%= Texto.paraUrl(r.getNickname()) %>">
            <span>
                <%-- En una edicion importa quien; en "mis registros", a que edicion. --%>
                <span class="ev-rowname"><%= deUnaEdicion
                        ? "@" + Texto.escapar(r.getNickname())
                        : Texto.escapar(r.getNombreEdicion()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(r.getTipoRegistro()) %> · <%= Texto.fecha(r.getFechaRegistro()) %><%= r.getUsoCodigo() ? " · con código de patrocinio" : "" %></span>
            </span>
            <span class="ev-estado"><%= costo(r.getCosto()) %></span>
        </a>
        <% } %>

        <% } else { %>
        <%-- ---------- Las ediciones del organizador, para elegir una ---------- --%>
        <% if (ediciones.isEmpty()) { %>
        <p class="text-secondary">Todavía no organizás ninguna edición.</p>
        <% } %>
        <% for (DTEdicionEvento ed : ediciones) {
               String estado = ed.getEstado().name().toLowerCase(); %>
        <a class="ev-row" href="<%= ctx %>/registro?edicion=<%= Texto.paraUrl(ed.getNombre()) %>">
            <span>
                <span class="ev-rowname"><%= Texto.escapar(ed.getNombre()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(ed.getNombreEvento()) %></span>
            </span>
            <span class="ev-estado ev-estado--<%= estado %>"><%= Character.toUpperCase(estado.charAt(0)) + estado.substring(1) %></span>
        </a>
        <% } %>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
<%!
    /** Un registro gratis (costo 0, por ejemplo con codigo) se muestra "Sin cargo". */
    private static String costo(Double costo) {
        if (costo == null || costo == 0) {
            return "Sin cargo";
        }
        return Texto.importe(costo);
    }
%>
