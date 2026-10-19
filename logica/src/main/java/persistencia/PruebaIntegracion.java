package persistencia;

import logica.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Prueba de integracion: los casos de uso que involucran a los cuatro
 * Manejadores. Todo pasa por IControladorSistema, igual que los paneles Swing.
 *
 *   .\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.PruebaIntegracion"
 *
 * Se corre DESPUES de CargarDatos. Es idempotente.
 */
public class PruebaIntegracion {

    public static void main(String[] args) {
        IControladorSistema c = Fabrica.getInstancia().getControladorSistema();
        String sufijo = String.valueOf(System.currentTimeMillis() % 10000);

        try {
            System.out.println("\n=== A. Alta de Categoria CON PADRE (jerarquia de Leandro) ===");
            String hija = "Robotica " + sufijo;
            c.altaCategoria(hija, "Ingeniería");
            System.out.println("  alta de '" + hija + "' colgando de 'Ingeniería': OK");
            try {
                c.altaCategoria(hija, "Ingeniería");
                System.out.println("  repetida: INESPERADO, la acepto");
            } catch (ReglaNegocioException e) {
                System.out.println("  repetida: rechazada, correcto  (" + e.getMessage() + ")");
            }

            System.out.println("\n=== B. Alta de Institucion ===");
            String inst = "Instituto " + sufijo;
            c.altaInstitucion(inst, "Instituto de prueba", "https://ejemplo.uy");
            System.out.println("  alta de '" + inst + "': OK");

            System.out.println("\n=== C. Alta de Evento (necesita categorias persistidas) ===");
            String ev = "Congreso " + sufijo;
            c.ingresarDatosEvento(ev, "Evento de prueba", LocalDate.of(2025, 6, 1),
                    "CG" + sufijo, List.of("Ingeniería", "Charlas"));
            System.out.println("  alta de '" + ev + "' con 2 categorias: OK");

            System.out.println("\n=== D. Alta de Usuario CON institucion ===");
            String nick = "usuario" + sufijo;
            c.ingresarDatosUsuario(
                    new DTUsuario(nick, "Usuario Prueba", nick + "@example.com"),
                    TipoUsuario.ASISTENTE);
            System.out.println("  datos basicos aceptados: OK");

            c.ingresarDatosAsistente("Prueba", DTFecha.desde(LocalDate.of(1998, 3, 20)));
            c.seleccionarInstitucion(inst);
            System.out.println("  asistente creado y asociado a '" + inst + "'");

            System.out.println("\n=== E. Alta de Patrocinio (necesita institucion + tipo de registro) ===");
            c.listarEdicionesDeEvento("JIAP");
            c.listarTiposRegistroDeEdicion("JIAP 2026");
            DTPatrocinio dtp = new DTPatrocinio(Integer.parseInt(sufijo),
                    DTFecha.desde(LocalDate.now()), 5000.0, "PLATA",
                    10, inst, "General");
            try {
                c.altaPatrocinio(dtp);
                System.out.println("  patrocinio creado (codigo " + sufijo + ")");
            } catch (ReglaNegocioException e) {
                System.out.println("  regla de negocio: " + e.getMessage());
            }

            System.out.println("\n=== F. La regla del 20% sigue funcionando ===");
            try {
                c.altaPatrocinio(new DTPatrocinio(Integer.parseInt(sufijo) + 1,
                        DTFecha.desde(LocalDate.now()), 100.0, "BRONCE",
                        50, inst, "General"));
                System.out.println("  INESPERADO: acepto 50 gratis sobre un aporte de 100.");
            } catch (ReglaNegocioException e) {
                System.out.println("  rechazado, correcto");
            }

            System.out.println("\n=== F2. La institucion llega hasta el DT (lo que ve la pantalla) ===");
            for (DTUsuario u : c.listarUsuarios()) {
                if (u instanceof DTAsistente a) {
                    System.out.printf("  %-14s institucion: %s%n", a.getNickname(),
                            a.getInstitucion() == null ? "(ninguna)" : a.getInstitucion());
                }
            }

            System.out.println("\n=== G. Estado leido de la base ===");
            System.out.println("  usuarios      : " + c.listarUsuarios().size());
            System.out.println("  eventos       : " + c.listarEventos().size());
            System.out.println("  categorias    : " + c.listarCategorias().size());
            System.out.println("  raices arbol  : " + c.listarCategoriasArbol().size());
            System.out.println("  instituciones : " + c.listarNombresInstituciones().size());
            System.out.println("  patrocinios JIAP 2026: " + c.listarPatrociniosDeEdicion("JIAP 2026").size());

        } catch (ReglaNegocioException e){
            // Una regla de negocio rechazo una operacion que la prueba daba por exitosa.
            System.out.println("\n>>> RECHAZADO POR UNA REGLA: " + e.getMessage());
        } finally {
            Persistencia.cerrar();
            System.out.println("\nFin de la prueba de integracion.");
        }
    }
}
