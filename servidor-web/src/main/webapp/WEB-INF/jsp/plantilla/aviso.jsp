<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="servlets.Texto" %>
<%
    // El servlet deja el mensaje en el request y esta plantilla lo muestra.
    // Reemplaza a los dos cuadros de alerta que cada pagina tenia en la Parte 1.
    String aviso = (String) request.getAttribute("aviso");
    String error = (String) request.getAttribute("error");
%>
<% if (aviso != null) { %>
<div class="alert alert-success" role="alert"><%= Texto.escapar(aviso) %></div>
<% } %>
<% if (error != null) { %>
<div class="alert alert-danger" role="alert"><%= Texto.escapar(error) %></div>
<% } %>
