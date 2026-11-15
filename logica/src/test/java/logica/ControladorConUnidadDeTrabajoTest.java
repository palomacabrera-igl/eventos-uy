package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.RollbackException;

/**
 * El intermediario que abre y cierra la unidad de trabajo, sin base de datos.
 *
 * Lo que necesita PostgreSQL (que cada operacion tenga su EntityManager, que se
 * haga rollback, que dos registros simultaneos no se pasen del cupo) esta en
 * UnidadDeTrabajoIntegracionTest. Aca va lo que se puede comprobar solo:
 * cuando un fallo es "otra operacion se me adelanto" y como se le avisa a la
 * persona.
 */
@DisplayName("Controlador con unidad de trabajo: traduccion de conflictos")
class ControladorConUnidadDeTrabajoTest {

    /** Hibernate envuelve el error de PostgreSQL en varias capas: se busca en toda la cadena. */
    private static Throwable conCausa(String sqlState) {
        SQLException deLaBase = new SQLException("rechazado por la base", sqlState);
        return new RollbackException("Error al hacer commit",
                new PersistenceException("no se pudo ejecutar",
                        new RuntimeException("capa de Hibernate", deLaBase)));
    }

    private static Method metodo(String nombre, Class<?>... parametros) throws NoSuchMethodException {
        return IControladorSistema.class.getMethod(nombre, parametros);
    }

    @Test
    @DisplayName("Clave repetida, conflicto de serializacion y deadlock son choques con otra operacion")
    void reconoceLosChoques() {
        assertTrue(ControladorConUnidadDeTrabajo.esConflictoConOtraOperacion(conCausa("23505")));
        assertTrue(ControladorConUnidadDeTrabajo.esConflictoConOtraOperacion(conCausa("40001")));
        assertTrue(ControladorConUnidadDeTrabajo.esConflictoConOtraOperacion(conCausa("40P01")));
    }

    @Test
    @DisplayName("Otros errores de la base NO se disfrazan de conflicto")
    void noSeDisfrazaCualquierError() {
        // 22001: texto demasiado largo. Eso es un error de programa, no un choque.
        assertFalse(ControladorConUnidadDeTrabajo.esConflictoConOtraOperacion(conCausa("22001")));
        assertFalse(ControladorConUnidadDeTrabajo.esConflictoConOtraOperacion(conCausa(null)));
        assertFalse(ControladorConUnidadDeTrabajo.esConflictoConOtraOperacion(
                new IllegalStateException("cualquier cosa")));
        assertFalse(ControladorConUnidadDeTrabajo.esConflictoConOtraOperacion(null));
    }

    @Test
    @DisplayName("Un choque se convierte en un mensaje que la pantalla puede mostrar")
    void unChoqueSeTraduce() throws NoSuchMethodException {
        Method altaCategoria = metodo("altaCategoria", String.class, String.class);

        Throwable traducido = ControladorConUnidadDeTrabajo.traducir(conCausa("23505"), altaCategoria);

        ReglaNegocioException regla = assertInstanceOf(ReglaNegocioException.class, traducido);
        assertTrue(regla.getMessage().contains("No se guardó nada"));
    }

    @Test
    @DisplayName("Si la operacion no declara ReglaNegocioException, el fallo sale tal cual")
    void sinReglaDeNegocioNoSeTraduce() throws NoSuchMethodException {
        // listarUsuarios() no declara ReglaNegocioException: la pantalla no
        // sabria mostrarla, y Java la envolveria en UndeclaredThrowableException.
        Method listarUsuarios = metodo("listarUsuarios");
        Throwable original = conCausa("23505");

        assertSame(original, ControladorConUnidadDeTrabajo.traducir(original, listarUsuarios));
    }

    @Test
    @DisplayName("Un fallo que no es un choque sale tal cual")
    void otroFalloSaleTalCual() throws NoSuchMethodException {
        Throwable original = new IllegalStateException("error de programa");

        assertSame(original, ControladorConUnidadDeTrabajo.traducir(
                original, metodo("altaCategoria", String.class, String.class)));
    }

    @Test
    @DisplayName("El controlador que entrega la Fabrica es el intermediario, no el Sistema pelado")
    void laFabricaEntregaElIntermediario() {
        IControladorSistema controlador = Fabrica.getInstancia().getControladorSistema();

        assertTrue(Proxy.isProxyClass(controlador.getClass()));
        assertFalse(controlador instanceof Sistema);
    }

    @Test
    @DisplayName("equals, hashCode y toString del intermediario hablan de si mismo y no abren transaccion")
    void metodosDeObject() {
        IControladorSistema controlador = Fabrica.getInstancia().getControladorSistema();

        assertEquals(controlador, controlador);
        assertEquals(controlador.hashCode(), controlador.hashCode());
        assertNotEquals(controlador, new Object());
        assertTrue(controlador.toString().contains("unidad de trabajo"));
    }
}
