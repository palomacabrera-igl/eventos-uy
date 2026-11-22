# Diseño del video, la búsqueda y la versión para celular

**eventos.uy** — Laboratorio 3, Programación de Aplicaciones, UTEC. Parte del Rol 3.

Este documento responde, para el Rol 3, al punto 2 de «Se pide»: *«Diseño de la solución distribuida: operaciones publicadas por el Servidor Central, datatypes que viajan por la red, manejo de excepciones remotas, configuración y versión para celular»*. Hay una sección por cada tema del rol (video, búsqueda y versión para celular), con la decisión que se tomó y por qué, y una última con las decisiones que la letra dejaba abiertas.

Los DSS de los casos de uso que cambian (punto 1 de «Se pide») están en `Laboratorio3-Rol3.drawio.xml` y su PDF:

| Hoja | Qué tiene |
| --- | --- |
| AltaDeEdicionConVideo | DSS de Alta de Edición de Evento, con el video opcional |
| ConsultaDeEdicionConVideo | DSS de Consulta de Edición de Evento (cambia: video) |
| InicioDeSesionCelular | DSS de Inicio de Sesión (celular) |

La búsqueda no tiene DSS porque no es un caso de uso de la letra sino un requerimiento (7.8); sus operaciones y contratos están en la sección 2.

## 1. El video de YouTube

La letra (4 y 6.2) pide que una edición pueda tener, de forma opcional, un video de YouTube, y (7.4) que se reproduzca embebido en la página de la edición, en el sitio y en el celular.

### Dónde vive el dato

`EdicionEvento` tiene un atributo nuevo, `urlVideo`, que puede ser nulo. No hay una clase aparte para el video: es un solo dato de la edición, sin comportamiento propio. Es el único cambio que el Rol 3 le hace al modelo de dominio, y figura en el modelo general del grupo (`ModeloGeneral.drawio`).

### La validación va en la lógica

`altaEdicion` pasa la dirección por `VideoYoutube.normalizar` antes de crear la edición:

- Acepta las tres formas que da YouTube al compartir un video: `youtube.com/watch?v=ID`, `youtu.be/ID` y `youtube.com/shorts/ID`, con o sin `https` y `www`.
- Saca el identificador del video (11 caracteres) y guarda siempre `https://www.youtube.com/watch?v=ID`.
- Vacía o nula: la edición queda sin video. Cualquier otra dirección: `ReglaNegocioException` y la edición no se crea.

Que la regla esté en la lógica, y no solo en la página, es lo que impide guardar una dirección que no es de YouTube aunque el pedido no pase por el formulario. `validacion.js` aplica la misma regla en el navegador, pero solo para avisar antes de enviar.

### Qué viaja por la red

No hay operaciones nuevas para el video: viaja en el campo `urlVideo` de `DTEdicionEvento` (en el alta) y de `DTEdicionCompleto` (en la consulta). Una dirección que no sirve vuelve al sitio como el mismo SOAP Fault de cualquier error de negocio, con su mensaje.

### Cómo se muestra

El sitio convierte la dirección guardada a la forma que admite el reproductor embebido (`Texto.videoEmbebido`: `watch?v=ID` pasa a `https://www.youtube.com/embed/ID`) y la muestra con un `iframe` dentro de un contenedor `ratio-16x9` de Bootstrap. En el celular ocupa el ancho de la pantalla y conserva el 16:9. Guardar siempre la misma forma es lo que hace que la conversión sea así de simple.

## 2. La búsqueda

La letra (7.8) pide un cuadro de búsqueda en el cabezal de todas las páginas y una página de resultados con eventos y ediciones, con orden por fecha de alta y alfabético.

### Dos operaciones publicadas, sin estado

| Operación | Devuelve |
| --- | --- |
| `buscarEventos(texto: String, orden: OrdenBusqueda)` | `List<DTEvento>`: eventos no finalizados cuyo nombre o descripción contienen el texto |
| `buscarEdiciones(texto: String, orden: OrdenBusqueda)` | `List<DTEdicionEvento>`: ediciones Aceptadas, de eventos no finalizados, cuyo nombre contiene el texto |

Devuelven `List` y no `Set` porque el orden importa. No modifican nada.

**Firma:** `buscarEventos(texto: String, orden: OrdenBusqueda) : List<DTEvento>`

**Precondición:** ninguna

**Postcondición:**

- Se retornan los eventos no finalizados cuyo nombre o descripción contienen texto, sin distinguir mayúsculas de minúsculas.
- Si texto es vacío o nulo, se retornan todos los eventos no finalizados.
- Se ordenan según orden (ver abajo).
- No modifica nada.

**Firma:** `buscarEdiciones(texto: String, orden: OrdenBusqueda) : List<DTEdicionEvento>`

**Precondición:** ninguna

**Postcondición:**

- Se retornan las ediciones en estado ACEPTADA, de eventos no finalizados, cuyo nombre contiene texto, sin distinguir mayúsculas de minúsculas.
- texto y orden se tratan igual que en buscarEventos.
- No modifica nada.

### El orden es un enumerado

`OrdenBusqueda` tiene tres valores: `FECHA_ALTA` (el orden por defecto, también si llega nulo), `ALFABETICO_ASC` y `ALFABETICO_DESC`. Es un enumerado y no un texto para que el sitio no pueda pedir un orden que no existe; en el WSDL queda como una lista de valores permitidos. Por fecha van de la más reciente a la más antigua y, si dos tienen la misma, por nombre. Los órdenes alfabéticos usan un `Collator` en español, para que la Ñ y las tildes queden en su lugar.

### Dónde se filtra y se ordena

En la clase `Busqueda` de la lógica: `Sistema` le pasa los eventos que lee con el manejador, y ella filtra y ordena. Como no toca la base, se prueba sin PostgreSQL (`BusquedaTest`).

### La página de resultados

El cuadro del cabezal manda a `/buscar?q=...&orden=fecha|az|za&tipo=todos|eventos|ediciones`. El servlet `Buscar` pide las dos listas y las intercala con el mismo criterio de orden, así eventos y ediciones se muestran juntos, como en la figura 7. La página tiene pestañas por tipo, el recuadro de orden y el texto buscado resaltado; cada resultado enlaza a su consulta.

## 3. La versión para celular

La letra (7.3) pide que forme parte del mismo `web.war`, bajo una ruta propia, con un diseño para pantallas chicas, y que reutilice los servlets y el acceso a los servicios web del resto del sitio.

### Los mismos servlets, otra página

Ante la pregunta de si «reutilizar los servlets» quería decir los mismos servlets, el profesor respondió que sí, para no duplicar trabajo. Por eso cada servlet que necesita el celular atiende una ruta más (`@WebServlet({"/edicion", "/movil/edicion"})`), hace lo mismo en los dos casos y termina con `Paginas.forward(...)`. Ese método elige la página según la ruta del pedido: si empieza con `/movil`, usa la versión para celular de esa página (el mapa `PARA_CELULAR`). La lógica del sitio y las llamadas al Servidor Central no se duplican; lo único propio del celular son las JSP de `WEB-INF/jsp/movil/`.

| Caso de uso (6.3) | Ruta | Servlet |
| --- | --- | --- |
| Inicio de Sesión | `/movil`, `/movil/iniciar-sesion` | `IniciarSesion` |
| Cierre de Sesión | `/movil/cerrar-sesion` | `CerrarSesion` |
| Consulta de Edición | `/movil/eventos`, `/movil/evento`, `/movil/edicion` | `ListarEventos`, `ConsultarEvento`, `ConsultarEdicion` |
| Consulta de Registro | `/movil/registros`, `/movil/registro?edicion=...` | `ConsultarRegistro` |
| Registro de Asistencia | `/movil/asistencia` | `RegistrarAsistencia` |

`RegistrarAsistencia` es el único servlet nuevo, porque ese caso de uso no existe en el sitio de escritorio. Usa la operación `registrarAsistencia` del Rol 4.

### Solo asistentes

La letra (5 y 6.3) dice que desde el celular solo inician sesión los asistentes. La operación `iniciarSesion` del Servidor Central es la misma de siempre; la regla la aplica el Servidor Web, que es el que sabe por qué ruta llegó el pedido:

- `IniciarSesion`: si el pedido viene de `/movil` y el usuario es un organizador, no abre la sesión e informa que desde el celular solo pueden iniciar sesión los asistentes. Se pueden reingresar los datos o cancelar.
- `Paginas.celularSinAsistente`: cualquier otra página del celular sin un asistente con sesión vuelve al inicio de sesión del celular.

### Las pantallas

Siguen la estructura de las figuras 2 y 3:

- La página inicial es el inicio de sesión (7.3.1).
- Con la sesión iniciada, una barra con menú hamburguesa (`collapse` de Bootstrap) da acceso a Consulta de edición, Mis registros, Registrar asistencia y Cerrar sesión (7.3.2).
- Los eventos se muestran en tarjetas que se reacomodan según el ancho; al elegir uno, sus ediciones Aceptadas, marcando en las que el asistente está registrado; al elegir una, todos sus datos, la imagen, el video y el detalle del registro del asistente (7.3.3).
- Mis registros muestra una tarjeta por registro con tipo, fecha, costo, si usó código de patrocinio y la asistencia.
- Registrar asistencia muestra los registros del asistente, primero los que se pueden marcar. El botón pide confirmación; si la asistencia ya estaba registrada, lo indica y no la ofrece otra vez. `RegistrosCelular` decide qué registros se pueden marcar con la misma regla del Central (edición Aceptada y ya empezada).

### El estilo

Bootstrap 5 está incluido en el proyecto (`assets/vendor/bootstrap`). Los colores, las fuentes y las tarjetas salen de `custom.css`, igual que en el sitio de escritorio, así las dos versiones se ven como un mismo sitio; `movil.css` solo agrega lo propio de las pantallas chicas.

### Las visitas desde el celular

El filtro de visitas del Rol 4 también atiende `/movil/evento`, así que las consultas desde el celular suman al ranking de eventos más visitados (7.7).

## 4. Las decisiones que la letra dejaba abiertas

| Tema | Decisión | Por qué |
| --- | --- | --- |
| Búsqueda de ediciones | Solo por nombre | Las ediciones no tienen descripción en el modelo |
| Ediciones de un evento finalizado | No aparecen en la búsqueda | Coherente con que el evento no aparece en los listados ni en las búsquedas |
| Formatos de video | `watch?v=`, `youtu.be/` y `shorts/`, guardados como `watch?v=ID` | Son las direcciones que da YouTube; guardar una sola forma simplifica la conversión a `embed/` |
| Búsqueda en el celular | No | La 7.3 limita el celular a los casos de uso de 6.3, y la búsqueda no está entre ellos |
| «Recordarme en este dispositivo» | No | Aparece en la figura 2a, pero el texto de la letra no lo pide; las figuras son una guía de estructura |

Para recorrer esta parte con los datos de prueba: **JIAP 2026** y **Montevideo Rock 2026** tienen video; en el celular entran `vale23`, `jperez` y `lgomez`, y tienen la asistencia pendiente `jperez` en Maratón 2026 y `lgomez` en Comics 2026 y JIAP 2026.
