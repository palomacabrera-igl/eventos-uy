# eventos.uy — Servidor Central

Plataforma de gestión de eventos. Trabajo de Programación de Aplicaciones — Tecnólogo en Informática, UTEC.

Este repositorio es el **Servidor Central** y su **Estación de Trabajo**. Los dos se entregan juntos en un solo programa, `servidor.jar`:

- **Servidor Central** (`logica/`): la lógica de negocio y la persistencia con JPA sobre PostgreSQL.
- **Servicio web** (`estacion-trabajo/.../servicios/`): publica las operaciones del Central por SOAP, para que el sitio las use desde otra computadora.
- **Estación de Trabajo** (`estacion-trabajo/`): la interfaz de administración en Swing. Corre en el mismo proceso que el Central y usa `IControladorSistema` directamente.

El **sitio web** (escritorio y celular) vive en otro repositorio, **eventos-uy-web**, y habla con el Central **solo por el servicio web**.

```
 Navegador (computadora o celular)
          |  HTTP, puerto 8080
 +--------v-----------------------+
 | Servidor Web (Tomcat)          |   repo eventos-uy-web
 |   servlets, JSP, sitio /movil  |
 +--------+-----------------------+
          |  SOAP, puerto 9128 (puede ser otra computadora)
 +--------v-----------------------+
 | servidor.jar                   |   este repositorio
 |   servicio web (Publicador)    |
 |   Servidor Central (logica)    |
 |   Estación de Trabajo (Swing)  |
 +--------+-----------------------+
          |  JPA
      PostgreSQL
```

## Por qué son dos repositorios

La letra pide que el Servidor Central y el Servidor Web sean lo más independientes posible. Lo único que comparten es el **contrato del servicio web** (el WSDL): el sitio genera sus clases a partir de él con `wsimport` y no tiene ninguna clase de la lógica ni de la persistencia.

Así cada uno se compila, se prueba y se ejecuta por separado, y pueden estar en computadoras distintas.

## Requisitos

| Herramienta   | Versión                                            |
| ------------- | -------------------------------------------------- |
| JDK           | 25 (Temurin / Adoptium), con `JAVA_HOME` apuntando a él |
| PostgreSQL    | 17                                                 |
| Tomcat        | 11.0.26 (solo para el sitio)                       |
| IntelliJ IDEA | 2026.2+, con el plugin *Swing UI Designer* (solo para editar las pantallas) |

No hace falta instalar Maven: el repositorio incluye el Maven Wrapper (`mvnw` / `mvnw.cmd`).

Los dos repositorios tienen que estar **uno al lado del otro**, en la misma carpeta: `eventos-uy` y `eventos-uy-web`.

## Puesta en marcha

### 1. Crear la base de datos (una sola vez)

Como superusuario (`postgres`). Pide la contraseña que cada uno definió al instalar PostgreSQL.

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -f scripts\crear-base.sql
```

```bash
psql -U postgres -f scripts/crear-base.sql
```

Crea el rol `eventosuy` y la base `eventosuy`. Se puede correr más de una vez sin romper nada. Las **tablas** no se crean acá: las genera Hibernate al arrancar (`hibernate.hbm2ddl.auto=update`).

### 2. Cargar los datos de prueba (una sola vez)

```powershell
.\mvnw.cmd -q -pl logica compile exec:java
```

Termina con `>>> Datos de prueba cargados.` Si ya estaban, avisa y no hace nada. Las imágenes de los datos viajan dentro del programa (`logica/src/main/resources/imagenes/`).

### 3. Compilar todo: el comando único

La letra (7.9) pide un solo comando que genere los dos paquetes. Compila los dos repositorios y deja el resultado en `entrega/`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\construir-todo.ps1
```

```bash
./scripts/construir-todo.sh
```

| Archivo | Qué es |
| --- | --- |
| `entrega/servidor.jar` | Servidor Central + servicio web + Estación de Trabajo |
| `entrega/web.war` | El sitio (escritorio y celular), para Tomcat |

Si el sitio está en otra carpeta: `-RutaSitio C:\ruta\a\eventos-uy-web` (en Linux y macOS, como primer argumento). Con `-ConPruebas` corre también las pruebas antes de empaquetar.

Si el Servidor Central está corriendo desde `entrega\servidor.jar`, hay que cerrarlo antes: Windows no deja reemplazar un archivo abierto.

### 4. Arrancar el Servidor Central

```powershell
java -jar entrega\servidor.jar
```

Prepara la base, publica el servicio web y abre la Estación de Trabajo. En la consola muestra a qué base se conectó, dónde quedó publicado el servicio y la IP de la computadora en la red. La terminal tiene que quedar abierta.

Sin la ventana de la Estación (por ejemplo, en un servidor): `java -jar entrega\servidor.jar --sin-estacion`. Se detiene con Ctrl+C.

### 5. Desplegar el sitio

Se copia `entrega\web.war` a la carpeta `webapps` de Tomcat y queda en http://localhost:8080/web (el sitio para celular, en `/web/movil`). Los detalles están en el README de **eventos-uy-web**.

## Configuración

Nada de direcciones, puertos ni contraseñas queda escrito en el código. Todo está en la carpeta `.eventosUy` del usuario (en Windows, `C:\Users\<usuario>\.eventosUy`). Para cambiar algo se edita el archivo y se reinicia el programa, sin recompilar.

`central.properties` (Servidor Central):

| Clave | Por defecto | Para qué |
| --- | --- | --- |
| `servicio.host` | `localhost` | Dónde escucha el servicio. `localhost` = solo esta computadora; `0.0.0.0` = también las demás de la red. |
| `servicio.puerto` | `9128` | Puerto del servicio web. |
| `servicio.ruta` | `/eventosuy` | Ruta del servicio. |
| `bd.url` | `jdbc:postgresql://localhost:5432/eventosuy` | Dirección de la base. |
| `bd.usuario` | `eventosuy` | Usuario de la base. |
| `bd.password` | `eventosuy` | Contraseña de la base. |

Si el archivo no existe, se crea con estos valores la primera vez. Si existe pero le falta alguna clave (porque se creó con una versión anterior), se le agrega al final sin tocar lo demás.

### Con el sitio en otra computadora

1. En la computadora del Central: `servicio.host=0.0.0.0` y reiniciar. Windows pide permiso en el firewall: permitir en **redes privadas** (la red tiene que figurar como privada).
2. En la computadora del sitio, en `web.properties`: `central.host=<IP de la computadora del Central>` (la muestra la consola del Central al arrancar).
3. Para comprobarlo, desde la computadora del sitio: `http://<IP>:9128/eventosuy?wsdl` tiene que mostrar el contrato.

## El servicio web

La clase `servicios.Publicador` publica 45 operaciones de `IControladorSistema` con JAX-WS. Con el Central andando, el contrato está en:

```
http://localhost:9128/eventosuy?wsdl
```

- Los datos viajan como los `DT*` de la lógica. Las fechas viajan como `DTFecha`, no como `LocalDate`, para que el contrato no dependa de Java.
- Un error de negocio (`ReglaNegocioException`) viaja como *SOAP Fault*, con el mismo mensaje. El sitio lo recibe como `ReglaNegocioException_Exception` y lo muestra.
- Las imágenes y la constancia en PDF viajan como bytes dentro de la respuesta.

Si cambia el contrato (una operación o un DT nuevo), hay que actualizar la copia del WSDL en el sitio: con el Central andando, `scripts\actualizar-wsdl.ps1` en **eventos-uy-web**, y volver a compilar el sitio.

## Pruebas

```powershell
.\mvnw.cmd test
```

Corre las pruebas de la lógica (`logica/src/test/java/`) y las de la Estación y el servicio web (`estacion-trabajo/src/test/java/`), con JUnit 5. El informe de cubrimiento de JaCoCo queda en `logica/target/site/jacoco/index.html`; los datos de prueba no cuentan para el cálculo. Son 590 pruebas: 540 de la lógica y 50 de la Estación y el servicio web. La letra (7.10) pide medir el cubrimiento en IntelliJ con *Run with Coverage*; con JaCoCo la capa lógica da **89 % de líneas** (1808 de 2029) y 81 % de ramas.

- Las pruebas de integración usan la base de `central.properties` y necesitan los datos de prueba cargados. Lo que crean lleva el prefijo `IT` y lo borran al terminar.
- Si PostgreSQL no responde, las de integración se **saltean** solas y el resto corre igual.
- Las pruebas de la asistencia, la constancia, las visitas y la validación están explicadas en [`docs/Laboratorio3-Asistencia-Visitas-y-Defensa.md`](docs/Laboratorio3-Asistencia-Visitas-y-Defensa.md).

## Datos de prueba

| Usuario | Contraseña | Tipo |
| --- | --- | --- |
| `imm` | `Imm2026!` | Organizador |
| `antel` | `Antel2026!` | Organizador |
| `vale23` | `Vale2026!` | Asistente |
| `jperez` | `Juan2026!` | Asistente |
| `lgomez` | `Lucia2026!` | Asistente |

Además de los cinco usuarios, los datos traen lo que pide la letra para recorrer el sistema:

- **Seguidores:** `imm` y `vale23` se siguen entre sí; `vale23` sigue también a `antel`, y `jperez` a `imm`.
- **Video:** JIAP 2026 (una charla de la JIAP 2011, publicada por ASIAP) y Montevideo Rock 2026 (la presentación de Montevideo Rock 2017 de TV Ciudad).
- **Evento finalizado:** Feria del libro.
- **Edición archivada:** Maratón 2025 (solo aparece en el perfil de `imm`). **En condiciones de archivarse:** Comics 2026 (aceptada y ya terminó).

Qué registros tienen asistencia y cuáles no, y las visitas iniciales de cada evento, están en [`docs/Laboratorio3-Asistencia-Visitas-y-Defensa.md`](docs/Laboratorio3-Asistencia-Visitas-y-Defensa.md).

Una demostración cambia la asistencia y las visitas. Para devolverlas a su valor inicial, sin recrear la base:

```powershell
$env:PGPASSWORD = "eventosuy"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U eventosuy -h localhost -d eventosuy -f scripts\restaurar-asistencia-y-visitas.sql
```

## Cuando cambia el esquema de la base

Hibernate arranca con `hibernate.hbm2ddl.auto=update`, que **agrega** lo que falta pero **no rehace** lo que ya existe:

| El cambio | ¿Hay que hacer algo? |
| --- | --- |
| Una columna **nullable** nueva | **No.** Hibernate la agrega sola en el próximo arranque. |
| Una columna **`NOT NULL`** nueva, en una tabla que ya tiene filas | **Sí.** PostgreSQL no tiene con qué llenarla y después fallan las inserciones. |
| Cambiar el tipo, el largo o los valores permitidos de una columna, o borrarla | **Sí.** `update` no modifica ni borra lo que ya existe. Por ejemplo, el estado `ARCHIVADA` en una base creada antes de que existiera. |

Para dejar la base como nueva, sin pedir la contraseña de `postgres` (antes hay que cerrar el Servidor Central y parar Tomcat):

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U eventosuy -h localhost -d eventosuy -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
.\mvnw.cmd -q -pl logica compile exec:java
```

> Quien cambia el esquema lo aclara en el mensaje de su commit y avisa en el grupo, así los demás recrean su base antes de seguir.

## Estructura

```
logica/                              El Servidor Central
  src/main/java/logica/              Dominio, controlador (Sistema), manejadores y DTs
  src/main/java/persistencia/        JPA, configuración (~/.eventosUy) y carga de datos
  src/main/resources/                persistence.xml, plantilla de central.properties, imágenes
  src/test/java/logica/              Pruebas JUnit 5
estacion-trabajo/
  src/main/java/servicios/           Main (arranque) y Publicador (el servicio web)
  src/main/java/presentacion/        Estación de Trabajo: paneles .form + ventanas
  src/test/java/                     Pruebas del servicio web y de la Estación
scripts/construir-todo.ps1 / .sh     El comando único: deja servidor.jar y web.war en entrega/
scripts/crear-base.sql               Creación del rol y la base
scripts/restaurar-asistencia-y-visitas.sql   Deja la demostración como al principio
docs/                                Análisis y diseño, guion de defensa y registro de horas
pom.xml                              POM padre: los dos módulos (Java 25)
```

## Arquitectura

El sistema está organizado en capas:

- **Presentación**: la Estación de Trabajo (Swing) y el servicio web (`Publicador`), en este repositorio; el sitio, en el otro. Las tres acceden a la lógica únicamente a través de `IControladorSistema`.
- **Lógica** (`logica`): entidades del dominio, controlador y manejadores de las colecciones.
- **Persistencia** (`persistencia`): encapsula JPA. Ni la presentación ni la lógica de negocio conocen Hibernate.

Los objetos del dominio no cruzan hacia la presentación: la comunicación entre capas se hace con tipos de datos (`DT*`), que son también los que viajan por el servicio web.

Cada operación del controlador abre un `EntityManager` nuevo, lo usa y lo cierra (commit si salió bien, rollback si falló). Así cada operación lee la base de verdad: lo que cambia el sitio se ve en la Estación de Trabajo sin un botón de «Actualizar». La Estación vuelve a leer sus listas sola cada 5 segundos.

## Documentación

La documentación de análisis y diseño está en [`docs/`](docs/README.md):

- **[Diseño de la solución distribuida](docs/Laboratorio3-Diseno-distribuido.md)**: operaciones publicadas, datatypes, excepciones remotas, configuración y empaquetado.
- **[Video, búsqueda y versión para celular](docs/Laboratorio3-Video-Busqueda-Celular.md)**: diseño de esas tres partes.
- **[Diseño de la solución web](docs/DisenioSolucionWeb.md)**: separación MVC, sesión, imágenes y estados de la edición (Laboratorio 2).
- **[Asistencia, constancia, visitas y validación](docs/Laboratorio3-Asistencia-Visitas-y-Defensa.md)**: datos de prueba, pruebas y guion de defensa de esa parte.
- Modelo de dominio y diagrama de clases de diseño.
- Diagramas de secuencia del sistema y de comunicación.
- Registro de horas del equipo.

Los diagramas están en **PDF**, para leerlos directamente, y en formato **draw.io** (`.drawio.xml`), para editarlos.

## Equipo

Paloma Cabrera · Martina Delgado · Sebastián De León · Leandro Acosta
