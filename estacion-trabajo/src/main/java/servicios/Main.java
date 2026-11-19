package servicios;

import jakarta.xml.ws.Endpoint;
import logica.Fabrica;
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
 * Arranque de servidor.jar: el Servidor Central y la Estacion de Trabajo.
 *
 * <ol>
 *   <li>Lee la direccion del servicio de ~/.eventosUy/central.properties.</li>
 *   <li>Publica el Publicador con Endpoint.publish. Eso levanta un servidor HTTP
 *       liviano que trae el propio JDK (no se usa Tomcat de este lado), arma el
 *       WSDL leyendo las anotaciones y queda atendiendo pedidos en segundo plano.</li>
 *   <li>Recien despues abre la ventana de la Estacion de Trabajo, como pide la
 *       letra: cuando aparece la ventana, el servicio ya esta disponible.</li>
 * </ol>
 *
 * La Estacion sigue llamando al controlador directamente, dentro de este mismo
 * programa; el servicio web es para el sitio, que esta en otra computadora.
 *
 * Con el argumento --sin-estacion publica el servicio y no abre la ventana: sirve
 * para probar el sitio sin la Estacion, o en una maquina sin pantalla.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Configuracion configuracion = Configuracion.cargar();
        String url = configuracion.urlServicio();

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
