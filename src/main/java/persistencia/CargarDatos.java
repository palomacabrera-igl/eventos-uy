package persistencia;

import logica.DatosDePrueba;

/**
 * Carga los datos de prueba de la plataforma. Se corre A MANO, una sola vez.
 *
 *   IntelliJ  -> boton verde al lado de main()
 *   Terminal  -> .\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.CargarDatos"
 *
 * Antes esto corria solo, dentro del constructor de Sistema. Se saco de ahi
 * porque con JPA los datos quedan guardados en PostgreSQL: recrearlos en cada
 * arranque daria nombres repetidos y la aplicacion no abriria.
 *
 * Es idempotente: si los datos ya estan, avisa y no hace nada.
 *
 * OJO: mientras los Manejadores sigan usando Map en memoria, esto carga los
 * datos y se pierden al terminar el programa. Recien tiene efecto real cuando
 * los cuatro Manejadores esten migrados a EntityManager.
 */
public class CargarDatos {

    public static void main(String[] args) {
        try {
            if (DatosDePrueba.cargar()) {
                System.out.println("\n>>> Datos de prueba cargados.\n");
            } else {
                System.out.println("\n>>> Los datos de prueba ya estaban cargados. No se hizo nada.\n");
            }
        } finally {
            Persistencia.cerrar();
        }
    }
}
