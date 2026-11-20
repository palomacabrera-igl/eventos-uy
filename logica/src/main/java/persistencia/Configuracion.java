package persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * La configuracion del Servidor Central, leida de ~/.eventosUy/central.properties.
 *
 * La letra pide que ninguna direccion, puerto ni credencial quede escrita en el
 * codigo: cada computadora tiene la suya y se cambia editando el archivo, sin
 * recompilar. Aca estan las dos cosas que necesita el Servidor Central:
 *
 * <ul>
 *   <li>Donde publicar el servicio web (servicio.host, servicio.puerto, servicio.ruta).
 *       Lo usa servicios.Main.</li>
 *   <li>Como conectarse a PostgreSQL (bd.url, bd.usuario, bd.password). Lo usa
 *       Persistencia al crear la fabrica de EntityManager: por eso esta clase
 *       vive en la logica, y no en la Estacion, porque la logica tambien se usa
 *       sin el Main (las pruebas y la carga de datos de prueba) y todos tienen
 *       que leer el mismo archivo.</li>
 * </ul>
 *
 * Si el archivo no existe, se crea copiando la plantilla que viaja dentro del
 * programa (src/main/resources/central.properties). Si existe pero le falta
 * alguna clave de la plantilla (por ejemplo, porque se creo con una version
 * anterior), se le agrega al final, sin tocar lo que la persona ya edito.
 */
public final class Configuracion {

    /** La carpeta de configuracion de eventos.uy, en la carpeta del usuario. */
    public static final Path CARPETA = Path.of(System.getProperty("user.home"), ".eventosUy");

    /** El archivo del Servidor Central. */
    public static final Path ARCHIVO = CARPETA.resolve("central.properties");

    private static final String PLANTILLA = "/central.properties";

    private final Properties valores;

    private Configuracion(Properties valores) {
        this.valores = valores;
    }

    /** Lee el archivo; lo crea o lo completa desde la plantilla si hace falta. */
    public static synchronized Configuracion cargar() {
        try {
            String plantilla = leerPlantilla();
            if (Files.notExists(ARCHIVO)) {
                Files.createDirectories(CARPETA);
                Files.writeString(ARCHIVO, plantilla, StandardCharsets.UTF_8);
            } else {
                completarClavesQueFalten(plantilla);
            }
            Properties valores = new Properties();
            try (Reader lector = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
                valores.load(lector);
            }
            return new Configuracion(valores);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuracion " + ARCHIVO, e);
        }
    }

    // ===== El servicio web =====

    /** La direccion donde se publica el servicio, por ejemplo http://localhost:9128/eventosuy. */
    public String urlServicio() {
        String host = valor("servicio.host", "localhost");
        String puerto = valor("servicio.puerto", "9128");
        String ruta = valor("servicio.ruta", "/eventosuy");
        if (!ruta.startsWith("/")) {
            ruta = "/" + ruta;
        }
        return "http://" + host + ":" + puerto + ruta;
    }

    /** Si el servicio solo acepta conexiones de esta misma computadora. */
    public boolean soloEstaMaquina() {
        String host = valor("servicio.host", "localhost");
        return host.equals("localhost") || host.equals("127.0.0.1");
    }

    // ===== La base de datos =====

    /** La direccion JDBC de la base, por ejemplo jdbc:postgresql://localhost:5432/eventosuy. */
    public String urlBaseDeDatos() {
        return obligatorio("bd.url");
    }

    public String usuarioBaseDeDatos() {
        return obligatorio("bd.usuario");
    }

    /**
     * Lo que JPA necesita para conectarse, con los nombres de propiedad que usa
     * persistence.xml. Se le pasa a Persistence.createEntityManagerFactory y
     * pisa (en realidad completa) lo que dice persistence.xml.
     *
     * Es publico solo porque una prueba arma una segunda fabrica, para simular
     * "otro programa" escribiendo en la misma base. El resto del codigo no lo usa.
     */
    public Map<String, String> conexionJpa() {
        Map<String, String> conexion = new HashMap<>();
        conexion.put("jakarta.persistence.jdbc.url", urlBaseDeDatos());
        conexion.put("jakarta.persistence.jdbc.user", usuarioBaseDeDatos());
        // La contrasena puede estar vacia (una base sin contrasena), pero la clave tiene que estar.
        conexion.put("jakarta.persistence.jdbc.password", valores.getProperty("bd.password", "").trim());
        return conexion;
    }

    // ===== Utilidades =====

    private String valor(String clave, String porDefecto) {
        String valor = valores.getProperty(clave);
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }

    /** Una clave sin valor por defecto: si falta, el mensaje dice cual y en que archivo. */
    private String obligatorio(String clave) {
        String valor = valores.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la clave " + clave + " en " + ARCHIVO);
        }
        return valor.trim();
    }

    private static String leerPlantilla() throws IOException {
        try (InputStream entrada = Configuracion.class.getResourceAsStream(PLANTILLA)) {
            if (entrada == null) {
                throw new IOException("Falta la plantilla " + PLANTILLA + " dentro del programa");
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Agrega al final del archivo las claves de la plantilla que el archivo no
     * tiene, con el valor de la plantilla. Lo que ya estaba no se toca.
     */
    private static void completarClavesQueFalten(String plantilla) throws IOException {
        Properties actuales = new Properties();
        try (Reader lector = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            actuales.load(lector);
        }
        List<String> faltantes = new ArrayList<>();
        for (String linea : plantilla.split("\\R")) {
            String limpia = linea.trim();
            if (limpia.isEmpty() || limpia.startsWith("#") || !limpia.contains("=")) {
                continue;
            }
            Properties una = new Properties();
            una.load(new StringReader(limpia));
            for (String clave : una.stringPropertyNames()) {
                if (!actuales.containsKey(clave)) {
                    faltantes.add(limpia);
                }
            }
        }
        if (faltantes.isEmpty()) {
            return;
        }
        StringBuilder agregado = new StringBuilder(System.lineSeparator())
                .append("# Agregadas automaticamente el ").append(LocalDate.now())
                .append(" desde la plantilla (faltaban en este archivo):")
                .append(System.lineSeparator());
        for (String linea : faltantes) {
            agregado.append(linea).append(System.lineSeparator());
        }
        Files.writeString(ARCHIVO, agregado.toString(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }
}
