<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="servlets.Texto" %>
<%
    // El login de la Parte 1 (login.html), ahora resuelto en el servidor.
    // Antes el usuario y el rol se guardaban en el navegador (localStorage) y
    // cualquiera los podia cambiar; ahora los guarda Tomcat en la sesion.
    //
    // El servlet deja aca dos cosas cuando algo salio mal: el mensaje de error
    // (lo muestra aviso.jsp) y el identificador que la persona ya habia
    // escrito, para no obligarla a escribirlo de nuevo. La contrasena no
    // vuelve nunca: se escribe otra vez.
    String identificador = (String) request.getAttribute("identificador");
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Inicio de sesión · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container pt-4">
        <section class="ev-pagehead">
            <p class="ev-kicker">Inicio de sesión</p>
            <h1 class="ev-pagetitle">Ingresá a eventos.uy</h1>
        </section>

        <jsp:include page="/WEB-INF/jsp/plantilla/aviso.jsp" />

        <%-- method="post" porque la contrasena NO puede viajar en la direccion.
             Con get quedaria escrita en la barra del navegador y en el historial. --%>
        <form class="ev-form" method="post" action="<%= ctx %>/iniciar-sesion">
            <div class="mb-3">
                <label class="form-label" for="identificador">Nickname o correo electrónico</label>
                <input class="form-control" id="identificador" name="identificador" type="text"
                       autocomplete="username" required
                       value="<%= identificador == null ? "" : Texto.escapar(identificador) %>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Contraseña</label>
                <input class="form-control" id="password" name="password" type="password"
                       autocomplete="current-password" required>
            </div>
            <div class="d-flex gap-2 mt-4">
                <button class="btn btn-accent" type="submit">Ingresar</button>
                <a class="btn btn-outline-secondary" href="<%= ctx %>/eventos">Cancelar</a>
            </div>
            <p class="form-text mt-3">
                ¿No tenés cuenta? <a href="<%= ctx %>/registrarse">Registrate</a>.
            </p>
            <div role="alert" class="alert alert-secondary mt-4">
                <strong>Usuarios de prueba</strong>
                <ul class="mb-0 mt-2">
                    <li>Asistente con institución: <code>vale23</code> / <code>Vale2026!</code></li>
                    <li>Asistente de UTEC (sin registros): <code>lgomez</code> / <code>Lucia2026!</code></li>
                    <li>Asistente sin institución: <code>jperez</code> / <code>Juan2026!</code></li>
                    <li>Organizador: <code>imm</code> / <code>Imm2026!</code></li>
                    <li>Organizador: <code>antel</code> / <code>Antel2026!</code></li>
                </ul>
            </div>
        </form>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
