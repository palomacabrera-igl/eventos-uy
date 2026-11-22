package logica;

import persistencia.Persistencia;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Set;

/**
 * El intermediario que le pone una unidad de trabajo a cada operacion del
 * controlador.
 *
 * Toda llamada a IControladorSistema tiene que abrir un EntityManager y una
 * transaccion, correr la operacion, hacer commit si sale bien o rollback si lanza
 * algo, y cerrar el EntityManager. Repetirlo en cada metodo de Sistema seria
 * escribir el mismo try / catch decenas de veces; aca se escribe una vez.
 *
 * Es un proxy dinamico de Java: implementa IControladorSistema pero todas las
 * llamadas pasan por un unico metodo, invoke, que abre la unidad, le pasa la
 * llamada al Sistema real y la cierra. Como implementa la interfaz, las pantallas
 * y los servlets no se enteran, y un metodo nuevo de la interfaz queda cubierto
 * sin tocar nada. Lo crea la Fabrica.
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
     * Si la operacion fallo porque otra, hecha al mismo tiempo, se le adelanto (lo
     * impiden las restricciones unicas de la base), lo convierte en un mensaje que
     * se puede mostrar en vez de un error de Hibernate. Solo se traduce si la
     * operacion declara ReglaNegocioException; si no, se devuelve tal cual.
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
