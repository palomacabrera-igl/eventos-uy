<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Spike - subir imagen</title>
</head>
<body>
    <h1>Spike: subir imagen a la base de datos</h1>

    <p>
        La imagen se guarda dentro de la entidad seleccionada
        y no como archivo en el servidor.
    </p>

    <% if (request.getAttribute("error") != null) { %>
        <p style="color: red;">
            <%= request.getAttribute("error") %>
        </p>
    <% } %>

    <form action="<%= request.getContextPath() %>/spike-imagen"
          method="post"
          enctype="multipart/form-data">

        <p>
            <label for="tipoEntidad">Tipo de entidad:</label>

            <select id="tipoEntidad" name="tipoEntidad" required>
                <option value="">Seleccioná una opción</option>
                <option value="usuario">Usuario</option>
                <option value="evento">Evento</option>
                <option value="edicion">Edición</option>
                <option value="institucion">Institución</option>
            </select>
        </p>

        <p>
            <label for="clave">Identificador:</label>

            <input id="clave"
                   name="clave"
                   type="text"
                   required>

            <small>
                Usuario: nickname, por ejemplo <code>vale23</code>.<br>
                Evento: nombre, por ejemplo <code>JIAP</code>.<br>
                Edición: nombre, por ejemplo <code>JIAP 2026</code>.<br>
                Institución: nombre, por ejemplo <code>UTEC</code>.
            </small>
        </p>

        <p>
            <label for="imagen">Imagen:</label>

            <input id="imagen"
                   name="imagen"
                   type="file"
                   accept="image/jpeg,image/png,image/webp"
                   required>
        </p>

        <button type="submit">Subir y guardar</button>
    </form>
</body>
</html>