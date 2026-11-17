package logica;

import persistencia.Persistencia;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Set;

/**
 * El intermediario que le pone una "unidad de trabajo" a cada operacion del
 * controlador.
 *
 * <h2>Que problema resuelve</h2>
 *
 * Cada vez que la Estacion de Trabajo o el sitio llaman a una operacion de
 * IControladorSistema (por ejemplo altaRegistro), tiene que pasar siempre lo
 * mismo, en este orden:
 *
 * <pre>
 *   1. abrir un EntityManager nuevo y empezar una transaccion
 *   2. correr la operacion
 *   3. si salio bien  -> commit    (se guarda todo junto)
 *      si lanzo algo  -> rollback  (no se guarda nada)
 *   4. cerrar el EntityManager, que descarta todo lo que tenia en memoria
 * </pre>
 *
 * Escribir eso a mano en cada uno de los ~60 metodos de Sistema seria repetir
 * el mismo try / catch 60 veces, y alcanza con que se olvide uno para que
 * vuelva el error de siempre: una operacion cambia objetos en memoria, falla
 * a la mitad, y esos cambios quedan vivos aunque nunca lleguen a la base.
 *
 * En cambio se escribe UNA vez, aca.
 *
 * <h2>Como funciona</h2>
 *
 * Es un "proxy dinamico" de Java: un objeto que implementa IControladorSistema
 * pero que, en lugar de tener codigo propio para cada metodo, tiene UN solo
 * metodo (invoke) por el que pasan TODAS las llamadas. Ahi se abre la unidad
 * de trabajo, se le pasa la llamada al Sistema de verdad, y se cierra.
 *
 * <pre>
 *   la pantalla / el servlet
 *         |  controlador.altaRegistro(...)
 *         v
 *   ControladorConUnidadDeTrabajo.invoke(...)   <- abre la unidad
 *         |  metodo.invoke(sistema, ...)
 *         v
 *   Sistema.altaRegistro(...)                   <- solo reglas de negocio
 *         |
 *         v
 *   Manejadores -> Persistencia.getEntityManager()  <- el EntityManager de la unidad
 * </pre>
 *
 * Y como el proxy implementa la interfaz, ni la pantalla ni el servlet se
 * enteran: siguen escribiendo controlador.altaRegistro(...) como siempre. Si
 * manana se agrega un metodo a IControladorSistema, tambien queda cubierto sin
 * escribir nada aca.
 *
 * Lo crea la Fabrica, que es el unico lugar que entrega el controlador.
 */
final class ControladorConUnidadDeTrabajo implements InvocationHandler {

    /** El Sistema de verdad, al que se le pasan las llamadas. */
    private final Sistema destino;

    private ControladorConUnidadDeTrabajo(Sistema destino) {
        this.destino = destino;
    }

    /** Envuelve al Sistema: lo que devuelve es lo que la Fabrica le entrega a las pantallas. */
    static IControladorSistema envolver(Sistema destino) {
        return (IControladorSistema) Proxy.newProxyInstance(
                IControladorSistema.class.getClassLoader(),
                new Class<?>[] {IControladorSistema.class},
                new ControladorConUnidadDeTrabajo(destino));
    }

    @Override
    public Object invoke(Object proxy, Method metodo, Object[] argumentos) throws Throwable {
        // toString(), equals() y hashCode() tambien pasan por el proxy, pero no
        // son operaciones del sistema: no tiene sentido abrirles una transaccion.
        // Se contestan aca, hablando del proxy (el objeto que tiene la pantalla),
        // no del Sistema de adentro: por eso equals compara contra el proxy.
        if (metodo.getDeclaringClass() == Object.class) {
            return switch (metodo.getName()) {
                case "equals" -> proxy == argumentos[0];
                case "hashCode" -> System.identityHashCode(proxy);
                default -> "Controlador con unidad de trabajo sobre " + destino;
            };
        }

        try {
            return Persistencia.enUnidadDeTrabajo(em -> {
                // Lo que una llamada del Laboratorio 1 dejo "seleccionado" quedo
                // desconectado cuando se cerro su unidad. Se vuelve a conectar a
                // la base ANTES de correr la operacion, para que trabaje con
                // datos frescos. Ver Sistema.reanclarSeleccion().
                destino.reanclarSeleccion();
                return metodo.invoke(destino, argumentos);
            });
        } catch (InvocationTargetException envoltorio) {
            // Cuando Sistema lanza algo, Java lo envuelve en un
            // InvocationTargetException. Se desenvuelve para que quien llamo
            // reciba la excepcion de verdad (por ejemplo ReglaNegocioException,
            // que las pantallas muestran con su mensaje). Para ese momento el
            // rollback ya se hizo y el EntityManager ya se cerro.
            throw traducir(envoltorio.getCause(), metodo);
        } catch (RuntimeException alConfirmar) {
            // El commit tambien puede fallar (por ejemplo, la base rechaza una
            // fila repetida): eso no pasa adentro de Sistema sino al final.
            throw traducir(alConfirmar, metodo);
        }
    }

    // ------------------------------------------------------------------
    //  Conflictos entre operaciones simultaneas
    // ------------------------------------------------------------------

    /**
     * Si la operacion fallo porque OTRA, hecha al mismo tiempo, se le adelanto,
     * lo convierte en un mensaje que se puede mostrar en lugar de un error de
     * Hibernate con su pila de lineas.
     *
     * Ahora que no hay un candado global, dos personas pueden registrarse a la
     * vez. Las restricciones unicas de la base (un asistente no puede quedar
     * registrado dos veces en la misma edicion, un nombre no se repite, etc.)
     * son la red de seguridad: la base rechaza la segunda, y aca se le explica
     * a la persona que fue por eso y que no se guardo nada.
     *
     * Solo se traduce si la operacion declara ReglaNegocioException, que es lo
     * unico que las pantallas saben mostrar; si no, se devuelve tal cual.
     */
    static Throwable traducir(Throwable fallo, Method metodo) {
        if (esConflictoConOtraOperacion(fallo) && declaraReglaNegocio(metodo)) {
            return new ReglaNegocioException(
                    "Otra operación hecha al mismo tiempo se adelantó y los datos cambiaron. "
                            + "No se guardó nada: volvé a intentarlo.");
        }
        return fallo;
    }

    /**
     * Los codigos SQLSTATE de PostgreSQL que significan "choque con otra
     * operacion": 23505 clave repetida, 40001 conflicto de serializacion,
     * 40P01 deadlock detectado. Se buscan en toda la cadena de causas, porque
     * Hibernate envuelve el SQLException de PostgreSQL en varias capas.
     */
    private static final Set<String> CONFLICTOS = Set.of("23505", "40001", "40P01");

    static boolean esConflictoConOtraOperacion(Throwable fallo) {
        for (Throwable causa = fallo; causa != null; causa = causa.getCause()) {
            // El estado puede ser null (algunos errores del driver no lo traen) y
            // Set.of(...).contains(null) lanzaria NullPointerException: se mira antes.
            if (causa instanceof SQLException sql
                    && sql.getSQLState() != null && CONFLICTOS.contains(sql.getSQLState())) {
                return true;
            }
            if (causa.getCause() == causa) {
                break;
            }
        }
        return false;
    }

    private static boolean declaraReglaNegocio(Method metodo) {
        for (Class<?> declarada : metodo.getExceptionTypes()) {
            if (declarada.isAssignableFrom(ReglaNegocioException.class)) {
                return true;
            }
        }
        return false;
    }
}
