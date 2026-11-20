# Rol 4 · asistencia, constancia, visitas y validación

Qué hay en este archivo, para quien arme el README y la defensa:

| Parte | Para qué sirve |
| --- | --- |
| [A. Para el README](#a-para-el-readme) | Texto listo para pegar en el README del proyecto: los datos de prueba de mi parte, cómo correr sus pruebas y cómo dejar la demostración como al principio. |
| [B. Guion de defensa](#b-guion-de-defensa) | Qué mostrar y en qué orden, con qué usuarios y qué tiene que verse. |
| [C. Preguntas probables](#c-preguntas-probables-en-la-defensa) | Qué contestar y en qué archivo está, porque en la defensa no hay IDE. |
| [D. Antes de empezar](#d-lista-para-chequear-antes-de-empezar) | Lista corta para chequear en la computadora, antes de que empiece la defensa. |

---

## A. Para el README

### Datos de prueba de asistencia y visitas

`DatosDePrueba` (sección «Rol 4») carga registros con y sin asistencia, y visitas por evento, para recorrer todo sin armar nada a mano. La letra los pide: «registros con y sin asistencia».

| Edición | Organizador | Asistente | ¿Asistió? | Para qué sirve |
| --- | --- | --- | --- | --- |
| JIAP 2026 | `imm` | `vale23` | sí | descargar la constancia |
| JIAP 2026 | `imm` | `lgomez` | no | el organizador ve quién fue y quién no |
| Comics 2026 | `imm` | `jperez` | sí | |
| Comics 2026 | `imm` | `lgomez` | no | registrar la asistencia desde el celular |
| Maratón 2026 | `imm` | `vale23` | sí | |
| Maratón 2026 | `imm` | `jperez` | no | **registrar la asistencia desde el celular** (la edición ya empezó) |
| Montevideo Rock 2026 | `antel` | `vale23` | no | todavía no empezó: no se puede registrar la asistencia |
| Libro1 | `antel` | nadie | | edición que ya empezó, sin registros |

Visitas iniciales, para que el ranking de «Eventos más visitados» tenga sentido:

| Evento | Visitas |
| --- | --- |
| Montevideo Rock | 24 |
| JIAP | 17 |
| Maratón de Montevideo | 12 |
| Montevideo Comics | 7 |
| Feria del libro | 3 |
| Expo Negocios UY | 0 (no entra al ranking hasta que alguien lo visite) |

Los usuarios y sus contraseñas están en la pantalla de inicio de sesión del sitio.

> La carga solo corre si todavía no existe el usuario `vale23`. Para ver estos datos con una base que ya tenía los anteriores, hay que **recrear la base** y volver a cargar.

### Pruebas de asistencia, constancia, visitas y validación

Las pruebas de integración necesitan PostgreSQL andando y los datos de prueba cargados; si no, se saltean solas.

```powershell
.\mvnw.cmd -pl logica test
```

| Clase de pruebas | Qué cubre |
| --- | --- |
| `RegistroAsistenciaTest` | Las reglas de la asistencia sin base de datos: edición Aceptada y empezada, una sola vez, valores límite de las fechas. |
| `RegistroAsistenciaIntegracionTest` | Lo mismo contra la base, más los casos de edición archivada y la constancia de punta a punta. |
| `ConstanciaAsistenciaTest` | El PDF: que es válido, de una página, con nombre, evento, edición, ciudad, fechas y fecha de emisión. |
| `VisitasIntegracionTest` | El contador atómico (10 hilos × 10 visitas, ninguna se pierde) y el ranking: orden, empates, límite. |
| `ValidacionDisponibilidadIntegracionTest` | `existeNickname` y `existeCorreo`: espacios, mayúsculas, vacío, coherencia con el alta. |

La ventana de la Estación se prueba en `estacion-trabajo` y no necesita base:

```powershell
.\mvnw.cmd -pl estacion-trabajo -am test "-Dtest=VentanaEventosMasVisitadosTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

El informe de cubrimiento queda en `logica/target/site/jacoco/index.html`. Los datos de prueba están excluidos del cálculo: la letra pide el cubrimiento de la capa lógica.

### Dejar la demostración como al principio

Una demostración cambia datos: la asistencia que se registra desde el celular y las visitas que suma el sitio. Este script los devuelve a su valor inicial, sin recrear la base. No borra registros, usuarios ni eventos: solo toca `registro.asistio` y `evento.visitas`. Se puede correr las veces que haga falta.

```powershell
$env:PGPASSWORD = "eventosuy"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U eventosuy -h localhost -d eventosuy -f scripts\restaurar-asistencia-y-visitas.sql
```

Si el grupo cambia esos datos en `DatosDePrueba`, hay que cambiarlos también en el script.

---

## B. Guion de defensa

Unos 8 minutos. Se asume: el Servidor Central y la Estación en una computadora, el sitio en otra, y el celular en la misma red. Antes de empezar, correr el script de restauración.

> Los pasos del celular dependen de las pantallas del Rol 3. Si la ruta o el texto de un botón cambia, ajustar el paso.

### 1. Eventos más visitados (Estación de Trabajo)

1. En la Estación: **Eventos y Ediciones → Consulta de Eventos más Visitados**.
2. Tiene que verse una tabla de cinco filas: Montevideo Rock 24, JIAP 17, Maratón de Montevideo 12, Montevideo Comics 7 y Feria del libro 3. Expo Negocios no aparece: no tiene visitas.
3. **Dejar la ventana abierta.** Se actualiza sola cada 5 segundos.

### 2. Visitas (sitio de escritorio, y celular si está la ruta)

1. En el sitio, sin iniciar sesión, abrir el evento «Expo Negocios UY» desde la cartelera. Recargar la página tres veces más (cuatro visitas en total).
2. Volver a la Estación, sin tocar nada: en unos segundos **Expo Negocios UY entra al ranking con 4 visitas y Feria del libro sale** (tiene 3).
3. Opcional: abrir el mismo evento desde el celular (`/movil/evento?nombre=...`). Cuenta igual: el filtro intercepta las dos rutas.

### 3. Verificación de nickname y correo (sitio de escritorio)

1. **Registrarse**. En *Nickname* escribir `vale23` y esperar un segundo: aparece «El nickname ya está en uso» en rojo, sin recargar la página.
2. Cambiarlo por `vale24`: pasa a «El nickname está disponible» en verde.
3. En *Correo*, escribir el de Valentina (`valentina@mail.com`): ocupado. Uno nuevo: disponible.
4. Con un nickname ocupado, apretar **Crear cuenta**: el navegador no deja enviar y dice por qué.
5. Si hace falta mostrar la petición: en el navegador, abrir `/disponibilidad?campo=nickname&valor=vale23`. Responde `{"disponible":false}`.

### 4. Registro de asistencia (celular)

1. En el celular iniciar sesión como `jperez` (asistente sin institución).
2. Ir a su registro en **Maratón 2026** (la edición ya empezó). Tiene que ofrecer **Registrar asistencia**. Confirmar.
3. Volver a abrir el registro: ahora dice que la asistencia está registrada y **no** ofrece registrarla otra vez.
4. Cerrar sesión e iniciar como `vale23`. Su registro en **Montevideo Rock 2026** (empieza más adelante) no permite registrar la asistencia. Si se fuerza, el servidor la rechaza con un mensaje.

### 5. Constancia de asistencia (sitio de escritorio)

1. Iniciar sesión como `jperez` y abrir **Mis registros → Maratón 2026**. Ahora dice «Asistencia: Registrada» y aparece **Descargar constancia (PDF)**.
2. Descargarla. Se abre un PDF de una página con: nombre y apellido, evento, edición, ciudad, fechas y fecha de emisión (la de hoy).
3. Con `lgomez`, en **Comics 2026** o **JIAP 2026**: dice «Todavía no registrada» y no hay botón. Si se pide la dirección a mano, el servidor contesta que todavía no registró su asistencia.

### 6. Vista del organizador

1. Iniciar sesión como `imm` y abrir **Registros → JIAP 2026**.
2. Cada fila dice si el asistente fue: `@vale23 · asistió` y `@lgomez · sin asistencia`.
3. En **Maratón 2026** se ve `@jperez · asistió`: es la que se registró hace un momento desde el celular.

### 7. Al terminar

Correr el script de restauración (parte A) para dejar todo listo para la próxima vez.

---

## C. Preguntas probables en la defensa

**¿Cómo evitan perder visitas cuando muchas personas consultan a la vez?**
El contador no se lee y se vuelve a guardar: se incrementa con un solo `UPDATE evento SET visitas = visitas + 1`, y la base traba la fila mientras lo hace. Hay una prueba con 10 hilos que suman 10 visitas cada uno y comprueba que llegan las 100. Está en `ManejadorEvento.sumarVisita` y en `VisitasIntegracionTest`.

**¿Por qué el contador no se pierde si alguien edita el evento a la vez?**
La columna `visitas` es `updatable = false` en la entidad: cuando Hibernate guarda un cambio del evento, no incluye esa columna y no pisa las visitas que entraron en el medio.

**¿Cómo se cuentan las visitas sin tocar los servlets?**
Con un filtro (`jakarta.servlet.Filter`), `FiltroVisitas`, aplicado a `/evento` y a `/movil/evento`. Atiende la consulta y, cuando la página ya salió bien, le avisa al Servidor Central con `registrarVisita`. Si el Central no responde o el evento no existe, lo anota en el registro del servidor y la persona no se entera. El conteo se guarda en el Central, así que sobrevive a reiniciar el sitio.

**¿Por qué `listarEventosMasVisitados` no está en el servicio web?**
Solo la usa la Estación de Trabajo, que llama al controlador directamente, en el mismo programa. El profesor confirmó que se publican solo las operaciones que usan el sitio y el celular.

**¿Dónde y cómo se genera el PDF?**
En el Servidor Central, en el momento de pedirlo, con OpenPDF. Viaja como `byte[]` (Base64 dentro del XML, como las imágenes) y el sitio lo entrega con el tipo `application/pdf`. No se guarda ningún archivo. Está en `ConstanciaAsistencia`. La regla de quién la recibe vive en la lógica: solo quien está registrado y ya registró su asistencia.

**¿Quién puede registrar la asistencia, y cuándo?**
Solo un asistente registrado en esa edición, una sola vez, con la edición **Aceptada** y que **ya empezó**. Una edición archivada la rechaza. Si el asistente ya la había registrado, el mensaje es ese, aunque la edición se archive después. Está en `EdicionEvento.registrarAsistencia`.

**¿La verificación del nickname reemplaza la validación del alta?**
No. Solo adelanta el aviso. El Servidor Central sigue rechazando un nickname o un correo repetido en `altaUsuario`, y la base tiene restricciones únicas por si dos personas eligen el mismo a la vez. Si la verificación falla (Central caído), no se muestra nada y no se frena el envío.

**¿Por qué `fetch` con `async/await` y no `XMLHttpRequest`?**
Es la API actual del navegador. Con `await` la función espera la respuesta sin congelar la página. Además espera una pausa después de la última tecla para no hacer una petición por letra, y descarta la respuesta de un valor viejo si la persona ya escribió otro. Está en `assets/js/disponibilidad.js`.

**¿Qué pasa si el Servidor Central se cae mientras se usa el sitio?**
Una llamada que no responde lanza `WebServiceException` y `web.xml` la manda a una página propia («el servicio no está disponible»), sin trazas. Los errores de negocio, en cambio, llegan como SOAP Fault y se muestran con su mensaje.

**¿Cómo se actualiza sola la ventana de la Estación?**
La ventana principal tiene un temporizador que cada 5 segundos llama a `actualizar()` en las ventanas abiertas. La de más visitados vuelve a pedir el ranking y solo toca la tabla si cambió. Está en `VentanaEventosMasVisitados`.

### Dónde está cada cosa

| Qué | Archivo |
| --- | --- |
| Regla de asistencia y constancia | `logica/src/main/java/logica/EdicionEvento.java` (sección «Rol 4: asistencia») |
| PDF | `logica/src/main/java/logica/ConstanciaAsistencia.java` |
| Contador y ranking | `logica/src/main/java/logica/ManejadorEvento.java` (sección «Rol 4: visitas») |
| Verificación de nickname y correo | `logica/src/main/java/logica/Sistema.java` y `ManejadorUsuario.java` |
| Ventana de más visitados | `estacion-trabajo/src/main/java/presentacion/VentanaEventosMasVisitados.java` |
| Filtro de visitas | `eventos-uy-web/src/main/java/servlets/FiltroVisitas.java` |
| Servlet de verificación y JavaScript | `servlets/Disponibilidad.java` y `assets/js/disponibilidad.js` |
| Descarga de la constancia | `servlets/DescargarConstancia.java` |
| Documentación de análisis | `docs/Laboratorio3-Rol4.drawio.pdf` |

---

## D. Lista para chequear antes de empezar

- [ ] La base tiene los datos de la parte A (si no, recrearla y cargar), y se corrió el script de restauración.
- [ ] El Servidor Central está publicado y se ve su contrato (`?wsdl`) desde la computadora del sitio. Con `servicio.host=localhost` el sitio, en otra computadora, **no** lo alcanza: poner la IP de la computadora del Central.
- [ ] La Estación de Trabajo abrió después de publicar el servicio.
- [ ] El sitio muestra la cartelera, y el celular abre el sitio por la IP de la computadora del sitio.
- [ ] La pantalla de inicio de sesión dice que `lgomez` es «Asistente de UTEC (sin registros)», pero ahora tiene registros: corregir ese texto en `login.jsp`.
- [ ] No correr `mvnw install` en el repositorio del Central: si hay un `logica-1.0-SNAPSHOT.jar` viejo en `~/.m2`, tapa al nuevo y el Central arranca con la lógica de otra fecha.
