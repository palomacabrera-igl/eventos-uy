package logica;

/**
 * Singleton. Es el unico punto donde se instancia Sistema; la
 * capa de presentacion obtiene el controlador exclusivamente a traves de
 * IControladorSistema.
 */
public class Fabrica {

    private static Fabrica instancia;
    private final IControladorSistema controladorSistema;

    private Fabrica() {
        this.controladorSistema = new Sistema();
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
}
