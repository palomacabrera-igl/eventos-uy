package logica;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import persistencia.Persistencia;

/**
 * Base de las pruebas de integracion: las que ejercitan Sistema y por lo
 * tanto necesitan PostgreSQL andando, con los datos de prueba cargados.
 *
 * Las reglas que viven en las entidades se prueban sin base (ver
 * ConsultaUsuarioTest, AltaPatrocinioTest y las demas). Las del controlador
 * no se pueden: Sistema busca, valida y guarda contra la base, asi que para
 * probarlo hay que tenerla.
 *
 * <b>Si la base no esta, las pruebas se saltean en vez de fallar.</b> Asi,
 * quien clone el proyecto y corra las pruebas sin levantar PostgreSQL ve el
 * build en verde con las de integracion marcadas como salteadas, en vez de
 * una lista de errores que hacen parecer que el proyecto esta roto.
 *
 * Para correr solo las unitarias:
 *   ./mvnw -pl logica test -Dgroups='!integracion'
 */
@Tag("integracion")
abstract class PruebaConBase {

    @BeforeAll
    static void exigirLaBase() {
        Assumptions.assumeTrue(hayBase(),
                "PostgreSQL no responde: se saltean las pruebas de integracion");
    }

    private static boolean hayBase() {
        try {
            return Persistencia.getEntityManager().isOpen();
        } catch (RuntimeException sinBase) {
            return false;
        }
    }

    /** Borra filas de prueba con SQL directo, sin que la cascada las reviva. */
    static void borrar(String tabla, String condicion) {
        Persistencia.enTransaccion(em ->
                em.createNativeQuery("DELETE FROM " + tabla + " WHERE " + condicion)
                  .executeUpdate());

        // Un DELETE en SQL pasa por debajo de JPA: el EntityManager sigue con
        // las filas viejas en memoria y la prueba siguiente las ve como si
        // existieran. Hay que vaciarle la memoria para que las relea.
        Persistencia.refrescar();
    }
}
