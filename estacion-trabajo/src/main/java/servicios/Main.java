package servicios;

import jakarta.xml.ws.Endpoint;
import logica.Fabrica;
import persistencia.Configuracion;
import presentacion.VentanaPrincipal;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Arranque de servidor.jar: el Servidor Central y la Estacion de Trabajo. Lee la
 * direccion de ~/.eventosUy/central.properties, deja lista la conexion con la base
 * (prepararBase), publica el Publicador con Endpoint.publish (servidor HTTP del
 * propio JDK, sin Tomcat) y recien despues abre la ventana de la Estacion, asi
 * cuando aparece el servicio ya esta disponible. La Estacion llama al controlador
 * directamente, en este mismo programa. Con --sin-estacion no abre la ventana.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Configuracion configuracion = Configuracion.cargar();
        String url = configuracion.urlServicio();

        prepararBase(configuracion);

        Endpoint endpoint;
        try {
            endpoint = Endpoint.publish(url, new Publicador());
        } catch (RuntimeException e) {
            // Lo mas comun: el puerto ya esta ocupado (otro Servidor Central abierto)
            // o el host no es una IP de esta maquina.
            System.err.println("No se pudo publicar el servicio en " + url + ": " + e.getMessage());
            System.err.println("Revisar " + Configuracion.ARCHIVO
                    + " y que no haya otro Servidor Central abierto.");
            System.exit(1);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(endpoint::stop));

        System.out.println("Configuracion:        " + Configuracion.ARCHIVO);
        System.out.println("Servicio publicado en " + url);
        System.out.println("Contrato (WSDL):      " + url + "?wsdl");
        System.out.println("IP de esta maquina en la red: " + String.join(", ", direccionesLocales()));
        if (configuracion.soloEstaMaquina()) {
            System.out.println("Ojo: con servicio.host=localhost solo lo ven los programas de esta");
            System.out.println("computadora. Para el sitio en otra maquina, poner una de las IP de arriba.");
        }

        if (Arrays.asList(args).contains("--sin-estacion")) {
            // Sin ventana, el que cierra la conexion con la base al terminar es este hook.
            Runtime.getRuntime().addShutdownHook(new Thread(Fabrica::cerrar));
            System.out.println("Sin Estacion de Trabajo (--sin-estacion). Para detener: Ctrl+C.");
            return;
        }
        VentanaPrincipal.main(args);
    }

    /**
     * Deja lista la conexion con la base antes de publicar el servicio. La primera
     * operacion arranca Hibernate y tarda; si fuera un pedido del sitio, este podria
     * cortar por tiempo ("Read timed out"). Usa una operacion comun del controlador
     * y, si PostgreSQL no esta andando, avisa y no publica nada.
     */
    private static void prepararBase(Configuracion configuracion) {
        long inicio = System.currentTimeMillis();
        try {
            // La contrasena no se muestra.
            System.out.println("Conectando con la base de datos " + configuracion.urlBaseDeDatos()
                    + " (usuario " + configuracion.usuarioBaseDeDatos() + ")...");
            Fabrica.getInstancia().getControladorSistema().listarCategorias();
        } catch (RuntimeException e) {
            System.err.println("No se pudo conectar con la base de datos: " + causaRaiz(e));
            System.err.println("Revisar que PostgreSQL este andando y bd.url, bd.usuario y bd.password en "
                    + Configuracion.ARCHIVO);
            System.exit(1);
        }
        System.out.println("Base de datos lista (" + (System.currentTimeMillis() - inicio) + " ms).");
    }

    /**
     * El mensaje de la causa original. Hibernate envuelve el error de conexion
     * en varias capas; el de abajo es el que dice que paso (por ejemplo, "la
     * autentificacion password fallo para el usuario").
     */
    private static String causaRaiz(Throwable error) {
        Throwable causa = error;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa.getMessage();
    }

    /** Las IPv4 de las interfaces de red activas, sin contar la 127.0.0.1. */
    private static List<String> direccionesLocales() {
        List<String> direcciones = new ArrayList<>();
        try {
            for (NetworkInterface interfaz : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!interfaz.isUp() || interfaz.isLoopback()) {
                    continue;
                }
                for (InetAddress direccion : Collections.list(interfaz.getInetAddresses())) {
                    if (direccion instanceof Inet4Address) {
                        direcciones.add(direccion.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            direcciones.add("(no se pudieron obtener)");
        }
        return direcciones.isEmpty() ? List.of("(ninguna)") : direcciones;
    }
}
