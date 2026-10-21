package persistencia;

import jakarta.persistence.EntityManager;

/**
 * Comprueba que el mapeo JPA de TODO el dominio sea valido.
 *
 * Con solo pedir un EntityManager, Hibernate: lee el persistence.xml, valida
 * las 10 entidades y sus relaciones, y crea/actualiza las tablas
 * (hbm2ddl.auto = update). Si hay un error de mapeo, revienta aca.
 *
 * No inserta ni lee datos: es la prueba del PASO 1.
 *
 *   .\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.PruebaMapeo"
 */
public class PruebaMapeo {

    public static void main(String[] args) {
        EntityManager em = null;
        try {
            em = Persistencia.getEntityManager();
            System.out.println("\n>>> MAPEO VALIDO: Hibernate acepto las 10 entidades.\n");
        } finally {
            if (em != null) em.close();
            Persistencia.cerrar();
        }
    }
}
