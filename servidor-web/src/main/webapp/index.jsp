<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%-- La entrada del sitio: manda a la cartelera, que es un servlet. Mismo patron
     que la demo del curso (index.jsp redirige a /rutinas). --%>
<% response.sendRedirect(request.getContextPath() + "/eventos"); %>
