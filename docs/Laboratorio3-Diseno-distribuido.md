# Diseño de la solución distribuida

**eventos.uy** — Laboratorio 3, Programación de Aplicaciones, UTEC. Plataforma y servicios.

Este documento responde al punto 2 de «Se pide»: *«Diseño de la solución distribuida: operaciones publicadas por el Servidor Central, datatypes que viajan por la red, manejo de excepciones remotas, configuración y versión para celular»*. Las secciones 1 a 8 cubren la parte común de la plataforma; la **versión para celular** (y el video y la búsqueda) la explica `Laboratorio3-Video-Busqueda-Celular.md`, y los datos de prueba y el guion de defensa de asistencia, constancia, visitas y validación están en `Laboratorio3-Asistencia-Visitas-y-Defensa.md`. Los diagramas de secuencia de los casos de uso nuevos están en `DSS-Laboratorio3.drawio.xml`.

| Sección | Tema |
|---|---|
| 1 | Qué cambia respecto del Laboratorio 2 |
| 2 | Las operaciones publicadas |
| 3 | Los datatypes que viajan por la red |
| 4 | Las excepciones remotas |
| 5 | La configuración |
| 6 | El empaquetado y el despliegue |
| 7 | Varios clientes a la vez |
| 8 | Resumen de las decisiones |

---

## 1. Qué cambia respecto del Laboratorio 2

En el Laboratorio 2 la lógica viajaba como un JAR dentro del sitio, y la Estación de Trabajo y el sitio entraban cada uno por su cuenta a la base. Ahora cada parte corre en su nodo y solo el Servidor Central habla con la base:

```
 Navegador (computadora o celular)
          |  HTTP
 +--------v-----------------------+
 | Servidor Web (Tomcat, web.war) |   repo eventos-uy-web
 |   servlets, JSP, sitio /movil  |   cliente SOAP generado con wsimport
 +--------+-----------------------+
          |  SOAP sobre HTTP (puerto 9128)
 +--------v-----------------------+
 | servidor.jar                   |   repo eventos-uy
 |   Publicador (servicio web)    |
 |   Servidor Central (lógica)    |
 |   Estación de Trabajo (Swing)  |
 +--------+-----------------------+
          |  JPA / JDBC
      PostgreSQL
```

- El **Servidor Central** es la lógica y la persistencia. La **Estación de Trabajo** corre en el mismo proceso y llama al controlador directamente (`IControladorSistema`), sin pasar por la red.
- El **Servidor Web** ya no tiene ninguna clase de la lógica ni accede a la base: todo dato que muestra o modifica lo pide por SOAP.
- Lo único que comparten los dos repositorios es el **contrato** (el WSDL).

Por eso hubo que decidir qué se publica, cómo viajan los datos y qué pasa cuando algo falla en la red, que es lo que sigue.

## 2. Las operaciones publicadas

### La clase `Publicador`

Una sola clase, `servicios.Publicador`, anotada con `@WebService`, publica todas las operaciones (un solo servicio, un solo WSDL). Es una **fachada delgada**: cada método llama al controlador que le da la `Fabrica` y devuelve lo que contesta, sin reglas de negocio ni JPA. Las reglas siguen en la lógica, así que valen igual para la Estación de Trabajo y para el sitio.

La publica `servicios.Main` con `Endpoint.publish`, que usa el servidor HTTP liviano del JDK: no hace falta Tomcat del lado del Central. La publicación se hace **antes de abrir la ventana** de la Estación, y antes se «calienta» la conexión con la base (si no, la primera consulta del sitio tardaría lo que tarda Hibernate en arrancar y podría cortar por tiempo).

### Criterios para elegir qué se publica

| Criterio | Por qué |
|---|---|
| **Solo operaciones sin estado** | Las operaciones del Laboratorio 1 guardan datos en campos de un único `Sistema` entre llamada y llamada (por ejemplo, elegir un evento y después dar de alta una edición). Con varios clientes se pisarían entre sí. Las operaciones del sitio reciben todo lo que necesitan en cada llamada. |
| **Nombre único** | SOAP no admite dos operaciones con el mismo nombre. Se publica la versión más completa de cada una (`listarEventos(categoria)` con `null` equivale a la versión sin parámetro; `altaRegistro` lleva el código de patrocinio, que puede ser `null`). |
| **El solicitante va como parámetro** | El Servidor Central no tiene sesión: quién pide lo sabe el sitio (que guarda la sesión) y se lo manda en cada llamada. Con eso el Central aplica la regla de quién puede ver qué. |
| **Parámetros con nombre** | Todos llevan `@WebParam(name = "...")`. Sin eso, el contrato los llama `arg0`, `arg1`. |
| **Listas, no conjuntos** | El cliente generado usa `List`, y en las búsquedas el orden importa. El `Publicador` convierte los `Set` del controlador. |
| **Las imágenes aparte** | Un `byte[]` viaja en Base64 dentro del XML y pesa: nunca va dentro de un listado, se pide con `obtenerImagenX`. |

### Las 45 operaciones

| Área | Operaciones |
|---|---|
| Sesión y usuarios (9) | `iniciarSesion`, `altaUsuario`, `consultarUsuario`, `listarUsuarios`, `modificarUsuario`, `listarEdicionesDeOrganizador`, `listarRegistrosDeAsistente`, `obtenerImagenUsuario`, `listarNombresInstituciones` |
| Eventos y ediciones (9) | `listarEventos`, `consultarEvento`, `listarEdicionesAceptadas`, `listarCategorias`, `altaEvento`, `altaEdicion`, `consultarEdicion`, `obtenerImagenEvento`, `obtenerImagenEdicion` |
| Tipos de registro e instituciones (6) | `altaTipoRegistro`, `listarTiposDeRegistro`, `consultarTipoRegistro`, `altaInstitucion`, `consultarInstitucion`, `obtenerImagenInstitucion` |
| Registros y patrocinios (6) | `altaRegistro`, `listarRegistrosDeEdicion`, `consultarRegistro`, `altaPatrocinio`, `listarPatrociniosDeEdicion`, `consultarPatrocinio` |
| Seguidores, finalizar y archivar (8) | `seguirUsuario`, `dejarDeSeguirUsuario`, `listarSeguidores`, `listarSeguidos`, `finalizarEvento`, `listarEventosFinalizables`, `archivarEdicion`, `listarEdicionesArchivables` |
| Búsqueda (2) | `buscarEventos`, `buscarEdiciones` |
| Asistencia, visitas y validación (5) | `registrarAsistencia`, `obtenerConstanciaAsistencia`, `registrarVisita`, `existeNickname`, `existeCorreo` |

Las 30 primeras son las que el sitio ya usaba en el Laboratorio 2; las otras 15 son las del Laboratorio 3. Las firmas completas, con sus precondiciones y postcondiciones, están en el javadoc de `IControladorSistema`.

### Lo que no se publica, a propósito

- **`listarEventosMasVisitados`**: solo la usa la Estación de Trabajo, que llama al controlador directamente. El sitio nunca la pide (solo informa visitas con `registrarVisita`).
- **El video**: no tiene operaciones. Viaja en el campo `urlVideo` de los datatypes de edición.
- **Las operaciones con estado del Laboratorio 1** (`seleccionarEvento`, `ingresarDatosUsuario`, `ingresarDatosEdicion`, etcétera): quedan para la Estación de Trabajo, que corre en el mismo proceso y las sigue usando.

### Cómo se repartió el trabajo

Primero se definieron **todas las firmas** de las operaciones nuevas, con su documentación, y se dejó el contrato publicado. Así cada integrante completó la lógica de sus casos de uso en `Sistema` sin cambiar el contrato, y el sitio pudo trabajar contra él desde el primer día.

## 3. Los datatypes que viajan por la red

Por la red viajan solamente tipos simples, enumerados, datatypes (`DT...`), listas de ellos y `byte[]`. **Ningún objeto del dominio viaja**: ya desde el Laboratorio 1 la presentación solo recibe `DT`, y eso es lo que hizo posible la separación.

### Las reglas de JAXB

JAXB convierte los objetos en XML y a la inversa. Para poder hacerlo, cada `DT` tiene:

- un **constructor público sin parámetros**, con el que JAXB crea el objeto vacío;
- un **getter y un setter públicos por cada atributo**, con los que lo completa;
- solo tipos que JAXB sabe convertir.

Los `DT` del Laboratorio 2 tenían constructor con datos pero no constructor vacío ni setters; se adaptaron **en el mismo lugar** (no se crearon copias paralelas), conservando los constructores con datos para el código que ya los usaba. La prueba `DatatypesJaxbTest` recorre todas las clases `DT...` y falla, con el nombre de la clase y del campo, si alguna deja de cumplir estas reglas: así un `DT` nuevo al que le falte un setter se detecta al correr las pruebas y no en la defensa.

### Las fechas: `DTFecha`

JAXB no sabe armar un `java.time.LocalDate`. En su lugar, los `DT` llevan `DTFecha` (día, mes y año): una clase simple, con su constructor vacío y sus setters, que además hace que el contrato no dependa de Java. La lógica sigue usando `LocalDate` por dentro y convierte en los bordes (`DTFecha.desdeOpcional` cuando la fecha puede faltar).

### Las colecciones

Los `Set` de los `DT` pasaron a `List`. En el contrato, un campo de lista se repite en el XML; el cliente generado lo ve como `List<...>`.

### Los enumerados

Tres enumerados viajan en el contrato y el cliente los genera con el mismo nombre: `TipoUsuario` (asistente u organizador), `EstadoEdicion` (con `ARCHIVADA` desde este laboratorio) y `OrdenBusqueda` (`FECHA_ALTA`, `ALFABETICO_ASC` y `ALFABETICO_DESC`). Con un enumerado, el sitio no puede pedir un valor que no existe: en el WSDL queda como una lista cerrada.

### Los usuarios: una superclase y dos subclases

Varias operaciones declaran `DTUsuario`, pero lo que viaja es un `DTAsistente` o un `DTOrganizador`. JAXB solo conoce los tipos que figuran en las firmas, así que el `Publicador` declara `@XmlSeeAlso({DTAsistente.class, DTOrganizador.class})`. En el sitio, el rol se lee del campo `tipo` (ver `Roles` y `SesionWeb`).

### Los nombres del contrato

Cada `DT` lleva `@XmlType(name = "...")` con su nombre exacto, para que el cliente genere `DTFecha` y no `DtFecha`. Las clases del cliente se generan con `wsimport` en el paquete `servicios` del sitio y **no se editan a mano**.

### Las imágenes y la constancia

Las imágenes (`DTImagen`) y el PDF de la constancia (`byte[]`) viajan en Base64 dentro de la respuesta. El sitio no accede al almacenamiento de imágenes del Central por ninguna otra vía: las pide con `obtenerImagenUsuario`, `obtenerImagenEvento`, `obtenerImagenEdicion` y `obtenerImagenInstitucion`, y las entrega al navegador con `VerImagen`.

## 4. Las excepciones remotas

Hay dos clases de fallas y se tratan distinto.

### Un error de negocio

Un nickname repetido, un cupo agotado o un código de patrocinio inválido son errores **esperados**. La lógica los comunica con una sola excepción, `ReglaNegocioException`, con el mensaje ya armado para la persona. Cada operación del `Publicador` la declara en su `throws`:

1. El Central lanza `ReglaNegocioException`.
2. JAX-WS la convierte en un **SOAP Fault** que lleva el mismo mensaje.
3. `wsimport` genera en el sitio `ReglaNegocioException_Exception` (una excepción *checked*, con el mismo mensaje).
4. El servlet la atrapa y muestra el mensaje en la página, sin pasar por una página de error.

Se eligió **una sola excepción** y no una por cada regla porque el sitio nunca decide nada según cuál fue: solo le muestra el mensaje a la persona. Una por regla habría llenado el contrato de tipos que nadie usa.

### El Servidor Central no responde

Si el Central está apagado, la IP de la configuración está mal o la red no deja pasar, el cliente generado lanza `jakarta.xml.ws.WebServiceException`. La letra pide una página de error propia, sin trazas.

- **Los servlets no la atrapan.** Se resuelve una sola vez en `web.xml`, con un `<error-page>` para `WebServiceException` que muestra `sin-central.jsp` («en este momento no podemos comunicarnos con el servidor de eventos.uy»). Tomcat elige la regla del tipo más parecido al de la excepción, así que esa le gana a la general de `Throwable`.
- **`SOAPFaultException`** también es una `WebServiceException`, pero significa que el Central respondió con un error que no era de negocio: es una falla real y va a la página de error general (500), no a «sin conexión».
- **Tiempos de espera.** El cliente espera 5 segundos para conectarse y 20 para recibir la respuesta. Sin eso, un Central que no responde dejaría el pedido colgado.
- **El cliente se crea la primera vez que se usa**, no al desplegar el sitio. Como el contrato se lee del propio WAR y no de la red, Tomcat levanta aunque el Central esté apagado, y el error recién aparece en el primer pedido.

### Las visitas y la validación

Dos llamadas del sitio no deben romper la página si fallan: la cuenta de visitas (`FiltroVisitas`, que solo deja registrado el problema en el log del servidor) y la verificación de nickname y correo mientras se escribe (`Disponibilidad`: si no se pudo verificar, decide el Central al enviar el formulario).

## 5. La configuración

Ninguna dirección, puerto ni credencial está en el código. Cada aplicación lee un archivo `.properties` de la carpeta `~/.eventosUy` del usuario que la ejecuta (en Windows, `C:\Users\<usuario>\.eventosUy`).

| Aplicación | Archivo | Claves |
|---|---|---|
| Servidor Central | `central.properties` | `servicio.host`, `servicio.puerto`, `servicio.ruta`, `bd.url`, `bd.usuario`, `bd.password` |
| Servidor Web | `web.properties` | `central.host`, `central.puerto`, `central.ruta` |

Ejemplo de `central.properties` (valores por defecto):

```
servicio.host=localhost
servicio.puerto=9128
servicio.ruta=/eventosuy
bd.url=jdbc:postgresql://localhost:5432/eventosuy
bd.usuario=eventosuy
bd.password=eventosuy
```

Ejemplo de `web.properties`, con el Central en otra computadora:

```
central.host=192.168.1.20
central.puerto=9128
central.ruta=/eventosuy
```

- **Se crean solos.** Si el archivo no existe, la aplicación lo crea desde una plantilla con valores por defecto que viaja dentro del JAR o del WAR. Si a un archivo viejo le falta una clave nueva, el Central se la agrega al final sin tocar las demás.
- **Cambiar la configuración no recompila nada**: se edita el archivo y se reinicia la aplicación.
- **La base de datos** dejó de estar fija en `persistence.xml`: `persistencia.Configuracion` lee las claves `bd.*` y se las pasa a `Persistence.createEntityManagerFactory`. Si la conexión falla, el Central dice la causa (por ejemplo, contraseña incorrecta) y **no publica el servicio**.
- **La dirección real manda sobre la del WSDL.** El contrato trae la dirección de la máquina donde se descargó; el cliente la reemplaza por la de `web.properties` al crearse.
- **IP y no `localhost`.** Para que otra computadora lo vea, el Central tiene que publicar con la IP de su máquina en la red (o `0.0.0.0` para escuchar en todas las interfaces). Con `localhost` solo lo ven los programas de la misma máquina; el Central lo avisa al arrancar y muestra las IP disponibles.
- En esa carpeta solo hay configuración: los recursos estáticos y las bibliotecas van dentro del JAR y del WAR.

## 6. El empaquetado y el despliegue

### Dos repositorios

| Repositorio | Contiene | Produce |
|---|---|---|
| `eventos-uy` | `logica` (Servidor Central) y `estacion-trabajo` (Estación y servicio web) | `servidor.jar` |
| `eventos-uy-web` | Servlets, JSP, sitio `/movil` y el cliente generado | `web.war` |

Se mantuvieron **dos repositorios** porque el sitio no puede incluir código de la lógica: así cada uno se compila, se prueba y se ejecuta por separado, y pueden estar en computadoras distintas. El precio es que el contrato se copia de uno a otro (ver más abajo).

### El contrato se copia, no se lee de la red

El sitio genera su cliente con `jaxws-maven-plugin` (`wsimport`) a partir de una **copia** del WSDL guardada en `src/main/resources/wsdl`, y no desde la dirección del servicio. Así el sitio compila aunque el Central esté apagado. Cuando el contrato cambia (una operación o un campo nuevo), se corre `scripts\actualizar-wsdl.ps1` en el repositorio del sitio con el Central andando, se recompila y se commitea la copia.

### Un solo comando

`scripts\construir-todo.ps1` (o `.sh`) compila los dos repositorios y deja `entrega\servidor.jar` y `entrega\web.war`.

- **`servidor.jar`** se arma con `maven-shade-plugin` y se ejecuta con `java -jar servidor.jar`. Junta las clases propias y las de todas las bibliotecas (Hibernate, el driver de PostgreSQL, JAX-WS, FlatLaf). Dos ajustes hacen que funcione fuera del IDE: un `ServicesResourceTransformer`, porque varias bibliotecas declaran implementaciones en archivos `META-INF/services` con el mismo nombre y sin él el JAR se queda con uno solo; y la exclusión de las firmas (`*.SF`, `*.DSA`, `*.RSA`) de las bibliotecas firmadas, que dejan de valer al mezclarlas.
- **`web.war`** se despliega en Tomcat 11, copiándolo a `webapps` o con el administrador de aplicaciones. Una sola versión de JAXB queda fijada con un BOM, porque con dos versiones el cliente fallaba al leer un error de negocio.
- **Nada depende del IDE**: el comando usa el Maven Wrapper (`mvnw`).

### Qué corre en cada computadora

| Dispositivo | Corre | Configuración |
|---|---|---|
| Computadora 1 | PostgreSQL y `java -jar servidor.jar` (Central y Estación) | `central.properties` con su IP en `servicio.host` |
| Computadora 2 | Tomcat con `web.war` | `web.properties` con la IP de la computadora 1 |
| Navegador de escritorio y celular | Ninguna aplicación | Entran a `http://<IP de la computadora 2>:8080/web` y a `/web/movil` |

## 7. Varios clientes a la vez

Con la lógica dentro del sitio había un solo cliente; ahora el Central atiende pedidos del sitio, de la Estación y de varios navegadores al mismo tiempo. Esto cambió tres cosas en el diseño:

- **Un `EntityManager` por operación.** Cada operación del controlador abre una unidad de trabajo, la usa y la cierra (confirma si salió bien, deshace si falló). Lo implementa `ControladorConUnidadDeTrabajo`, un intermediario que envuelve al controlador. Así cada operación lee la base de verdad y lo que cambia el sitio se ve en la Estación sin un botón «Actualizar» (que además vuelve a leer sus listas sola cada 5 segundos).
- **La edición se bloquea al registrar un asistente.** Dos asistentes que se registran a la vez en el último lugar de un tipo de registro, o con el último registro gratuito, no pueden pasar los dos: la edición se lee con un bloqueo de escritura (`PESSIMISTIC_WRITE`, un `SELECT ... FOR UPDATE`) y el segundo pedido espera a que termine el primero. Solo se bloquea esa edición y mientras dura la operación.
- **Las visitas se cuentan con un solo `UPDATE`** (`visitas = visitas + 1`) y no leyendo el número para guardarlo de nuevo: dos visitas simultáneas al mismo evento no se pisan.

## 8. Resumen de las decisiones

| # | Decisión | Por qué |
|---|---|---|
| 1 | Dos repositorios y un script que arma los dos paquetes | El sitio no puede tener código de la lógica; la letra pide un único comando documentado |
| 2 | Un solo servicio (`Publicador`) y un solo WSDL | Es una fachada del mismo controlador; separar en varios no daba independencia real |
| 3 | Fachada delgada: sin reglas de negocio ni JPA | Las reglas se aplican igual desde la Estación y desde el sitio |
| 4 | Solo operaciones sin estado, con el solicitante como parámetro | Con varios clientes las operaciones con estado se pisarían; el Central no tiene sesión |
| 5 | Los `DT` se adaptan en el lugar, con `DTFecha` y `List` | Evita copias paralelas; el contrato no depende de `LocalDate` ni de `Set` |
| 6 | Una sola `ReglaNegocioException` que viaja como SOAP Fault | El sitio solo muestra el mensaje; una excepción por regla no aportaba nada |
| 7 | La falta de conexión se resuelve en `web.xml`, no en cada servlet | Una sola página de error sin trazas, sin repetir código |
| 8 | Las imágenes y la constancia viajan como `byte[]`, y las imágenes se piden aparte | Pesan en Base64; un listado no tiene que cargar todas |
| 9 | El WSDL se copia al sitio (`actualizar-wsdl.ps1`) | El sitio compila aunque el Central esté apagado |
| 10 | Configuración en `~/.eventosUy`, con plantilla que se crea sola | Cambiar IP o contraseña no recompila; ninguna dirección está en el código |
| 11 | Un `EntityManager` por operación, bloqueo de la edición y `UPDATE` atómico de visitas | Hay varios clientes a la vez y cada uno tiene que ver la base real |
