<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEdicionEvento" %>
<%@ page import="logica.DTEvento" %>
<%@ page import="servlets.Texto" %>
<%
    // Consulta de Evento (CU 7): la pagina evento.html de la Parte 1, ahora con
    // los datos de la base. Las ediciones ya llegan filtradas (solo Aceptada) y
    // ordenadas: la JSP no decide nada, solo las muestra.
    DTEvento evento = (DTEvento) request.getAttribute("evento");
    @SuppressWarnings("unchecked")
    List<DTEdicionEvento> ediciones = (List<DTEdicionEvento>) request.getAttribute("ediciones");
    String ctx = request.getContextPath();
    String categorias = String.join(", ", evento.getCategorias());
    logica.DTUsuario actual = servlets.SesionWeb.actual(request);
    boolean organizador = actual != null && actual.esOrganizador();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title><%= Texto.escapar(evento.getNombre()) %> · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container">
        <section class="ev-pagehead">
            <a class="ev-back" href="<%= ctx %>/eventos">&larr; Volver a la cartelera</a>
        </section>

        <div class="row g-4">
            <div class="col-12 col-lg-5">
                <%-- La letra pide la imagen "si tiene"; si no, el poster de color. --%>
                <% if (evento.getTieneImagen()) { %>
                <img class="img-fluid rounded"
                     src="<%= ctx %>/imagen?tipoEntidad=evento&amp;clave=<%= Texto.paraUrl(evento.getNombre()) %>"
                     alt="Imagen del evento <%= Texto.escapar(evento.getNombre()) %>">
                <% } else { %>
                <div class="ev-poster ev-poster--ink" style="aspect-ratio:4/3;border-radius:4px;">
                    <div class="ev-poster-top"><span><%= Texto.escapar(String.join(" · ", evento.getCategorias())) %></span></div>
                    <div class="ev-poster-title" style="font-size:2.4rem;"><%= Texto.escapar(evento.getNombre()) %></div>
                </div>
                <% } %>
            </div>

            <div class="col-12 col-lg-7">
                <p class="ev-kicker">Evento</p>
                <h1 class="ev-pagetitle mb-3"><%= Texto.escapar(evento.getNombre()) %></h1>
                <dl class="ev-detail">
                    <dt>Descripción</dt><dd><%= Texto.escapar(evento.getDescripcion()) %></dd>
                    <dt>Sigla</dt><dd><%= Texto.escapar(evento.getSigla()) %></dd>
                    <dt>Categorías</dt><dd><%= Texto.escapar(categorias) %></dd>
                    <dt>Alta</dt><dd><%= Texto.fecha(evento.getFechaAlta()) %></dd>
                </dl>
            </div>
        </div>

        <div class="ev-section">Ediciones</div>
        <% if (ediciones.isEmpty()) { %>
        <p class="text-secondary">Este evento todavía no tiene ediciones publicadas.</p>
        <% } else { %>
            <% for (DTEdicionEvento ed : ediciones) { %>
        <a class="ev-row" href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(ed.getNombre()) %>">
            <span>
                <span class="ev-rowname"><%= Texto.escapar(ed.getNombre()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(ed.getCiudad()) %> · <%= Texto.fecha(ed.getFechaInicio()) %></span>
            </span>
            <span class="ev-estado ev-estado--aceptada">Aceptada</span>
        </a>
            <% } %>
        <% } %>
        <%-- Un organizador puede crearle una edicion nueva (CU 8). Solo es el
             enlace: quien puede hacerlo lo vuelve a controlar la logica. --%>
        <% if (organizador) { %>
        <a class="ev-back d-inline-block mt-2" href="<%= ctx %>/edicion/alta?evento=<%= Texto.paraUrl(evento.getNombre()) %>">+ Agregar edición</a>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
