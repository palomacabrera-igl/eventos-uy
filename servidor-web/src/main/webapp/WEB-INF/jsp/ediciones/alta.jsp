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
    // Alta de Edicion (CU 8): la pagina alta-edicion.html de la Parte 1. Los
    // eventos del desplegable los deja el servlet; la JSP no consulta nada.
    @SuppressWarnings("unchecked")
    List<String> eventos = (List<String>) request.getAttribute("eventos");
    String eventoElegido = (String) request.getAttribute("evento");
    String pais = previo(pageContext, "pais");
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Alta de edición · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container">
        <section class="ev-pagehead">
            <p class="ev-kicker">Alta de edición</p>
            <h1 class="ev-pagetitle">Crear una edición</h1>
        </section>
        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <form class="ev-form" method="post" action="<%= ctx %>/edicion/alta" enctype="multipart/form-data">
            <div class="mb-3">
                <label class="form-label" for="evento">Evento</label>
                <select class="form-select" id="evento" name="evento" required>
                    <option value="">Elegí un evento</option>
                    <% for (String e : eventos) { %>
                    <option value="<%= Texto.escapar(e) %>"<%= e.equals(eventoElegido) ? " selected" : "" %>><%= Texto.escapar(e) %></option>
                    <% } %>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre de la edición</label>
                <input class="form-control" id="nombre" name="nombre" type="text" required value="<%= previo(pageContext, "nombre") %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="sigla">Sigla</label>
                <input class="form-control" id="sigla" name="sigla" type="text" required value="<%= previo(pageContext, "sigla") %>">
            </div>
            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="ciudad">Ciudad</label>
                    <input class="form-control" id="ciudad" name="ciudad" type="text" required value="<%= previo(pageContext, "ciudad") %>">
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="pais">País</label>
                    <input class="form-control" id="pais" name="pais" type="text" required value="<%= pais.isEmpty() ? "Uruguay" : pais %>">
                </div>
            </div>
            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="ini">Fecha de inicio</label>
                    <input class="form-control" id="ini" name="ini" type="date" required value="<%= previo(pageContext, "ini") %>">
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="fin">Fecha de fin</label>
                    <input class="form-control" id="fin" name="fin" type="date" required value="<%= previo(pageContext, "fin") %>">
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="imagen">Imagen (opcional)</label>
                <input class="form-control" id="imagen" name="imagen" type="file" accept="image/jpeg,image/png,image/webp">
                <div class="form-text">JPEG, PNG o WebP; máximo 5 MB.</div>
            </div>

            <p class="form-text">La edición queda en estado <b>Ingresada</b> hasta que el administrador la acepte o rechace.</p>

            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-accent" type="submit">Crear edición</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
        </form>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
    <script>
        // Solo ayuda en el navegador, como en la Parte 1: no deja elegir un fin
        // anterior al inicio. La regla de verdad la vuelve a controlar la logica.
        const ini = document.getElementById("ini");
        const fin = document.getElementById("fin");
        function ajustarFin() {
            fin.min = ini.value;
            if (fin.value && fin.value < ini.value) { fin.value = ""; }
        }
        ini.addEventListener("change", ajustarFin);
        if (ini.value) { fin.min = ini.value; }
    </script>
</body>
</html>
