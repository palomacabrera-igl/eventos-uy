# eventos.uy

Plataforma de gestión de eventos. Trabajo del **Laboratorio 1** de Programación de Aplicaciones — Tecnólogo en Informática, UTEC.

En esta etapa se desarrollan dos componentes:

- **Servidor Central** — lógica de negocio y persistencia con JPA sobre PostgreSQL.
- **Estación de Trabajo** — interfaz gráfica de administración en Swing.

## Requisitos

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
pom.xml                        Proyecto Maven (Java 25)
```

## Arquitectura

El sistema está organizado en capas:

- **Presentación** (`presentacion`) — Swing. Accede a la lógica únicamente a través de la interfaz `IControladorSistema`.
- **Lógica** (`logica`) — entidades del dominio, controlador y manejadores de las colecciones.
- **Persistencia** (`persistencia`) — encapsula JPA. Ni la presentación ni la lógica de negocio conocen Hibernate.

Los objetos del dominio no cruzan hacia la interfaz gráfica: la comunicación entre capas se hace con tipos de datos (`DT*`).

## Equipo

Paloma Cabrera · Martina Delgado · Sebastián De León · Elías Sosa · Leandro Acosta
