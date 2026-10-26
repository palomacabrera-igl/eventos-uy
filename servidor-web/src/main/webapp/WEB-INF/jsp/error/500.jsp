<%@ page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true" %>
<!doctype html>
<html lang="es">
<head>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabeza.jsp" />
    <title>Error · eventos.uy</title>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/plantilla/cabezal.jsp" />
    <main class="container-fluid py-5 px-3 px-lg-4">
        <h1 class="ev-pagetitle">Algo salió mal</h1>
        <p>Hubo un error inesperado. Si vuelve a pasar, avisanos.</p>
        <a href="<%= request.getContextPath() %>/eventos">← Volver a la cartelera</a>
    </main>
    <jsp:include page="/WEB-INF/jsp/plantilla/pie.jsp" />
</body>
</html>
