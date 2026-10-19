package persistencia;

import logica.DatosDePrueba;

/**
 * Carga los datos de prueba de la plataforma. Se corre A MANO, una sola vez.
 *
 *   IntelliJ  -> boton verde al lado de main()
 *   Terminal  -> .\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.CargarDatos"
 *
 * Es idempotente: si los datos ya estan, avisa y no hace nada.
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
