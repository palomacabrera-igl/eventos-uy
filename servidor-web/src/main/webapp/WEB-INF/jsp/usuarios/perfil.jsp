<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTAsistente" %>
<%@ page import="logica.DTEdicionEvento" %>
<%@ page import="logica.DTOrganizador" %>
<%@ page import="logica.DTRegistro" %>
<%@ page import="logica.DTUsuario" %>
<%@ page import="servlets.Texto" %>
<%
    // El perfil de la Parte 1 (perfil.html), ahora con los datos de la base.
    //
    // Lo que llega ya viene filtrado por la logica: las ediciones que no
    // corresponde mostrar no estan en la lista, y los registros llegan vacios
    // cuando el perfil no es propio. La JSP no esconde nada, solo dibuja.
    DTUsuario perfil = (DTUsuario) request.getAttribute("perfil");
    boolean esPropio = Boolean.TRUE.equals(request.getAttribute("esPropio"));
    String ctx = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<DTEdicionEvento> ediciones = (List<DTEdicionEvento>) request.getAttribute("ediciones");
    @SuppressWarnings("unchecked")
    List<DTRegistro> registros = (List<DTRegistro>) request.getAttribute("registros");

    // De donde se llego (ver Perfil.java). Vacio = se entro de otro lado,
    // por ejemplo desde "Mi perfil" del cabezal, y se vuelve a la cartelera.
    String desdeEdicion = (String) request.getAttribute("desdeEdicion");
    String desdeRegistro = (String) request.getAttribute("desdeRegistro");

    DTAsistente asistente = (perfil instanceof DTAsistente a) ? a : null;
    DTOrganizador organizador = (perfil instanceof DTOrganizador o) ? o : null;

    String nombreCompleto = asistente == null
            ? perfil.getNombre()
            : perfil.getNombre() + " " + asistente.getApellido();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title><%= Texto.escapar(nombreCompleto) %> · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <% if (desdeRegistro != null && !desdeRegistro.isEmpty()) { %>
            <a class="ev-back" href="<%= ctx %>/registro?edicion=<%= Texto.paraUrl(desdeRegistro) %>&amp;asistente=<%= Texto.paraUrl(perfil.getNickname()) %>">&larr; Registro de @<%= Texto.escapar(perfil.getNickname()) %></a>
            <% } else if (desdeEdicion != null && !desdeEdicion.isEmpty()) { %>
            <a class="ev-back" href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(desdeEdicion) %>">&larr; <%= Texto.escapar(desdeEdicion) %></a>
            <% } else { %>
            <a class="ev-back" href="<%= ctx %>/eventos">&larr; Volver a la cartelera</a>
            <% } %>
        </section>

        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <div class="d-flex align-items-center gap-3 flex-wrap mb-4">
            <%-- La imagen la sirve el servlet /imagen; si no tiene, va la inicial. --%>
            <% if (perfil.getTieneImagen()) { %>
            <img class="ev-ava" alt=""
                 src="<%= ctx %>/imagen?tipoEntidad=usuario&amp;clave=<%= Texto.paraUrl(perfil.getNickname()) %>">
            <% } else { %>
            <div class="ev-ava"><%= Texto.escapar(perfil.getNombre().substring(0, 1).toUpperCase()) %></div>
            <% } %>
            <div>
                <h1 class="ev-pagetitle mb-1"><%= Texto.escapar(nombreCompleto) %></h1>
                <div class="ev-meta">
                    <%= perfil.esOrganizador() ? "Organizador" : "Asistente" %>
                    · <%= Texto.escapar(perfil.getNickname()) %>
                </div>
            </div>
            <% if (esPropio) { %>
            <a class="btn btn-outline-secondary btn-sm ms-auto" href="<%= ctx %>/perfil/editar">Editar perfil</a>
            <% } %>
        </div>

        <div class="ev-section">Datos</div>
        <dl class="ev-detail mb-4">
            <dt>Nickname</dt>
            <dd><%= Texto.escapar(perfil.getNickname()) %></dd>

            <dt>Correo electrónico</dt>
            <dd><%= Texto.escapar(perfil.getCorreo()) %></dd>

            <% if (asistente != null) { %>
            <dt>Apellido</dt>
            <dd><%= Texto.escapar(asistente.getApellido()) %></dd>

            <dt>Fecha de nacimiento</dt>
            <dd><%= Texto.fecha(asistente.getFechaNacimiento()) %></dd>

            <dt>Institución</dt>
            <dd><%= asistente.getInstitucion() == null
                        ? "Sin institución"
                        : Texto.escapar(asistente.getInstitucion()) %></dd>
            <% } else { %>
            <dt>Descripción</dt>
            <dd><%= Texto.escapar(organizador.getDescripcion()) %></dd>

            <dt>Sitio web</dt>
            <dd>
                <% if (organizador.getSitioWeb() == null) { %>
                    —
                <% } else { %>
                <a href="<%= Texto.escapar(organizador.getSitioWeb()) %>" rel="noopener noreferrer" target="_blank">
                    <%= Texto.escapar(organizador.getSitioWeb()) %>
                </a>
                <% } %>
            </dd>
            <% } %>
        </dl>

        <%-- ---------- Ediciones, cuando el perfil es de un organizador ---------- --%>
        <% if (ediciones != null) { %>
        <div class="ev-section">Ediciones</div>
        <% if (esPropio) { %>
        <p class="ev-fnote">En tu propio perfil ves también las ediciones ingresadas y las rechazadas.</p>
        <% } %>

        <% if (ediciones.isEmpty()) { %>
        <p class="text-secondary">
            <%= esPropio ? "Todavía no creaste ninguna edición."
                         : "Este organizador todavía no tiene ediciones publicadas." %>
        </p>
        <% } else {
               for (DTEdicionEvento ed : ediciones) {
                   String estado = ed.getEstado().name().toLowerCase();
        %>
        <a class="ev-row" href="<%= ctx %>/edicion?nombre=<%= Texto.paraUrl(ed.getNombre()) %>">
            <span>
                <span class="ev-rowname"><%= Texto.escapar(ed.getNombre()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(ed.getCiudad()) %> · <%= Texto.fecha(ed.getFechaInicio()) %></span>
            </span>
            <span class="ev-estado ev-estado--<%= estado %>"><%= Texto.escapar(ed.getEstado().name().charAt(0)
                    + ed.getEstado().name().substring(1).toLowerCase()) %></span>
        </a>
        <%     }
           } %>
        <% } %>

        <%-- ---------- Registros, solo en el perfil propio de un asistente ---------- --%>
        <% if (registros != null) { %>
        <div class="ev-section">Registros</div>
        <% if (!esPropio) { %>
        <p class="text-secondary">Los registros de una persona solo los ve ella.</p>
        <% } else if (registros.isEmpty()) { %>
        <p class="text-secondary">Todavía no te registraste a ninguna edición.</p>
        <% } else {
               for (DTRegistro reg : registros) { %>
        <a class="ev-row" href="<%= ctx %>/registro?edicion=<%= Texto.paraUrl(reg.getNombreEdicion()) %>&amp;asistente=<%= Texto.paraUrl(reg.getNickname()) %>">
            <span>
                <span class="ev-rowname"><%= Texto.escapar(reg.getNombreEdicion()) %></span><br>
                <span class="ev-rowmeta">
                    <%= Texto.escapar(reg.getTipoRegistro()) %>
                    · <%= Texto.fecha(reg.getFechaRegistro()) %>
                    <% if (reg.getUsoCodigo()) { %> · con código de patrocinio<% } %>
                </span>
            </span>
            <span class="ev-estado"><%= Texto.importe(reg.getCosto()) %></span>
        </a>
        <%     }
           } %>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
