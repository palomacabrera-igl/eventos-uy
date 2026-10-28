<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="servlets.Texto" %>
<%!
    private static String previo(jakarta.servlet.jsp.PageContext pagina, String campo) {
        Object valor = pagina.getRequest().getAttribute(campo);
        return valor == null ? "" : Texto.escapar(valor.toString());
    }
%>
<%
    String ctx = request.getContextPath();
    String creada = request.getParameter("creada");
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Alta de institucion · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <p class="ev-kicker">Alta de institucion</p>
            <h1 class="ev-pagetitle">Crear una institucion</h1>
        </section>

        <% if (creada != null && !creada.isBlank()) { %>
        <div class="alert alert-success" role="alert">
            La institucion <strong><%= Texto.escapar(creada) %></strong> fue creada correctamente.
        </div>
        <% } %>
        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <form class="ev-form" method="post" action="<%= ctx %>/institucion/alta"
              enctype="multipart/form-data">
            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre</label>
                <input class="form-control" id="nombre" name="nombre" type="text" required
                       value="<%= previo(pageContext, "nombre") %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="descripcion">Descripcion</label>
                <textarea class="form-control" id="descripcion" name="descripcion" rows="2" required><%= previo(pageContext, "descripcion") %></textarea>
            </div>
            <div class="mb-3">
                <label class="form-label" for="sitioWeb">Sitio web</label>
                <input class="form-control" id="sitioWeb" name="sitioWeb" type="url" placeholder="https://" required
                       value="<%= previo(pageContext, "sitioWeb") %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="imagen">Imagen (opcional)</label>
                <input class="form-control" id="imagen" name="imagen" type="file"
                       accept="image/jpeg,image/png,image/webp">
                <div class="form-text">JPEG, PNG o WebP; maximo 5 MB.</div>
            </div>
            <div class="d-flex gap-2 mt-4">
                <button class="btn btn-accent" type="submit">Crear institucion</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
        </form>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
