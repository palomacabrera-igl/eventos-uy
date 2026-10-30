<%@ page contentType="text/html" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ page import="servlets.Texto" %>
<%
    String mensaje = (String) request.getAttribute("mensaje");
    // Esta pagina sirve para el "no existe" y para el "no te corresponde".
    // Si quien reenvia aca pone un titulo, manda el suyo.
    String titulo = (String) request.getAttribute("titulo");
    if (titulo == null) { titulo = "No encontramos esa página"; }
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title><%= Texto.escapar(titulo) %> · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container-fluid py-5 px-3 px-lg-4">
        <h1 class="ev-pagetitle"><%= Texto.escapar(titulo) %></h1>
        <p><%= mensaje == null ? "La dirección no existe o el contenido ya no está disponible." : Texto.escapar(mensaje) %></p>
        <a href="<%= request.getContextPath() %>/eventos">← Volver a la cartelera</a>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
