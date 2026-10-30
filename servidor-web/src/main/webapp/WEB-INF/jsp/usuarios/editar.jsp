<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="logica.DTAsistente" %>
<%@ page import="logica.DTOrganizador" %>
<%@ page import="logica.DTUsuario" %>
<%@ page import="servlets.Texto" %>
<%!
    /**
     * Lo que se escribio en el intento anterior, o el dato que hay guardado la
     * primera vez. Asi un error no borra lo que la persona venia tipeando.
     */
    private static String valor(jakarta.servlet.jsp.PageContext pagina,
                                String campo, String guardado) {
        Object escrito = pagina.getRequest().getAttribute(campo);
        return Texto.escapar(escrito != null ? escrito.toString()
                                             : (guardado == null ? "" : guardado));
    }
%>
<%
    // El formulario de edicion que en la Parte 1 estaba escondido dentro de
    // perfil.html. Ahora es una pagina propia: el nickname y el correo se
    // muestran pero no se pueden cambiar, como dice la letra.
    DTUsuario perfil = (DTUsuario) request.getAttribute("perfil");
    String ctx = request.getContextPath();

    @SuppressWarnings("unchecked")
    List<String> instituciones = (List<String>) request.getAttribute("instituciones");

    DTAsistente asistente = (perfil instanceof DTAsistente a) ? a : null;
    DTOrganizador organizador = (perfil instanceof DTOrganizador o) ? o : null;
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Editar perfil · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <a class="ev-back" href="<%= ctx %>/perfil">&larr; Volver a mi perfil</a>
            <h1 class="ev-pagetitle">Editar perfil</h1>
        </section>

        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <form class="ev-form" method="post" action="<%= ctx %>/perfil/editar"
              enctype="multipart/form-data">

            <p class="form-text mb-3">El nickname y el correo electrónico no se pueden modificar.</p>

            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="eNick">Nickname</label>
                    <input class="form-control" id="eNick" type="text" readonly
                           value="<%= Texto.escapar(perfil.getNickname()) %>">
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="eCorreo">Correo electrónico</label>
                    <input class="form-control" id="eCorreo" type="email" readonly
                           value="<%= Texto.escapar(perfil.getCorreo()) %>">
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre</label>
                <input class="form-control" id="nombre" name="nombre" type="text" required
                       value="<%= valor(pageContext, "nombre", perfil.getNombre()) %>">
            </div>

            <% if (asistente != null) { %>
            <div class="mb-3">
                <label class="form-label" for="apellido">Apellido</label>
                <input class="form-control" id="apellido" name="apellido" type="text" required
                       value="<%= valor(pageContext, "apellido", asistente.getApellido()) %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="fnac">Fecha de nacimiento</label>
                <%-- El input type=date quiere aaaa-mm-dd, no el formato de pantalla. --%>
                <input class="form-control" id="fnac" name="fnac" type="date" required
                           max="<%= java.time.LocalDate.now() %>"
                       value="<%= valor(pageContext, "fnac",
                               String.format("%04d-%02d-%02d",
                                       asistente.getFechaNacimiento().getAnio(),
                                       asistente.getFechaNacimiento().getMes(),
                                       asistente.getFechaNacimiento().getDia())) %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="inst">Institución (opcional)</label>
                <select class="form-select" id="inst" name="inst">
                    <option value="">Sin institución</option>
                    <%
                        String elegida = valor(pageContext, "inst", asistente.getInstitucion());
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
            <% } else { %>
            <div class="mb-3">
                <label class="form-label" for="desc">Descripción</label>
                <textarea class="form-control" id="desc" name="desc" rows="2" required><%= valor(pageContext, "desc", organizador.getDescripcion()) %></textarea>
            </div>
            <div class="mb-3">
                <label class="form-label" for="web">Sitio web (puede quedar vacío)</label>
                <input class="form-control" id="web" name="web" type="url" placeholder="https://"
                       value="<%= valor(pageContext, "web", organizador.getSitioWeb()) %>">
            </div>
            <% } %>

            <div class="ev-section">Cambiar la contraseña</div>
            <p class="form-text mb-3">Dejá los dos campos vacíos si no querés cambiarla.</p>
            <div class="row g-3">
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="pass">Nueva contraseña</label>
                    <input class="form-control" id="pass" name="pass" type="password"
                           autocomplete="new-password">
                </div>
                <div class="col-12 col-sm-6 mb-3">
                    <label class="form-label" for="pass2">Confirmar contraseña</label>
                    <input class="form-control" id="pass2" name="pass2" type="password"
                           autocomplete="new-password">
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label" for="imagen">Cambiar la imagen (opcional)</label>
                <input class="form-control" id="imagen" name="imagen" type="file" accept="image/*">
                <% if (perfil.getTieneImagen()) { %>
                <p class="form-text mt-2">Si no elegís ninguna, queda la que ya tenés.</p>
                <% } %>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button class="btn btn-accent" type="submit">Guardar cambios</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/perfil">Cancelar</a>
            </div>
        </form>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
    <script src="<%= ctx %>/assets/js/validacion.js"></script>
</body>
</html>
