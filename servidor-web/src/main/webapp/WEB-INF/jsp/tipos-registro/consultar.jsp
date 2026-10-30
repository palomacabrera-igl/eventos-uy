<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="logica.DTTipoRegistro" %>
<%@ page import="servlets.Texto" %>
<%
    DTTipoRegistro tipo = (DTTipoRegistro) request.getAttribute("tipo");
    String edicion = (String) request.getAttribute("edicion");
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title><%= Texto.escapar(tipo.getNombre()) %> · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <a class="ev-back" href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(edicion) %>">
                &larr; <%= Texto.escapar(edicion) %>
            </a>
            <p class="ev-kicker mt-2">Tipo de registro</p>
            <h1 class="ev-pagetitle"><%= Texto.escapar(tipo.getNombre()) %></h1>
        </section>

        <dl class="ev-detail">
            <dt>Edicion</dt>
            <dd><a href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(edicion) %>"><%= Texto.escapar(edicion) %></a></dd>
            <dt>Descripcion</dt><dd><%= Texto.escapar(tipo.getDescripcion()) %></dd>
            <dt>Costo</dt><dd>$ <%= tipo.getCosto() %></dd>
            <dt>Cupo</dt><dd><%= tipo.getCupo() %> lugares</dd>
        </dl>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
