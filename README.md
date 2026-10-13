# eventos.uy

Plataforma de gestión de eventos. Trabajo de Programación de Aplicaciones — Tecnólogo en Informática, UTEC.

El sistema tiene tres componentes:

- **Sitio web** — lo que usan visitantes, asistentes y organizadores desde el navegador. Es la **Parte 1 del Laboratorio 2**: front-end con HTML, CSS y JavaScript sobre Bootstrap 5, con datos fijos.
- **Servidor Central** — lógica de negocio y persistencia con JPA sobre PostgreSQL. Laboratorio 1.
- **Estación de Trabajo** — interfaz gráfica de administración en Swing. Laboratorio 1.

> Para ver el sitio web alcanza con abrir **`web/index.html`** en el navegador. No hace falta instalar nada, ni base de datos, ni servidor, ni conexión a internet.

## El sitio web — Laboratorio 2, Parte 1

Abrí `web/index.html` con doble clic, o desde IntelliJ con el botón del navegador que aparece arriba a la derecha del editor.

Bootstrap y las tipografías están incluidos en `web/assets/`, así que el sitio se ve igual con la computadora desconectada.

### Qué incluye

Los **16 casos de uso de la Sección 6.2** de la letra, navegables desde la página principal y distinguidos por rol. Los datos son fijos y viven en un solo archivo: `web/js/datos.js`.

### Usuarios de prueba

La sesión se simula en el navegador. Para iniciarla, **Iniciar sesión** en el cabezal. Estos mismos usuarios están listados debajo del formulario de login, para no tener que volver acá.

| Usuario | Contraseña | Rol | Para qué sirve |
| --- | --- | --- | --- |
| `vale23` | `Vale2026!` | Asistente | De UTEC. Registrada en JIAP 2026 con código de patrocinio, y en Montevideo Rock 2026 |
| `lgomez` | `Lucia2026!` | Asistente | De UTEC, sin ningún registro |
| `jperez` | `Juan2026!` | Asistente | **Sin institución**: solo puede registrarse de forma general |
| `imm` | `Imm2026!` | Organizador | Seis ediciones, una en cada estado |
| `antel` | `Antel2026!` | Organizador | Para comprobar que un organizador no ve las ediciones de otro |

La sesión vive en el navegador y no en la pestaña: si se inicia sesión en una pestaña, cambia en todas. Para comparar dos usuarios a la vez conviene usar una ventana de incógnito.

### Qué todavía no hace

En esta parte el sitio **no se integra con la lógica ni se despliega en Tomcat**, como indica la letra. Los formularios validan en el navegador y muestran el mensaje de resultado, pero no guardan nada: al recargar la página, los datos vuelven a ser los de `datos.js`. La integración con el Servidor Central es la Parte 2.

### Estructura de la carpeta

```
web/index.html                   Cartelera: eventos y usuarios
web/*.html                       Una página por caso de uso (15 en total)
web/js/datos.js                  Único archivo de datos fijos del sitio
web/assets/js/cabezal.js         Cabezal, categorías y pie, compartidos por las 15 páginas
web/assets/css/custom.css        Estilos propios
web/assets/css/fuentes.css       Declaración de las tipografías locales
web/assets/vendor/bootstrap/     Bootstrap 5.3.3
web/assets/fonts/                Tipografías Archivo y JetBrains Mono
web/public/images/               Imágenes de eventos, ediciones, usuarios e instituciones
```

## Requisitos

Lo que sigue es para el **Servidor Central y la Estación de Trabajo**. El sitio web no necesita nada de esto.

| Herramienta   | Versión                                                |
| ------------- | ------------------------------------------------------ |
| JDK           | 25 (Temurin / Adoptium)                                |
| PostgreSQL    | 17                                                     |
| IntelliJ IDEA | 2026.2+ (con el plugin *Swing UI Designer* activo)     |

No hace falta instalar Maven: el repositorio incluye el Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Puesta en marcha

Cuatro pasos. Los tres primeros se hacen una sola vez por máquina.

### 1. Crear la base de datos

El repositorio incluye el script de creación. Hay que ejecutarlo como superusuario (`postgres`):

**Windows**

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -f scripts\crear-base.sql
```

**macOS / Linux**

```bash
psql -U postgres -f scripts/crear-base.sql
```

Pide la contraseña del usuario `postgres`, la que cada uno definió al instalar PostgreSQL. El script crea el rol `eventosuy` y la base `eventosuy`, y se puede correr más de una vez sin romper nada.

> Las **tablas** no se crean en este paso: las genera Hibernate automáticamente en el primer arranque, porque `persistence.xml` usa `hibernate.hbm2ddl.auto=update`.

### 2. Compilar

**Windows**

```powershell
.\mvnw.cmd package
```

**macOS / Linux**

```bash
./mvnw package
```

Genera `target/eventos-uy-1.0-SNAPSHOT.jar`.

### 3. Cargar los datos de prueba

Deja en la base un conjunto inicial de usuarios, eventos, ediciones, tipos de registro y patrocinios para poder recorrer los casos de uso:

```powershell
.\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.CargarDatos"
```

Es idempotente: si los datos ya están, avisa y no hace nada. También se puede ejecutar la clase `persistencia.CargarDatos` desde IntelliJ.

### 4. Ejecutar la aplicación

```powershell
.\mvnw.cmd exec:java
```

O desde IntelliJ, ejecutando la clase `presentacion.VentanaPrincipal`.

## Conexión a la base

Credenciales que usa el sistema, definidas en `src/main/resources/META-INF/persistence.xml`:

| Parámetro  | Valor       |
| ---------- | ----------- |
| host       | `localhost` |
| puerto     | `5432`      |
| base       | `eventosuy` |
| usuario    | `eventosuy` |
| contraseña | `eventosuy` |

Sirven igual para conectarse desde pgAdmin o DBeaver.

## Estructura

```
src/main/java/logica/          Dominio, controlador (Sistema), manejadores y DTs
src/main/java/persistencia/    Acceso a JPA (Persistencia) y carga de datos
src/main/java/presentacion/    Interfaz gráfica Swing (paneles .form + ventanas)
src/main/resources/META-INF/   persistence.xml (configuración de JPA)
scripts/crear-base.sql         Creación del rol y la base en PostgreSQL
web/                           Sitio web del Laboratorio 2 (Parte 1)
docs/                          Documentación de análisis y diseño, y registro de horas
pom.xml                        Proyecto Maven (Java 25)
```

## Arquitectura

El sistema está organizado en capas:

- **Presentación** (`presentacion`) — Swing. Accede a la lógica únicamente a través de la interfaz `IControladorSistema`.
- **Lógica** (`logica`) — entidades del dominio, controlador y manejadores de las colecciones.
- **Persistencia** (`persistencia`) — encapsula JPA. Ni la presentación ni la lógica de negocio conocen Hibernate.

Los objetos del dominio no cruzan hacia la interfaz gráfica: la comunicación entre capas se hace con tipos de datos (`DT*`).

## Documentación

La documentación de análisis y diseño está en [`docs/`](docs/README.md):

- Modelo de dominio y diagrama de clases de diseño.
- Diagramas de secuencia del sistema (DSS) y diagramas de comunicación de los 15 casos de uso.
- Registro de horas del equipo.

Los diagramas están en **PDF**, para leerlos directamente, y en formato **draw.io** (`.drawio.xml`),
para abrirlos con zoom o editarlos con draw.io.

## Equipo

Paloma Cabrera · Martina Delgado · Sebastián De León · Elías Sosa · Leandro Acosta
