<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTInstitucion" %>
<%@ page import="logica.DTPatrocinio" %>
<%@ page import="servlets.Texto" %>
<%--
    Consulta de Patrocinio (CU 16). Dos vistas en la misma pagina:
    el listado de los patrocinios de una edicion, o el detalle de uno.
    La JSP solo dibuja: el codigo aparece si la logica lo mando, y la
    logica lo manda solo al organizador de la edicion.
--%>
<%
    String edicion = (String) request.getAttribute("edicion");
    DTPatrocinio patrocinio = (DTPatrocinio) request.getAttribute("patrocinio");
    DTInstitucion institucion = (DTInstitucion) request.getAttribute("institucion");
    @SuppressWarnings("unchecked")
    List<DTPatrocinio> patrocinios = (List<DTPatrocinio>) request.getAttribute("patrocinios");
    String ctx = request.getContextPath();
    String edicionUrl = Texto.paraUrl(edicion);
    String titulo = patrocinio != null ? patrocinio.getInstitucion() : "Patrocinios de " + edicion;
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
            <a class="ev-back" href="<%= ctx %>/edicion?nombre=<%= edicionUrl %>">&larr; <%= Texto.escapar(edicion) %></a>
            <p class="ev-kicker mt-2">Patrocinio</p>
            <div class="d-flex align-items-center gap-3 flex-wrap">
                <% if (institucion != null && institucion.getTieneImagen()) { %>
                <img src="<%= ctx %>/imagen?tipoEntidad=institucion&amp;clave=<%= Texto.paraUrl(institucion.getNombre()) %>"
                     alt="Imagen de <%= Texto.escapar(institucion.getNombre()) %>"
                     style="width:56px;height:56px;border-radius:4px;object-fit:cover;">
                <% } %>
                <h1 class="ev-pagetitle mb-0"><%= Texto.escapar(titulo) %></h1>
            </div>
        </section>

        <% if (patrocinio != null) { %>
        <dl class="ev-detail">
            <dt>Edición</dt>
            <dd><a href="<%= ctx %>/edicion?nombre=<%= edicionUrl %>"><%= Texto.escapar(edicion) %></a></dd>
            <dt>Institución</dt><dd><%= Texto.escapar(patrocinio.getInstitucion()) %></dd>
            <dt>Nivel</dt><dd><%= Texto.escapar(nivel(patrocinio.getNivel())) %></dd>
            <dt>Aporte</dt><dd><%= Texto.importe(patrocinio.getMonto()) %></dd>
            <dt>Tipo de registro</dt>
            <dd><a href="<%= ctx %>/tipo-registro?edicion=<%= edicionUrl %>&amp;nombre=<%= Texto.paraUrl(patrocinio.getTipoRegistro()) %>"><%= Texto.escapar(patrocinio.getTipoRegistro()) %></a></dd>
            <dt>Registros gratuitos</dt><dd><%= patrocinio.getCantRegistrosGratis() %></dd>
            <dt>Fecha de alta</dt><dd><%= Texto.fecha(patrocinio.getFecha()) %></dd>
            <%-- La letra: el codigo no se publica. Solo llega si quien mira organiza la edicion. --%>
            <% if (patrocinio.getCodigoPatrocinio() != null) { %>
            <dt>Código de patrocinio</dt><dd><code><%= Texto.escapar(patrocinio.getCodigoPatrocinio()) %></code></dd>
            <% } %>
        </dl>

        <% } else { %>
        <% if (patrocinios.isEmpty()) { %>
        <p class="text-secondary">Esta edición todavía no tiene patrocinios.</p>
        <% } %>
        <% for (DTPatrocinio p : patrocinios) { %>
        <a class="ev-row" href="<%= ctx %>/patrocinio?edicion=<%= edicionUrl %>&amp;institucion=<%= Texto.paraUrl(p.getInstitucion()) %>">
            <span class="ev-rowname"><%= Texto.escapar(p.getInstitucion()) %></span>
            <span class="ev-rowmeta"><%= Texto.escapar(nivel(p.getNivel())) %> · <%= Texto.importe(p.getMonto()) %></span>
        </a>
        <% } %>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
<%!
    /** "ORO" se muestra "Oro", como en la letra. */
    private static String nivel(String nivel) {
        if (nivel == null || nivel.isEmpty()) {
            return "";
        }
        return nivel.charAt(0) + nivel.substring(1).toLowerCase();
    }
%>
