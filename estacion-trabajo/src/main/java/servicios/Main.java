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
 *   <li>Deja lista la conexion con la base (ver prepararBase): asi el primer
 *       pedido del sitio no tiene que esperar a que arranque Hibernate.</li>
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

        prepararBase();

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
     * Deja lista la conexion con la base ANTES de publicar el servicio.
     *
     * La primera operacion que llega arranca Hibernate (lee el mapeo, revisa las
     * tablas y abre las conexiones), y eso tarda. Si esa primera operacion es un
     * pedido del sitio, el sitio se queda esperando y puede cortar por tiempo
     * ("Read timed out") justo al empezar la demostracion. Haciendolo aca, cuando
     * la consola dice "Servicio publicado", el Central ya contesta rapido.
     *
     * Se usa una operacion comun del controlador (por la Fabrica, con su unidad de
     * trabajo), no un acceso directo a la persistencia. Y si PostgreSQL no esta
     * andando, se avisa con un mensaje claro y no se publica nada.
     */
    private static void prepararBase() {
        System.out.println("Conectando con la base de datos...");
        long inicio = System.currentTimeMillis();
        try {
            Fabrica.getInstancia().getControladorSistema().listarCategorias();
        } catch (RuntimeException e) {
            System.err.println("No se pudo conectar con la base de datos: " + e.getMessage());
            System.err.println("Revisar que PostgreSQL este andando y los datos de conexion.");
            System.exit(1);
        }
        System.out.println("Base de datos lista (" + (System.currentTimeMillis() - inicio) + " ms).");
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
