package persistencia;

/**
 * Comprueba que el mapeo JPA de TODO el dominio sea valido.
 *
 * Con solo abrir una unidad de trabajo, Hibernate: lee el persistence.xml, valida
 * las 10 entidades y sus relaciones, y crea/actualiza las tablas
 * (hbm2ddl.auto = update). Si hay un error de mapeo, revienta aca.
 *
 * No inserta ni lee datos: es la prueba del PASO 1.
 *
 *   .\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.PruebaMapeo"
 */
public class PruebaMapeo {

    public static void main(String[] args) {
        try {
            // Abrir una unidad de trabajo crea la fabrica de EntityManagers, y
            // eso es lo que valida el mapeo.
            Persistencia.enTransaccion(em ->
                    System.out.println("\n>>> MAPEO VALIDO: Hibernate acepto las 10 entidades.\n"));
        } finally {
            Persistencia.cerrar();
        }
    }
}
