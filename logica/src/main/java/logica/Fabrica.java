package logica;

/**
 * Singleton: el unico punto donde se instancia Sistema. La presentacion obtiene el
 * controlador solo a traves de IControladorSistema, y siempre por aca: lo que entrega es
 * el Sistema envuelto en ControladorConUnidadDeTrabajo, y un "new Sistema()" no tiene
 * unidad de trabajo y sus operaciones fallan.
 */
public class Fabrica {

    private static Fabrica instancia;
    private final IControladorSistema controladorSistema;

    private Fabrica() {
        this.controladorSistema = ControladorConUnidadDeTrabajo.envolver(new Sistema());
    }

    public static Fabrica getInstancia() {
        if (instancia == null) {
            instancia = new Fabrica();
        }
        return instancia;
    }

    public IControladorSistema getControladorSistema() {
        return controladorSistema;
    }

    /**
     * Apaga el Servidor Central y suelta la conexion con la base.
     *
     * Lo llama quien hospeda la logica cuando su aplicacion termina: Tomcat al
     * replegar el sitio, o la Estacion de Trabajo al cerrarse. Esta aca, en la
     * Fabrica, para que la presentacion no tenga que conocer a la capa de
     * persistencia: la Fabrica es la puerta de entrada del componente, y
     * tambien la de salida.
     */
    public static void cerrar() {
        persistencia.Persistencia.cerrar();
        instancia = null;
    }
}
