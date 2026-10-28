<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEdicionCompleto" %>
<%@ page import="logica.DTPatrocinio" %>
<%@ page import="logica.DTRegistro" %>
<%@ page import="logica.DTTipoRegistro" %>
<%@ page import="servlets.Texto" %>
<%
    // Consulta de Edicion (CU 9): la pagina edicion.html de la Parte 1, ahora
    // con los datos de la base. Lo que llega ya viene recortado por la logica
    // segun quien mira: un visitante no recibe registros ni codigos.
    DTEdicionCompleto ed = (DTEdicionCompleto) request.getAttribute("edicion");
    @SuppressWarnings("unchecked")
    List<DTTipoRegistro> tipos = (List<DTTipoRegistro>) request.getAttribute("tipos");
    @SuppressWarnings("unchecked")
    List<DTPatrocinio> patrocinios = (List<DTPatrocinio>) request.getAttribute("patrocinios");
    @SuppressWarnings("unchecked")
    List<DTRegistro> registros = (List<DTRegistro>) request.getAttribute("registros");
    boolean esSuOrganizador = (Boolean) request.getAttribute("esSuOrganizador");
    boolean puedeRegistrarse = (Boolean) request.getAttribute("puedeRegistrarse");
    String ctx = request.getContextPath();

    String nombreUrl = Texto.paraUrl(ed.getNombre());
    String estado = ed.getEstado().name().toLowerCase();
    String estadoTexto = estado.substring(0, 1).toUpperCase() + estado.substring(1);
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title><%= Texto.escapar(ed.getNombre()) %> · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container">
        <section class="ev-pagehead">
            <% if (ed.getNombreEvento() != null) { %>
            <a class="ev-back" href="<%= ctx %>/evento?nombre=<%= Texto.paraUrl(ed.getNombreEvento()) %>">&larr; <%= Texto.escapar(ed.getNombreEvento()) %></a>
            <% } else { %>
            <a class="ev-back" href="<%= ctx %>/eventos">&larr; Volver a la cartelera</a>
            <% } %>
        </section>
        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <div class="row g-4">
            <div class="col-12 col-lg-5">
                <%-- La letra pide la imagen "si tiene"; si no, el poster de color. --%>
                <% if (ed.getTieneImagen()) { %>
                <img class="img-fluid rounded"
                     src="<%= ctx %>/imagen?tipoEntidad=edicion&amp;clave=<%= nombreUrl %>"
                     alt="Imagen de la edición <%= Texto.escapar(ed.getNombre()) %>">
                <% } else { %>
                <div class="ev-poster ev-poster--paper" style="aspect-ratio:4/3;border-radius:4px;">
                    <div class="ev-poster-top"><span><%= Texto.escapar(ed.getCiudad()) %> · <%= Texto.fecha(ed.getFechaIni()) %></span></div>
                    <div class="ev-poster-title" style="font-size:2.4rem;"><%= Texto.escapar(ed.getNombre()) %></div>
                </div>
                <% } %>
            </div>

            <div class="col-12 col-lg-7">
                <p class="ev-kicker">Edición</p>
                <h1 class="ev-pagetitle mb-2"><%= Texto.escapar(ed.getNombre()) %></h1>
                <p class="mb-3"><span class="ev-estado ev-estado--<%= estado %>"><%= estadoTexto %></span></p>
                <dl class="ev-detail">
                    <dt>Ciudad</dt><dd><%= Texto.escapar(ed.getCiudad()) %></dd>
                    <dt>País</dt><dd><%= Texto.escapar(ed.getPais()) %></dd>
                    <dt>Sigla</dt><dd><%= Texto.escapar(ed.getSigla()) %></dd>
                    <dt>Inicio</dt><dd><%= Texto.fecha(ed.getFechaIni()) %></dd>
                    <dt>Fin</dt><dd><%= Texto.fecha(ed.getFechaFin()) %></dd>
                    <dt>Alta</dt><dd><%= Texto.fecha(ed.getFechaAlta()) %></dd>
                    <dt>Organizador</dt>
                    <dd><a href="<%= ctx %>/perfil?nickname=<%= Texto.paraUrl(ed.getOrganizador().getNickname()) %>"><%= Texto.escapar(ed.getOrganizador().getNombre()) %></a></dd>
                </dl>
                <% if (puedeRegistrarse) { %>
                <div class="d-flex gap-2 mt-3">
                    <a class="btn btn-accent" href="<%= ctx %>/registro?edicion=<%= nombreUrl %>">Registrarme</a>
                </div>
                <% } %>
            </div>
        </div>

        <div class="ev-section">Tipos de registro</div>
        <% if (tipos.isEmpty()) { %>
        <p class="text-secondary">Esta edición todavía no tiene tipos de registro.</p>
        <% } %>
        <% for (DTTipoRegistro t : tipos) { %>
        <a class="ev-row" href="<%= ctx %>/tipo-registro?edicion=<%= nombreUrl %>&amp;nombre=<%= Texto.paraUrl(t.getNombre()) %>">
            <span class="ev-rowname"><%= Texto.escapar(t.getNombre()) %></span>
            <span class="ev-rowmeta"><%= Texto.importe(t.getCosto()) %> · cupo <%= t.getCupo() %></span>
        </a>
        <% } %>
        <% if (esSuOrganizador) { %>
        <a class="ev-back d-inline-block mt-2" href="<%= ctx %>/tipo-registro/alta?edicion=<%= nombreUrl %>">+ Agregar tipo de registro</a>
        <% } %>

        <div class="ev-section">Patrocinios</div>
        <% if (patrocinios.isEmpty()) { %>
        <p class="text-secondary">Esta edición todavía no tiene patrocinios.</p>
        <% } %>
        <% for (DTPatrocinio p : patrocinios) { %>
        <a class="ev-row" href="<%= ctx %>/patrocinio?edicion=<%= nombreUrl %>&amp;institucion=<%= Texto.paraUrl(p.getInstitucion()) %>">
            <span class="ev-rowname"><%= Texto.escapar(p.getInstitucion()) %></span>
            <span class="ev-rowmeta"><%= Texto.escapar(p.getNivel()) %></span>
        </a>
        <% } %>
        <% if (esSuOrganizador) { %>
        <a class="ev-back d-inline-block mt-2" href="<%= ctx %>/patrocinio/alta?edicion=<%= nombreUrl %>">+ Agregar patrocinio</a>
        <% } %>

        <%-- Los registros solo le llegan a su organizador (requisito R14). --%>
        <% if (esSuOrganizador) { %>
        <div class="ev-section">Registros</div>
            <% if (registros.isEmpty()) { %>
        <p class="text-secondary">Todavía nadie se registró a esta edición.</p>
            <% } %>
            <% for (DTRegistro r : registros) { %>
        <a class="ev-row" href="<%= ctx %>/perfil?nickname=<%= Texto.paraUrl(r.getNickname()) %>">
            <span>
                <span class="ev-rowname">@<%= Texto.escapar(r.getNickname()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(r.getTipoRegistro()) %> · <%= Texto.fecha(r.getFechaRegistro()) %><%= r.getUsoCodigo() ? " · con código de patrocinio" : "" %></span>
            </span>
            <span class="ev-estado"><%= Texto.importe(r.getCosto()) %></span>
        </a>
            <% } %>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
