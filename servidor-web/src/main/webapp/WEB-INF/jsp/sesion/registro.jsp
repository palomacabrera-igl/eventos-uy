<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="servlets.Texto" %>
<%!
    /** Lo que se escribio antes, listo para el HTML, o vacio la primera vez. */
    private static String previo(jakarta.servlet.jsp.PageContext pagina, String campo) {
        Object valor = pagina.getRequest().getAttribute(campo);
        return valor == null ? "" : Texto.escapar(valor.toString());
    }
%>
<%
    // El alta de usuario de la Parte 1 (alta-usuario.html), ahora contra la base.
    // Todo lo escrito vuelve desde el servlet cuando algo falla, asi no hay que
    // escribirlo de nuevo. Las dos contrasenas no vuelven nunca.
    @SuppressWarnings("unchecked")
    List<String> instituciones = (List<String>) request.getAttribute("instituciones");
    String ctx = request.getContextPath();

    boolean esOrganizador = "organizador".equals(request.getAttribute("tipo"));
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Alta de usuario · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <p class="ev-kicker">Alta de usuario</p>
            <h1 class="ev-pagetitle">Creá tu cuenta</h1>
        </section>

        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <%-- enctype multipart porque lleva un archivo: sin eso la imagen no viaja. --%>
        <form class="ev-form" method="post" action="<%= ctx %>/registrarse"
              enctype="multipart/form-data">

            <fieldset class="mb-3">
                <legend class="form-label d-block">Me registro como</legend>
                <div class="d-flex gap-4">
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="tipo" id="tAsis"
                               value="asistente" <%= esOrganizador ? "" : "checked" %>>
                        <label class="form-check-label" for="tAsis">Asistente</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="tipo" id="tOrg"
                               value="organizador" <%= esOrganizador ? "checked" : "" %>>
                        <label class="form-check-label" for="tOrg">Organizador</label>
                    </div>
                </div>
            </fieldset>

            <div class="mb-3">
                <label class="form-label" for="nick">Nickname</label>
                <input class="form-control" id="nick" name="nick" type="text"
                       autocomplete="username" required
                       value="<%= previo(pageContext, "nick") %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre</label>
                <input class="form-control" id="nombre" name="nombre" type="text" required
                       value="<%= previo(pageContext, "nombre") %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="correo">Correo electrónico</label>
                <input class="form-control" id="correo" name="correo" type="email"
                       autocomplete="email" required
                       value="<%= previo(pageContext, "correo") %>">
            </div>
            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="pass">Contraseña</label>
                    <input class="form-control" id="pass" name="pass" type="password"
                           autocomplete="new-password" required>
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="pass2">Confirmar contraseña</label>
                    <input class="form-control" id="pass2" name="pass2" type="password"
                           autocomplete="new-password" required>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="imagen">Imagen (opcional)</label>
                <input class="form-control" id="imagen" name="imagen" type="file" accept="image/*">
            </div>

            <fieldset id="campos-asistente" <%= esOrganizador ? "hidden disabled" : "" %>>
                <div class="ev-section">Datos de asistente</div>
                <div class="mb-3">
                    <label class="form-label" for="apellido">Apellido</label>
                    <input class="form-control" id="apellido" name="apellido" type="text" required
                           value="<%= previo(pageContext, "apellido") %>">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="fnac">Fecha de nacimiento</label>
                    <input class="form-control" id="fnac" name="fnac" type="date" required
                           value="<%= previo(pageContext, "fnac") %>">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="inst">Institución (opcional)</label>
                    <select class="form-select" id="inst" name="inst">
                        <option value="">Sin institución</option>
                        <%
                            String elegida = previo(pageContext, "inst");
                            if (instituciones != null) {
                                for (String nombreInstitucion : instituciones) {
                                    String nombre = Texto.escapar(nombreInstitucion);
                        %>
                        <option value="<%= nombre %>" <%= nombre.equals(elegida) ? "selected" : "" %>><%= nombre %></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>
            </fieldset>

            <fieldset id="campos-organizador" <%= esOrganizador ? "" : "hidden disabled" %>>
                <div class="ev-section">Datos de organizador</div>
                <div class="mb-3">
                    <label class="form-label" for="desc">Descripción</label>
                    <textarea class="form-control" id="desc" name="desc" rows="2" required><%= previo(pageContext, "desc") %></textarea>
                </div>
                <div class="mb-3">
                    <label class="form-label" for="web">Sitio web (opcional)</label>
                    <input class="form-control" id="web" name="web" type="url" placeholder="https://"
                           value="<%= previo(pageContext, "web") %>">
                </div>
            </fieldset>

            <div class="d-flex gap-2 mt-4">
                <button class="btn btn-accent" type="submit">Crear cuenta</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
            <p class="form-text mt-3">
                ¿Ya tenés cuenta? <a href="<%= ctx %>/iniciar-sesion">Iniciá sesión</a>.
            </p>
        </form>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />

    <%-- Esto es presentacion, no una regla: muestra los campos del tipo elegido.
         "disabled" ademas los saca del envio, asi un "required" escondido no
         frena el formulario sin decir por que. La validacion de verdad esta en
         la logica, que es por donde pasa todo el mundo. --%>
    <script>
        (function () {
            var asistente = document.getElementById("campos-asistente");
            var organizador = document.getElementById("campos-organizador");

            function mostrar() {
                var esOrg = document.getElementById("tOrg").checked;
                asistente.hidden = esOrg;
                asistente.disabled = esOrg;
                organizador.hidden = !esOrg;
                organizador.disabled = !esOrg;
            }

            document.getElementById("tAsis").addEventListener("change", mostrar);
            document.getElementById("tOrg").addEventListener("change", mostrar);
            mostrar();
        })();
    </script>
</body>
</html>
