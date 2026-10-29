<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTEdicionEvento" %>
<%@ page import="logica.DTEvento" %>
<%@ page import="logica.DTTipoRegistro" %>
<%@ page import="servlets.Texto" %>
<%!
    /** Lo que se eligio en el intento anterior, o vacio la primera vez. */
    private static String previo(jakarta.servlet.jsp.PageContext pagina, String campo) {
        Object valor = pagina.getRequest().getAttribute(campo);
        return valor == null ? "" : Texto.escapar(valor.toString());
    }
%>
<%
    // El registro a edicion de la Parte 1 (registro-edicion.html). Alla los
    // tres desplegables se llenaban con JavaScript; aca cada paso es una
    // pagina y el servlet le pasa a cada una solo lo que le toca.
    String paso = (String) request.getAttribute("paso");
    String ctx = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<DTEvento> eventos = (List<DTEvento>) request.getAttribute("eventos");
    @SuppressWarnings("unchecked")
    List<DTEdicionEvento> ediciones = (List<DTEdicionEvento>) request.getAttribute("ediciones");
    @SuppressWarnings("unchecked")
    List<DTTipoRegistro> tipos = (List<DTTipoRegistro>) request.getAttribute("tipos");

    String evento = (String) request.getAttribute("evento");
    String edicion = (String) request.getAttribute("edicion");
    String institucion = (String) request.getAttribute("institucion");
    boolean puedeUsarCodigo = institucion != null && !institucion.isBlank();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Registro a edición · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <% if ("datos".equals(paso)) { %>
            <a class="ev-back" href="<%= ctx %>/registro/alta">&larr; Elegir otra edición</a>
            <% } else if ("edicion".equals(paso)) { %>
            <a class="ev-back" href="<%= ctx %>/registro/alta">&larr; Elegir otro evento</a>
            <% } %>
            <p class="ev-kicker">Registro a edición</p>
            <h1 class="ev-pagetitle">
                <% if ("datos".equals(paso)) { %>
                    <%= Texto.escapar(edicion) %>
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
        <p class="text-secondary">Todavía no hay eventos publicados.</p>
            <% } else {
                   for (DTEvento e : eventos) { %>
        <a class="ev-row" href="<%= ctx %>/registro/alta?evento=<%= Texto.paraUrl(e.getNombre()) %>">
            <span>
                <span class="ev-rowname"><%= Texto.escapar(e.getNombre()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(e.getDescripcion()) %></span>
            </span>
        </a>
            <%     }
               } %>

        <%-- --------------------------- Paso 2: la edición ---------------------------- --%>
        <% } else if ("edicion".equals(paso)) { %>
            <% if (ediciones.isEmpty()) { %>
        <p class="text-secondary">
            Este evento todavía no tiene ediciones publicadas a las que registrarse.
        </p>
            <% } else { %>
        <p class="ev-fnote">Solo se puede uno registrar a las ediciones aceptadas.</p>
                <% for (DTEdicionEvento ed : ediciones) { %>
        <a class="ev-row" href="<%= ctx %>/registro/alta?edicion=<%= Texto.paraUrl(ed.getNombre()) %>">
            <span>
                <span class="ev-rowname"><%= Texto.escapar(ed.getNombre()) %></span><br>
                <span class="ev-rowmeta"><%= Texto.escapar(ed.getCiudad()) %> · <%= Texto.fecha(ed.getFechaInicio()) %></span>
            </span>
        </a>
                <% } %>
            <% } %>

        <%-- ---------------------------- Paso 3: los datos ---------------------------- --%>
        <% } else { %>
            <% if (tipos.isEmpty()) { %>
        <p class="text-secondary">Esta edición todavía no tiene tipos de registro.</p>
            <% } else { %>
        <form class="ev-form" method="post" action="<%= ctx %>/registro/alta">
            <input type="hidden" name="edicion" value="<%= Texto.escapar(edicion) %>">

            <div class="mb-3">
                <label class="form-label" for="tipo">Tipo de registro</label>
                <select class="form-select" id="tipo" name="tipo" required>
                    <option value="">Elegí un tipo de registro</option>
                    <% String tipoElegido = previo(pageContext, "tipo");
                       for (DTTipoRegistro t : tipos) {
                           String nombre = Texto.escapar(t.getNombre()); %>
                    <option value="<%= nombre %>" <%= nombre.equals(tipoElegido) ? "selected" : "" %>>
                        <%= nombre %> — <%= Texto.importe(t.getCosto()) %> · <%= t.getCupo() %> lugares
                    </option>
                    <% } %>
                </select>
            </div>

            <% if (puedeUsarCodigo) {
                   boolean conCodigo = "codigo".equals(previo(pageContext, "modo")); %>
            <fieldset class="mb-3">
                <legend class="form-label d-block">Tipo de inscripción</legend>
                <div class="d-flex gap-4">
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="modo" id="mGeneral"
                               value="general" <%= conCodigo ? "" : "checked" %>>
                        <label class="form-check-label" for="mGeneral">General</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="modo" id="mCodigo"
                               value="codigo" <%= conCodigo ? "checked" : "" %>>
                        <label class="form-check-label" for="mCodigo">Con código de patrocinio</label>
                    </div>
                </div>
            </fieldset>

            <fieldset class="mb-3" id="boxCodigo" <%= conCodigo ? "" : "hidden disabled" %>>
                <label class="form-label" for="codigo">Código de patrocinio</label>
                <input class="form-control" id="codigo" name="codigo" type="text" required
                       value="<%= previo(pageContext, "codigo") %>">
                <p class="form-text">
                    Con un código válido de <b><%= Texto.escapar(institucion) %></b>,
                    tu institución, el registro no tiene costo.
                </p>
            </fieldset>
            <% } else { %>
            <%-- Sin institucion no se puede usar codigo (letra, CU 12), asi que
                 ni se ofrece la opcion: el formulario manda siempre "general". --%>
            <input type="hidden" name="modo" value="general">
            <p class="form-text mb-3">
                Como no pertenecés a ninguna institución, tu registro es general.
                Podés sumarte a una desde <a href="<%= ctx %>/perfil/editar">tu perfil</a>.
            </p>
            <% } %>

            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-accent" type="submit">Registrarme</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
        </form>

        <% if (puedeUsarCodigo) { %>
        <%-- Presentacion, no una regla: muestra el campo del codigo solo si
             se eligio esa opcion. La validacion esta en la logica. --%>
        <script>
            (function () {
                var caja = document.getElementById("boxCodigo");
                var general = document.getElementById("mGeneral");
                var codigo = document.getElementById("mCodigo");

                function mostrar() {
                    var usa = codigo.checked;
                    caja.hidden = !usa;
                    caja.disabled = !usa;
                }

                general.addEventListener("change", mostrar);
                codigo.addEventListener("change", mostrar);
                mostrar();
            })();
        </script>
        <% } %>
            <% } %>
        <% } %>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
