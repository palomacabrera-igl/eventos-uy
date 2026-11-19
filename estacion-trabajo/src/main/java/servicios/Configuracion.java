package servicios;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * La configuracion del Servidor Central, leida de ~/.eventosUy/central.properties.
 *
 * La letra pide que ninguna direccion, puerto ni credencial quede escrita en el
 * codigo: cada computadora tiene la suya y se cambia editando el archivo, sin
 * recompilar. Si el archivo no existe, se crea copiando la plantilla que viaja
 * dentro del JAR (src/main/resources/central.properties), asi la primera vez
 * arranca solo y el archivo queda a mano para editarlo.
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

    /** Lee el archivo, creandolo desde la plantilla si todavia no existe. */
    public static Configuracion cargar() {
        try {
            if (Files.notExists(ARCHIVO)) {
                crearDesdePlantilla();
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

    private static void crearDesdePlantilla() throws IOException {
        Files.createDirectories(CARPETA);
        try (InputStream plantilla = Configuracion.class.getResourceAsStream(PLANTILLA)) {
            if (plantilla == null) {
                throw new IOException("Falta la plantilla " + PLANTILLA + " dentro del programa");
            }
            Files.copy(plantilla, ARCHIVO);
        }
    }

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

    private String valor(String clave, String porDefecto) {
        String valor = valores.getProperty(clave);
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }
}
