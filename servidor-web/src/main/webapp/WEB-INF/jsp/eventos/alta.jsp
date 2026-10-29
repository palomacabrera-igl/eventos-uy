<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="servlets.Texto" %>
<%!
    // Lo que la persona ya habia escrito, para no perderlo si hay un error.
    private static String previo(jakarta.servlet.jsp.PageContext pagina, String campo) {
        Object valor = pagina.getRequest().getAttribute(campo);
        return valor == null ? "" : Texto.escapar(valor.toString());
    }
%>
<%
    // Alta de Evento (CU 6): la pagina alta-evento.html de la Parte 1. Las
    // categorias las deja el servlet; la JSP no consulta nada.
    @SuppressWarnings("unchecked")
    List<String> categorias = (List<String>) request.getAttribute("categoriasDisponibles");
    @SuppressWarnings("unchecked")
    List<String> elegidas = (List<String>) request.getAttribute("categoriasElegidas");
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Alta de evento · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container">
        <section class="ev-pagehead">
            <p class="ev-kicker">Alta de evento</p>
            <h1 class="ev-pagetitle">Crear un evento</h1>
        </section>
        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <form class="ev-form" id="form-evento" method="post" action="<%= ctx %>/evento/alta" enctype="multipart/form-data">
            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre</label>
                <input class="form-control" id="nombre" name="nombre" type="text" required value="<%= previo(pageContext, "nombre") %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="descripcion">Descripción</label>
                <textarea class="form-control" id="descripcion" name="descripcion" rows="2" required><%= previo(pageContext, "descripcion") %></textarea>
            </div>
            <div class="mb-3">
                <label class="form-label" for="sigla">Sigla</label>
                <input class="form-control" id="sigla" name="sigla" type="text" required value="<%= previo(pageContext, "sigla") %>">
            </div>

            <fieldset class="mb-3">
                <legend class="form-label d-block">Categorías (al menos una)</legend>
                <div class="d-flex flex-wrap gap-3">
                    <% for (int i = 0; i < categorias.size(); i++) {
                           String c = categorias.get(i);
                           boolean marcada = elegidas != null && elegidas.contains(c); %>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="categorias" id="cat-<%= i %>"
                               value="<%= Texto.escapar(c) %>"<%= marcada ? " checked" : "" %>>
                        <label class="form-check-label" for="cat-<%= i %>"><%= Texto.escapar(c) %></label>
                    </div>
                    <% } %>
                </div>
                <div class="form-text text-danger d-none" id="error-categorias">Elegí al menos una categoría para el evento.</div>
            </fieldset>

            <div class="mb-3">
                <label class="form-label" for="imagen">Imagen (opcional)</label>
                <input class="form-control" id="imagen" name="imagen" type="file" accept="image/jpeg,image/png,image/webp">
                <div class="form-text">JPEG, PNG o WebP; máximo 5 MB.</div>
            </div>

            <p class="form-text">La fecha de alta es la de hoy: la pone el sistema.</p>

            <div class="d-flex gap-2 mt-4">
                <button class="btn btn-accent" type="submit">Crear evento</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
        </form>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
    <script>
        // Solo ayuda en el navegador, como en la Parte 1: avisa si no se eligio
        // ninguna categoria. La regla de verdad la vuelve a controlar la logica.
        document.getElementById("form-evento").addEventListener("submit", function (ev) {
            const alguna = document.querySelector('input[name="categorias"]:checked');
            document.getElementById("error-categorias").classList.toggle("d-none", alguna !== null);
            if (!alguna) { ev.preventDefault(); }
        });
    </script>
</body>
</html>
