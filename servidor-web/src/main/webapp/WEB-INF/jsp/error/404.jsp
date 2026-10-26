<%@ page contentType="text/html" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ page import="servlets.Texto" %>
<% String mensaje = (String) request.getAttribute("mensaje"); %>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>No encontrado · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container-fluid py-5 px-3 px-lg-4">
        <h1 class="ev-pagetitle">No encontramos esa página</h1>
        <p><%= mensaje == null ? "La dirección no existe o el contenido ya no está disponible." : Texto.escapar(mensaje) %></p>
        <a href="<%= request.getContextPath() %>/eventos">← Volver a la cartelera</a>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
