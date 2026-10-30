# eventos.uy — Servidor Central

Plataforma de gestión de eventos. Trabajo de Programación de Aplicaciones — Tecnólogo en Informática, UTEC.

Este repositorio es el **Servidor Central** y su **Estación de Trabajo**:

- **Servidor Central** (`logica/`) — lógica de negocio y persistencia con JPA sobre PostgreSQL. Se empaqueta como un `.jar`.
- **Estación de Trabajo** (`estacion-trabajo/`) — la interfaz de administración en Swing. Habla con la lógica únicamente a través de `IControladorSistema`.

El **sitio web** no está acá: vive en su propio repositorio, **eventos-uy-web**, y recibe la lógica ya compilada.

```
                     +-------------------------------+
 Cliente Web  ---->  | Servidor Web (JSP y Servlets) |   repo eventos-uy-web
                     |  +-------------------------+  |
                     |  | Servidor Central (JAR)  |  |  <-- este repositorio
                     |  +-------------------------+  |
                     +---------------+---------------+
                                     | JPA
 Estación de Trabajo --------------->+--------> PostgreSQL
 (Swing, este repositorio)
```

## Por qué son dos repositorios

Porque es lo que pide el diagrama de distribución de la letra (Figura 1): el Servidor Central **viaja embebido adentro** del Servidor Web, como una biblioteca, no como código que se compila junto con él.

Mientras fueron un solo proyecto Maven, el sitio compilaba la lógica desde el código fuente. Eso parecía lo mismo y no lo es: borraba el límite entre los dos componentes, y el sitio no podía usar una versión distinta de la que estaba abierta en el editor.

Ahora:

- Acá se compila la lógica y sale `logica-1.0-SNAPSHOT.jar`.
- `scripts/publicar-logica.ps1` deja ese JAR, con su ficha de dependencias, dentro del repositorio del sitio.
- Allá, Maven lo toma como una biblioteca cualquiera y lo mete en el WAR, en `WEB-INF/lib`.

El repositorio del sitio **no tiene ni una clase** de la lógica ni de la persistencia.

### Son dos procesos, con un EntityManager cada uno

El Servidor Web corre dentro de Tomcat y la Estación de Trabajo es una aplicación de escritorio: son dos JVM distintas. Cada una levanta su propio `EntityManagerFactory` a partir del mismo `persistence.xml`, y las dos escriben en la misma base. No comparten ni sesión ni caché de JPA.

## Requisitos

| Herramienta   | Versión                                            |
| ------------- | -------------------------------------------------- |
| JDK           | 25 (Temurin / Adoptium)                            |
| PostgreSQL    | 17                                                 |
| IntelliJ IDEA | 2026.2+ (con el plugin *Swing UI Designer* activo) |

No hace falta instalar Maven: el repositorio incluye el Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Puesta en marcha

### 1. Crear la base de datos

Una sola vez por máquina, como superusuario (`postgres`):

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -f scripts\crear-base.sql
```

```bash
psql -U postgres -f scripts/crear-base.sql
```

Pide la contraseña del usuario `postgres`, la que cada uno definió al instalar PostgreSQL. Crea el rol `eventosuy` y la base `eventosuy`, y se puede correr más de una vez sin romper nada.

> Las **tablas** no se crean en este paso: las genera Hibernate en el primer arranque, porque `persistence.xml` usa `hibernate.hbm2ddl.auto=update`.

### 2. Compilar

```powershell
.\mvnw.cmd install -DskipTests
```

Genera `logica/target/logica-1.0-SNAPSHOT.jar` y `estacion-trabajo/target/estacion-trabajo-1.0-SNAPSHOT.jar`.

### 3. Cargar los datos de prueba

```powershell
.\mvnw.cmd -pl logica exec:java
```

Deja en la base usuarios, eventos, ediciones, tipos de registro y patrocinios para poder recorrer los casos de uso. Es idempotente: si ya están, avisa y no hace nada. También se puede ejecutar `persistencia.CargarDatos` desde IntelliJ.

Las imágenes de esos datos viajan dentro del JAR, en `logica/src/main/resources/imagenes/`, así que funciona igual desde IntelliJ, desde Maven o desde el JAR empaquetado.

### 4. Ejecutar la Estación de Trabajo

```powershell
.\mvnw.cmd -pl estacion-trabajo exec:java
```

O desde IntelliJ, ejecutando `presentacion.VentanaPrincipal`.

## Publicar la lógica en el sitio

Cada vez que cambia algo de `logica/`:

```powershell
.\scripts\publicar-logica.ps1
```

Compila la lógica y deja en el repositorio del sitio, dentro de `lib/`, cuatro archivos: el JAR, su ficha de dependencias (`.pom`) y los dos checksums. Después hay que commitearlos **allá**.

Por defecto busca el repositorio del sitio en una carpeta `eventos-uy-web` al lado de esta. Si está en otro lado:

```powershell
.\scripts\publicar-logica.ps1 -Destino C:\ruta\a\eventos-uy-web
```

> La ficha `.pom` es la que se olvida siempre: es la que le dice a Maven que la lógica necesita Hibernate y el driver de PostgreSQL. Sin ella el sitio compila, arranca, y revienta en el primer acceso a la base. El script la genera leyendo el `pom.xml` de la lógica, así que si mañana se agrega una biblioteca, aparece sola.

## Pruebas

```powershell
.\mvnw.cmd -pl logica test
```

Las pruebas están en `logica/src/test/java/` y usan JUnit 5. El mismo comando genera el informe de cubrimiento de JaCoCo en `logica/target/site/jacoco/index.html` (letra, 7.5).

Las pruebas de integración necesitan PostgreSQL andando. Si no responde, se **saltean** solas y el resto corre igual: no hace falta configurar nada para compilar.

Una prueba nueva refleja el paquete de la clase probada: las de `logica/` van en `logica/src/test/java/logica/` y se llaman `NombreDeLaClaseTest.java`.

## Cuando cambia el esquema de la base

Hibernate arranca con `hibernate.hbm2ddl.auto=update`, que **agrega** lo que falta pero **no rehace** lo que ya existe:

| El cambio | ¿Hay que hacer algo? |
| --- | --- |
| Una columna **nullable** nueva | **No.** Hibernate la agrega sola en el próximo arranque. |
| Una columna **`NOT NULL`** nueva, en una tabla que ya tiene filas | **Sí.** PostgreSQL no tiene con qué llenarla, así que la columna no se crea y después fallan las inserciones. |
| Cambiar el tipo o el largo de una columna, o borrarla | **Sí.** `update` no modifica ni borra lo que ya existe. |

Para dejar la base como nueva, sin pedir la contraseña de `postgres`:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U eventosuy -d eventosuy -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
.\mvnw.cmd -pl logica exec:java
```

Pide la contraseña `eventosuy`. Antes hay que parar Tomcat y cerrar la Estación de Trabajo, para que suelten las conexiones.

> Quien cambia el esquema lo aclara en el mensaje de su commit y avisa en el grupo, así los demás recrean su base antes de seguir. Si no, el síntoma típico es un error de Hibernate al guardar, en una tabla que al resto le funciona bien.

## Conexión a la base

Lo que usa el sistema, definido en `logica/src/main/resources/META-INF/persistence.xml`:

| Parámetro  | Valor       |
| ---------- | ----------- |
| host       | `localhost` |
| puerto     | `5432`      |
| base       | `eventosuy` |
| usuario    | `eventosuy` |
| contraseña | `eventosuy` |

Sirve igual para conectarse desde pgAdmin o DBeaver.

## Estructura

```
logica/                           JAR: el Servidor Central
  src/main/java/logica/           Dominio, controlador (Sistema), manejadores y DTs
  src/main/java/persistencia/     Acceso a JPA y carga de datos
  src/main/resources/META-INF/    persistence.xml
  src/main/resources/imagenes/    Imágenes de los datos de prueba
  src/test/java/logica/           Pruebas JUnit 5
estacion-trabajo/                 JAR: interfaz Swing (paneles .form + ventanas)
scripts/crear-base.sql            Creación del rol y la base
scripts/publicar-logica.ps1       Deja el JAR en el repositorio del sitio
docs/                             Análisis y diseño, y registro de horas
pom.xml                           POM padre: los dos módulos (Java 25)
```

## Arquitectura

El sistema está organizado en capas:

- **Presentación** — Swing acá, servlets y JSP en el otro repositorio. Las dos acceden a la lógica únicamente a través de `IControladorSistema`.
- **Lógica** (`logica`) — entidades del dominio, controlador y manejadores de las colecciones.
- **Persistencia** (`persistencia`) — encapsula JPA. Ni la presentación ni la lógica de negocio conocen Hibernate.

Los objetos del dominio no cruzan hacia la presentación: la comunicación entre capas se hace con tipos de datos (`DT*`).

La letra, en 7.4: *«Toda consulta o modificación de datos del sitio pasa por un caso de uso de esa biblioteca»*.

## Documentación

La documentación de análisis y diseño está en [`docs/`](docs/README.md):

- Modelo de dominio y diagrama de clases de diseño.
- Diagramas de secuencia del sistema (DSS) y diagramas de comunicación.
- Registro de horas del equipo.

Los diagramas están en **PDF**, para leerlos directamente, y en formato **draw.io** (`.drawio.xml`), para abrirlos con zoom o editarlos.

## Equipo

Paloma Cabrera · Martina Delgado · Sebastián De León · Elías Sosa · Leandro Acosta
