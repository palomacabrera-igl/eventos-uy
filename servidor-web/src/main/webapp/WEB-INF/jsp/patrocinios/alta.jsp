<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEdicionEvento" %>
<%@ page import="logica.DTTipoRegistro" %>
<%@ page import="servlets.Texto" %>
<%!
    /** Lo que se escribio en el intento anterior, o vacio la primera vez. */
    private static String previo(jakarta.servlet.jsp.PageContext pagina, String campo) {
        Object valor = pagina.getRequest().getAttribute(campo);
        return valor == null ? "" : Texto.escapar(valor.toString());
    }
%>
<%
    // El alta de patrocinio de la Parte 1 (alta-patrocinio.html). Alla los tres
    // desplegables se llenaban con JavaScript a medida que se elegia; aca cada
    // paso es una pagina, y el servlet le pasa a cada una solo lo que le toca.
    String paso = (String) request.getAttribute("paso");
    String ctx = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<String> eventos = (List<String>) request.getAttribute("eventos");
    @SuppressWarnings("unchecked")
    List<DTEdicionEvento> ediciones = (List<DTEdicionEvento>) request.getAttribute("ediciones");
    @SuppressWarnings("unchecked")
    List<DTTipoRegistro> tipos = (List<DTTipoRegistro>) request.getAttribute("tipos");
    @SuppressWarnings("unchecked")
    List<String> instituciones = (List<String>) request.getAttribute("instituciones");

    DTEdicionEvento edicion = (DTEdicionEvento) request.getAttribute("edicion");
    String evento = (String) request.getAttribute("evento");
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Alta de patrocinio · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <% if ("datos".equals(paso)) { %>
            <a class="ev-back" href="<%= ctx %>/patrocinio/alta?evento=<%= Texto.paraUrl(edicion.getNombreEvento()) %>">&larr; Elegir otra edición</a>
            <% } else if ("edicion".equals(paso)) { %>
            <a class="ev-back" href="<%= ctx %>/patrocinio/alta">&larr; Elegir otro evento</a>
            <% } %>
            <p class="ev-kicker">Alta de patrocinio</p>
            <h1 class="ev-pagetitle">
                <% if ("datos".equals(paso)) { %>
                    <%= Texto.escapar(edicion.getNombre()) %>
                <% } else if ("edicion".equals(paso)) { %>
                    Elegí una edición de <%= Texto.escapar(evento) %>
                <% } else { %>
                    Elegí un evento
                <% } %>
            </h1>
        </section>

        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <%-- ---------------------------- Paso 1: el evento ---------------------------- --%>
        <% if ("evento".equals(paso)) { %>
            <% if (eventos.isEmpty()) { %>
        <p class="text-secondary">
            Todavía no organizás ninguna edición, así que no hay nada para patrocinar.
        </p>
            <% } else { %>
        <p class="ev-fnote">Se muestran los eventos donde organizás alguna edición.</p>
                <% for (String nombre : eventos) { %>
        <a class="ev-row" href="<%= ctx %>/patrocinio/alta?evento=<%= Texto.paraUrl(nombre) %>">
            <span class="ev-rowname"><%= Texto.escapar(nombre) %></span>
        </a>
                <% } %>
            <% } %>

        <%-- --------------------------- Paso 2: la edición ---------------------------- --%>
        <% } else if ("edicion".equals(paso)) { %>
            <% if (ediciones.isEmpty()) { %>
        <p class="text-secondary">No organizás ninguna edición de este evento.</p>
            <% } else { %>
        <p class="ev-fnote">Se puede patrocinar una edición en cualquier estado.</p>
                <% for (DTEdicionEvento ed : ediciones) {
                       String estado = ed.getEstado().name().toLowerCase(); %>
        <a class="ev-row" href="<%= ctx %>/patrocinio/alta?edicion=<%= Texto.paraUrl(ed.getNombre()) %>">
            <span>
                <span class="ev-rowname"><%= Texto.escapar(ed.getNombre()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(ed.getCiudad()) %> · <%= Texto.fecha(ed.getFechaInicio()) %></span>
            </span>
            <span class="ev-estado ev-estado--<%= estado %>"><%= Texto.escapar(ed.getEstado().name()) %></span>
        </a>
                <% } %>
            <% } %>

        <%-- ---------------------------- Paso 3: los datos ---------------------------- --%>
        <% } else { %>
            <% if (tipos.isEmpty()) { %>
        <p class="text-secondary">
            Esta edición todavía no tiene tipos de registro, y un patrocinio regala
            registros de alguno. Creá primero un tipo de registro.
        </p>
            <% } else if (instituciones.isEmpty()) { %>
        <p class="text-secondary">Todavía no hay instituciones registradas.</p>
            <% } else { %>
        <form class="ev-form" method="post" action="<%= ctx %>/patrocinio/alta">
            <%-- La edicion no es un campo que se elija: ya viene decidida del paso
                 anterior, asi que viaja escondida y la pagina la muestra en el titulo. --%>
            <input type="hidden" name="edicion" value="<%= Texto.escapar(edicion.getNombre()) %>">

            <div class="mb-3">
                <label class="form-label" for="institucion">Institución</label>
                <select class="form-select" id="institucion" name="institucion" required>
                    <option value="">Elegí una institución</option>
                    <% String instElegida = previo(pageContext, "institucion");
                       for (String i : instituciones) {
                           String nombre = Texto.escapar(i); %>
                    <option value="<%= nombre %>" <%= nombre.equals(instElegida) ? "selected" : "" %>><%= nombre %></option>
                    <% } %>
                </select>
            </div>

            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="nivel">Nivel de patrocinio</label>
                    <% String nivelElegido = previo(pageContext, "nivel"); %>
                    <select class="form-select" id="nivel" name="nivel" required>
                        <option value="">Elegí un nivel</option>
                        <option value="PLATINO" <%= "PLATINO".equals(nivelElegido) ? "selected" : "" %>>Platino</option>
                        <option value="ORO" <%= "ORO".equals(nivelElegido) ? "selected" : "" %>>Oro</option>
                        <option value="PLATA" <%= "PLATA".equals(nivelElegido) ? "selected" : "" %>>Plata</option>
                        <option value="BRONCE" <%= "BRONCE".equals(nivelElegido) ? "selected" : "" %>>Bronce</option>
                    </select>
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="monto">Aporte económico</label>
                    <input class="form-control" id="monto" name="monto" type="number"
                           min="1" step="0.01" required
                           value="<%= previo(pageContext, "monto") %>">
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label" for="tipo">Tipo de registro (gratuito)</label>
                <select class="form-select" id="tipo" name="tipo" required>
                    <option value="">Elegí un tipo de registro</option>
                    <% String tipoElegido = previo(pageContext, "tipo");
                       for (DTTipoRegistro t : tipos) {
                           String nombre = Texto.escapar(t.getNombre()); %>
                    <option value="<%= nombre %>" <%= nombre.equals(tipoElegido) ? "selected" : "" %>>
                        <%= nombre %> — <%= Texto.importe(t.getCosto()) %>
                    </option>
                    <% } %>
                </select>
            </div>

            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="gratis">Cantidad de registros gratuitos</label>
                    <input class="form-control" id="gratis" name="gratis" type="number"
                           min="0" step="1" required
                           value="<%= previo(pageContext, "gratis") %>">
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="codigo">Código de patrocinio</label>
                    <input class="form-control" id="codigo" name="codigo" type="text" required
                           value="<%= previo(pageContext, "codigo") %>">
                </div>
            </div>

            <p class="form-text">
                El costo de los registros gratuitos no puede superar el 20 % del aporte,
                y el código tiene que ser único en la plataforma.
            </p>

            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-accent" type="submit">Crear patrocinio</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
        </form>
            <% } %>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
