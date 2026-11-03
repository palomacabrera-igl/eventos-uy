# Diseño de la solución web

**eventos.uy** — Laboratorio 2, Programación de Aplicaciones, UTEC.

Este documento responde al punto 2 de «Se pide»: *«Diseño de la solución web: separación MVC, biblioteca JAR embebida, sesión, imágenes y estados de la edición»*. Hay una sección por cada uno de esos cinco temas, con la decisión que se tomó y por qué.

---

## 1. Separación MVC

El servidor web se organiza según el patrón Modelo–Vista–Controlador, como pide la Sección 7.4.

| | Quién lo cumple | Responsabilidad |
|---|---|---|
| **Modelo** | La lógica del Servidor Central, embebida como JAR | Las reglas de negocio y el acceso a la base por JPA |
| **Vista** | Las JSP, bajo `WEB-INF/jsp/` | Armar el HTML con los datos que reciben |
| **Controlador** | Los servlets, en `servlets/` | Recibir la petición, invocar el caso de uso y elegir la JSP |

La regla que ordena todo: **ninguna consulta ni modificación de datos ocurre fuera de un caso de uso de la lógica**. En el repositorio del sitio no hay una sola línea de JPA ni de SQL.

### Las JSP viven bajo `WEB-INF`

No es un detalle de organización: lo que está bajo `WEB-INF` **no se puede pedir por URL**. Se llega a una página únicamente pasando por su servlet, que es el que trae los datos y verifica los permisos.

Si las JSP estuvieran fuera, alguien podría abrir `/eventos/listar.jsp` directamente: la página aparecería vacía o rota, y —más grave— se saltearía los controles del servlet.

### Los objetos del dominio no viajan a la vista

Las JSP reciben **tipos de datos** (`DTUsuario`, `DTEvento`, `DTEdicionEvento`…), nunca entidades. Son tres razones distintas:

1. **La entidad tiene métodos que la modifican.** Si una JSP recibiera un `Usuario`, podría llamarle `setNombre()` y cambiarlo sin que la lógica se entere. El DT es de solo lectura.
2. **La entidad está conectada a la base.** Recorrer un objeto de JPA desde la vista puede disparar consultas cuando la transacción ya se cerró.
3. **El DT lleva solo lo que la pantalla necesita.** `DTUsuario` tiene el nickname, el nombre y el correo, pero **no la contraseña**: aunque alguien se equivoque al escribir una JSP, la contraseña no está ahí para mostrarse.

Es el mismo criterio que ya usaba la Estación de Trabajo en el Laboratorio 1.

### Cómo se reparte el trabajo dentro del controlador

Todos los servlets siguen la misma forma —leer los parámetros, llamar al caso de uso, dejar el resultado en el *request*, elegir la JSP— y comparten cuatro clases de apoyo:

| Clase | Qué resuelve |
|---|---|
| `Paginas` | Dónde está cada JSP, y las respuestas comunes: `forward`, `redirect`, `prohibida`, `noEncontrada` |
| `SesionWeb` | Quién está conectado. Es el único lugar que toca la sesión |
| `Texto` | Escapar el HTML y dar formato a fechas e importes |
| `FiltroUtf8`, `FiltroDatosFrescos` | La codificación de las peticiones y la frescura de los datos leídos |

### La dirección de las dependencias

Los pedidos van siempre **hacia abajo**: la presentación le pide a la lógica, y la lógica a la persistencia. La lógica no sabe que existe una pantalla.

Esto no es sólo teoría: al separar los repositorios apareció que la lógica leía las imágenes de los datos de prueba desde una carpeta del servidor web — una dependencia apuntando hacia arriba. Se corrigió moviéndolas dentro del JAR de la lógica, y una prueba automática evita que vuelva a pasar.

---

## 2. La biblioteca JAR embebida

La Figura 1 de la letra muestra, dentro del nodo del Servidor Web, una caja que dice *«Servidor Central: lógica embebida (JAR)»*. **Embebida** significa que viaja adentro ya compilada, como una biblioteca, no como código que se compila junto.

### Dos repositorios

El proyecto está en dos repositorios:

| Repositorio | Contiene | Produce |
|---|---|---|
| `eventos-uy` | La lógica, la persistencia y la Estación de Trabajo | `logica-1.0-SNAPSHOT.jar` |
| `eventos-uy-web` | Los servlets y las JSP. **Ninguna clase de la lógica** | `eventos-uy.war` |

Mientras fueron un único proyecto Maven, el sitio compilaba la lógica desde su código fuente. Parecía equivalente y no lo es: borraba el límite entre dos componentes que el diagrama de distribución muestra separados, y hacía imposible que el sitio usara una versión distinta de la que estaba abierta en el editor.

### Cómo llega el JAR

La carpeta `lib/` del repositorio del sitio está armada con la estructura de un repositorio de Maven, y el `pom.xml` la declara como tal:

```xml
<repositories>
    <repository>
        <id>servidor-central-embebido</id>
        <url>file://${project.basedir}/lib</url>
    </repository>
</repositories>
```

Así Maven trata la lógica como cualquier otra biblioteca: la resuelve sin tener su código y la incluye dentro del WAR, en `WEB-INF/lib`, al lado de Hibernate y del driver de PostgreSQL. Abrir el `.war` y ver ahí `logica-1.0-SNAPSHOT.jar` **es exactamente el dibujo de la Figura 1**.

Junto al JAR viaja su **ficha de dependencias** (`logica-1.0-SNAPSHOT.pom`), sin elemento `<parent>` porque el repositorio del sitio no tiene el POM padre. Esa ficha es la que le dice a Maven que la lógica necesita Hibernate y el driver: sin ella el sitio compila, arranca, y falla en el primer acceso a la base.

El script `scripts/publicar-logica.ps1`, en el repositorio de la lógica, compila y deja los cuatro archivos —el JAR, la ficha y sus dos *checksums*— en el repositorio del sitio. La ficha se genera leyendo el `pom.xml` real de la lógica, así que si mañana se agrega una biblioteca aparece sola.

### Dos procesos, un EntityManager cada uno

El Servidor Web corre dentro de Tomcat y la Estación de Trabajo es una aplicación de escritorio: son dos JVM distintas. Cada una levanta su propio `EntityManagerFactory` a partir del mismo `persistence.xml`, y las dos escriben en la misma base PostgreSQL. No comparten sesión ni caché de JPA.

Un alta hecha desde el sitio queda disponible en la Estación y viceversa, y cada aplicación funciona con la otra detenida, como pide la Sección 3.

### Preparado para la iteración siguiente

La letra avisa que *«la separación en procesos comunicados por web services corresponde a la iteración siguiente, así que la interfaz de la lógica tiene que poder reemplazar el JAR embebido por esa llamada remota más adelante»*.

El diseño ya lo permite, por tres decisiones:

1. **Los servlets no conocen la clase `Sistema`: conocen la interfaz `IControladorSistema`.** Reemplazar el JAR por una llamada remota es escribir otra implementación de esa interfaz; los servlets no cambian.
2. **Se usan DT y no entidades.** Un DT es un objeto de datos: eso puede viajar por la red. Una entidad de JPA no.
3. **Las operaciones reciben todo de una vez.** Se hizo porque HTTP no tiene memoria, y es justamente lo que necesita una llamada remota.

---

## 3. La sesión

La sesión del usuario autenticado **se conserva en el servidor**, como pide la Sección 7.4. En la Parte 1 se simulaba en el navegador con `localStorage`, donde cualquiera podía editarla desde las herramientas de desarrollo.

### Qué se guarda

Al iniciar sesión correctamente, el servidor guarda en la `HttpSession` el **`DTUsuario`** de quien entró — un dato de transporte, nunca una entidad. Con eso le alcanza al cabezal para mostrar el nombre y el rol sin volver a consultar en cada página.

Guardar el DT tiene un riesgo conocido: queda una copia, y si la persona edita su perfil, esa copia envejece. Por eso **el caso de uso de modificar datos vuelve a abrir la sesión** con los datos recién guardados:

```java
SesionWeb.iniciar(request, controlador.consultarUsuario(nickname, nickname));
```

Así no hay dos versiones de la verdad dando vueltas.

La clase `SesionWeb` es el único punto del sitio que toca la sesión: la abre al iniciar, la cierra al salir, y responde quién está conectado y quién es el solicitante que se le pasa a la lógica. Ningún otro servlet la manipula.

### El solicitante

Casi todas las operaciones de consulta reciben un **solicitante**, y es la lógica —no el servlet— la que decide qué corresponde mostrarle. Por ejemplo, `consultarUsuario(nickname, solicitante)` devuelve los registros del perfil solo cuando el solicitante es su dueño.

Si el servlet tuviera un `if` eligiendo qué esconder, esa sería una regla de negocio metida en el controlador, que es justo lo que la Sección 7.4 no quiere.

### El cierre de sesión va por POST

Cerrar sesión cambia el estado del servidor, así que no puede ser un enlace. Si lo fuera, cualquier página podría cerrarle la sesión a un usuario con una imagen escondida apuntando a esa dirección. Por eso es un formulario con `method="post"`.

### Cuando falta el permiso

Se distinguen dos situaciones que antes se trataban igual:

| Situación | Respuesta |
|---|---|
| No hay sesión | Redirección a iniciar sesión |
| Hay sesión, pero no es el rol que hace falta | **403** con la página de error y el motivo |

Mandar a iniciar sesión a alguien que ya la inició lo deja girando en el aire: como `/iniciar-sesion` rebota a la cartelera cuando ya hay sesión, el usuario terminaba en la cartelera sin ningún mensaje.

### La consecuencia sobre las operaciones de la lógica

HTTP no tiene memoria: cada petición llega sola. Por eso **los servlets no pueden usar las operaciones del Laboratorio 1 que guardaban estado entre llamadas** (`seleccionarEvento`, `ingresarDatosUsuario`…). La Estación de Trabajo las llama en orden dentro de un mismo proceso; el sitio atiende pedidos sueltos de muchos usuarios a la vez.

Para el sitio se agregaron operaciones equivalentes que reciben todo lo que necesitan en una sola llamada. Las del Laboratorio 1 se conservan porque la Estación las sigue usando.

---

## 4. Las imágenes

Los usuarios, eventos, ediciones e instituciones pueden tener una imagen. La Sección 7.2 deja que el grupo elija dónde guardarla: en la base o en el sistema de archivos del servidor.

### La decisión: en la base

Se guardan en PostgreSQL, como una columna `bytea` junto al tipo MIME. Los motivos:

- **Una sola copia de seguridad.** Respaldar la base respalda también las imágenes; no hay que acordarse de una carpeta aparte.
- **No hay rutas que mantener sincronizadas.** Con archivos, la base guarda una ruta y el disco guarda el archivo: si uno de los dos cambia, quedan descolgados.
- **No depende de permisos de escritura del servidor**, que varían entre máquinas.

El precio es que la base crece más rápido y que una imagen muy grande pesa en cada consulta. Para el tamaño de este sistema no es un problema, y los formularios limitan el archivo a 5 MB.

En el modelo, la imagen es un `@Embeddable` (`Imagen`) con el contenido y el tipo. Las cuatro entidades que pueden tenerla la incluyen de la misma forma.

### Cómo llega al navegador

El servlet `VerImagen`, en `/imagen`, recibe el tipo de entidad y su clave, **le pide los bytes a la lógica** y los devuelve con su tipo MIME. Las páginas la muestran así:

```html
<img src="/imagen?tipoEntidad=evento&clave=JIAP">
```

El servlet no abre archivos ni consulta la base: cumple lo que pide la letra, *«el servlet no accede a ese almacenamiento por fuera de la lógica»*.

Cuando la entidad no tiene imagen, la página no muestra un archivo por defecto: dibuja la inicial del nombre sobre un fondo de color. Así no hay que mantener imágenes de relleno.

### Las imágenes de los datos de prueba

Son un caso aparte: no son imágenes del sitio sino **de los datos de prueba**, y las usa `CargarDatos` para llenar la base. Viven dentro del JAR de la lógica, en `src/main/resources/imagenes/`, y se leen del *classpath*.

Antes estaban en la carpeta del servidor web y la lógica las leía de ahí por una ruta relativa. Eso tenía dos problemas: ataba el Servidor Central a archivos de otro componente —la dependencia al revés— y solo funcionaba ejecutando el programa parado en la raíz del proyecto.

---

## 5. Los estados de la edición

Una edición de evento tiene tres estados. Las transiciones válidas son las de la Tabla 1 de la letra:

| Origen | Destino | Caso de uso que lo produce |
|---|---|---|
| — | Ingresada | Alta de Edición de Evento |
| Ingresada | Aceptada | Aceptar/Rechazar Edición de Evento |
| Ingresada | Rechazada | Aceptar/Rechazar Edición de Evento |

Una vez Aceptada o Rechazada, **el estado no vuelve a cambiar**.

### Dónde vive la regla

El estado es un atributo de `EdicionEvento` y nace en `INGRESADA`. La clase expone dos operaciones, `aceptar()` y `rechazar()`, y **no tiene un `setEstado()`**: son las dos únicas puertas. Las dos pasan por un método privado que verifica que el estado actual sea `INGRESADA` y, si no lo es, rechaza el cambio.

Que la regla esté en la entidad y no en el caso de uso sigue el criterio **Experto en Información** de GRASP: la edición es la que conoce su propio estado.

El cambio ocurre únicamente en el caso de uso **Aceptar/Rechazar Edición de Evento**, que es exclusivo del administrador desde la Estación de Trabajo. **El sitio no tiene ninguna forma de cambiar un estado.**

### Quién ve qué

| Quién consulta | Qué ve |
|---|---|
| Visitante, asistente u otro organizador | Solo las ediciones **Aceptada** |
| El organizador de la edición | Las suyas en los tres estados, desde su propio perfil, y puede abrir el detalle |

Esa regla está escrita **una sola vez**, en el método `puedeVerla(solicitante)` de `EdicionEvento`. Es la edición la que sabe en qué estado está y de quién es, así que es la experta en decidir quién puede verla — el criterio **Experto en Información** otra vez.

Las consultas de la lógica llegan a ese método de dos formas: `consultarEdicion` lo llama directamente, porque ya tiene la edición en la mano; y las consultas de tipo de registro y de patrocinio pasan por `edicionVisiblePara(nombre, solicitante)`, un auxiliar que busca la edición y aplica la misma verificación.

Tener la regla una sola vez evita que una pantalla la aplique y otra no, que fue exactamente un problema que apareció durante el desarrollo: la consulta del tipo de registro exigía Aceptada para todos, y el organizador se topaba con un callejón sin salida en su propia edición.

### La regla se verifica en la lógica, no en la pantalla

El listado del sitio ofrece solo las ediciones Aceptadas, y el botón de registrarse no aparece en las demás. **Pero eso es presentación.** La regla de que no se puede registrar a una edición que no esté Aceptada está escrita en el caso de uso `altaRegistro`, de modo que una petición que no pase por esas pantallas se rechaza igual.

Es el mismo criterio que la Sección 7.3 pide para la validación: lo que el navegador controla, la lógica lo vuelve a controlar.

---

## Resumen de las decisiones

| Tema | Decisión | Motivo principal |
|---|---|---|
| MVC | Servlets controlan, JSP dibujan, la lógica sabe | La regla se escribe una sola vez |
| JSP bajo `WEB-INF` | No accesibles por URL | Nadie se saltea el servlet |
| Datos a la vista | DT, nunca entidades | La vista no puede modificar ni disparar consultas |
| JAR embebido | `lib/` como repositorio de Maven | El JAR termina dentro del WAR, como la Figura 1 |
| Repositorios | Dos, separados | El límite entre componentes es real, no sólo conceptual |
| Sesión | En el servidor, guardando el `DTUsuario` | No se puede falsificar; se reabre al editar el perfil |
| Cierre de sesión | Por POST | Un enlace permitiría cerrarla desde afuera |
| Imágenes | En la base, como `bytea` | Un solo respaldo, sin rutas que sincronizar |
| Estados | En la entidad, con dos puertas y sin `setEstado()` | La edición es la experta en su propio estado |
| Visibilidad | Un único `puedeVerla` en la entidad | Que ninguna pantalla aplique una regla distinta |
