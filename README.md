eventos.uy
Plataforma de gestión de eventos. Trabajo del Laboratorio 1 de Programación de Aplicaciones (UTEC).

En esta etapa: Servidor Central (lógica + persistencia JPA) y Estación de Trabajo (interfaz de administración en Swing).

Requisitos
JDK 25 (Temurin / Adoptium)
PostgreSQL 17
IntelliJ IDEA 2026.2+ (para desarrollo; con el plugin Swing UI Designer activo)
No hace falta instalar Maven: el repositorio incluye el Maven Wrapper (mvnw / mvnw.cmd).

Base de datos
La aplicación se conecta a una base PostgreSQL local. Cada integrante la crea una vez, conectado como superusuario (postgres):

CREATE ROLE eventos WITH LOGIN PASSWORD 'eventos';
CREATE DATABASE eventosuy OWNER eventos;
GRANT ALL PRIVILEGES ON DATABASE eventosuy TO eventos;
Credenciales que usa el sistema (definidas en src/main/resources/META-INF/persistence.xml):

Parámetro	Valor
host	localhost
puerto	5432
base	eventosuy
usuario	eventos
contraseña	eventos
Las tablas se crean automáticamente en el primer arranque (hibernate.hbm2ddl.auto=update). No hace falta correr scripts de creación de tablas.

La integración con JPA está en curso. Hasta que esté completa, la aplicación usa datos de prueba en memoria y no requiere la base de datos.

Compilar
En Windows:

.\mvnw.cmd package
En macOS / Linux:

./mvnw package
Genera target/eventos-uy-1.0-SNAPSHOT.jar.

Ejecutar
Con Maven, sin abrir el IDE:

.\mvnw.cmd exec:java
O desde IntelliJ: ejecutar la clase presentacion.VentanaPrincipal.

Estructura
src/main/java/logica/          Dominio y controlador (Sistema, entidades, DTs)
src/main/java/presentacion/    Interfaz gráfica Swing (paneles .form + ventanas)
src/main/resources/META-INF/   persistence.xml (configuración de JPA)
docs/                          Documentación de análisis y diseño
pom.xml                        Proyecto Maven (Java 25)
La Estación de Trabajo accede a la lógica únicamente a través de la interfaz IControladorSistema. Los objetos de dominio no cruzan a la interfaz: se usan tipos de datos (DT*).

Equipo:
Paloma Cabrera, Martina Delgado, Sebastián De León, Elías Sosa, Leandro Acosta.