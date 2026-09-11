package persistencia;

import logica.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import logica.ReglaNegocioException;

/**
 * Prueba de ManejadorUsuario y ManejadorEvento contra PostgreSQL.
 *
 *   .\mvnw.cmd exec:java "-Dexec.mainClass=persistencia.PruebaUsuarioEvento"
 *
 * Es idempotente: se puede correr las veces que sea.
 */
public class PruebaUsuarioEvento {

    public static void main(String[] args) {
        ManejadorUsuario mu = ManejadorUsuario.getInstancia();
        ManejadorEvento me = ManejadorEvento.getInstancia();

        try {
            System.out.println("\n=== 1. Alta de usuarios (asistente y organizador) ===");
            if (mu.buscar("pfernandez") == null) {
                mu.agregar(new Asistente("pfernandez", "Paloma", "paloma@example.com",
                        "Fernandez", LocalDate.of(2000, 5, 14)));
                System.out.println("  guardado: pfernandez (asistente)");
            } else {
                System.out.println("  ya existia: pfernandez");
            }
            if (mu.buscar("utec") == null) {
                mu.agregar(new Organizador("utec", "UTEC Eventos", "eventos@utec.edu.uy",
                        "Organizador institucional de UTEC", "https://utec.edu.uy"));
                System.out.println("  guardado: utec (organizador)");
            } else {
                System.out.println("  ya existia: utec");
            }

            System.out.println("\n=== 2. Consulta polimorfica: una query, dos subtipos ===");
            for (Usuario u : mu.listar()) {
                System.out.printf("  id=%d  %-12s -> %s%n",
                        u.getId(), u.getNickname(), u.getClass().getSimpleName());
            }

            System.out.println("\n=== 3. buscarPorCorreo ===");
            Usuario porCorreo = mu.buscarPorCorreo("eventos@utec.edu.uy");
            System.out.println("  eventos@utec.edu.uy -> " +
                    (porCorreo == null ? "*** NULL ***" : porCorreo.getNickname()));

            System.out.println("\n=== 4. Modificar y que el cambio SOBREVIVA ===");
            Usuario paloma = mu.buscar("pfernandez");
            String nuevoNombre = "Paloma " + (paloma.getNombre().length() % 2 == 0 ? "A." : "B.");
            paloma.modificarDatos(new DTAsistente("pfernandez", nuevoNombre,
                    "paloma@example.com", "Fernandez", DTFecha.desde(LocalDate.of(2000, 5, 14))));
            mu.actualizar(paloma);
            System.out.println("  nombre cambiado a: " + nuevoNombre);
            System.out.println("  releido de la base: " + mu.buscar("pfernandez").getNombre());

            System.out.println("\n=== 5. Alta de evento con categorias ===");
            ManejadorCategoria mc = ManejadorCategoria.getInstancia();
            Categoria cat = mc.buscar("Ingenieria");
            if (cat == null) {
                cat = new Categoria("Ingenieria");
                mc.agregar(cat);
            }
            if (me.buscar("JIAP") == null) {
                me.agregar(new Evento("JIAP", "Jornadas de Ingenieria", LocalDate.of(2025, 1, 10),
                        "JIAP", Arrays.asList(cat)));
                System.out.println("  guardado: JIAP");
            } else {
                System.out.println("  ya existia: JIAP");
            }
            for (Evento e : me.listar()) {
                System.out.printf("  id=%d  %-10s categorias=%d%n",
                        e.getId(), e.getNombre(), e.obtenerDT().getCategorias().size());
            }

            System.out.println("\n=== 6. Colgarle una edicion a un evento YA guardado ===");
            Evento jiap = me.buscar("JIAP");
            Organizador org = (Organizador) mu.buscar("utec");
            if (me.buscarEdicion("JIAP 2026") == null) {
                DTEdicionEvento dt = new DTEdicionEvento("JIAP 2026", "JIAP26", "Montevideo", "Uruguay",
                        DTFecha.desde(LocalDate.of(2026, 10, 1)),
                        DTFecha.desde(LocalDate.of(2026, 10, 3)),
                        DTFecha.desde(LocalDate.of(2026, 1, 15)));
                jiap.altaEdicion(dt, org);
                me.actualizar(jiap);
                System.out.println("  creada la edicion JIAP 2026");
            } else {
                System.out.println("  ya existia: JIAP 2026");
            }

            System.out.println("\n=== 7. buscarEdicion: una sola consulta, sin recorrer eventos ===");
            EdicionEvento ed = me.buscarEdicion("JIAP 2026");
            System.out.println("  JIAP 2026 -> evento: " +
                    (ed == null ? "*** NULL ***" : ed.getEvento().getNombre())
                    + " | organizador: " + (ed == null ? "-" : ed.getOrganizador().getNickname()));
            System.out.println("  inexistente -> " + me.buscarEdicion("NO EXISTE"));

            System.out.println("\n=== 8. La base rechaza un nickname repetido ===");
            try {
                mu.agregar(new Asistente("pfernandez", "Otra", "otra@example.com",
                        "Persona", LocalDate.of(1999, 1, 1)));
                System.out.println("  INESPERADO: acepto un nickname repetido.");
            } catch (Exception e) {
                System.out.println("  rollback OK: " + causaRaiz(e).getClass().getSimpleName());
            }

            System.out.println("\n=== 9. LOS CASOS DE USO, por el mismo camino que la pantalla ===");
            System.out.println("    (todo pasa por IControladorSistema, igual que los paneles Swing)");
            IControladorSistema ctrl = Fabrica.getInstancia().getControladorSistema();

            System.out.println("\n  -- Modificar Datos de Usuario --");
            DTUsuario antes = ctrl.seleccionarUsuario("pfernandez");
            String nombreCU = "Paloma CU" + (antes.getNombre().endsWith("1") ? "2" : "1");
            ctrl.modificarDatosUsuario(new DTAsistente("pfernandez", nombreCU,
                    "paloma@example.com", "Fernandez", DTFecha.desde(LocalDate.of(2000, 5, 14))));
            System.out.println("     antes: " + antes.getNombre());
            System.out.println("     ahora: " + ctrl.seleccionarUsuario("pfernandez").getNombre());

            System.out.println("\n  -- Alta de Edicion de Evento --");
            ctrl.seleccionarEvento("JIAP");
            ctrl.seleccionarOrganizador("utec");
            String nombreEd = "JIAP " + (2030 + ctrl.listarEdicionesDeEvento("JIAP").size());
            DTEdicionEvento nueva = new DTEdicionEvento(nombreEd, "JE", "Salto", "Uruguay",
                    DTFecha.desde(LocalDate.of(2030, 10, 1)),
                    DTFecha.desde(LocalDate.of(2030, 10, 3)),
                    DTFecha.desde(LocalDate.of(2030, 1, 15)));
            ctrl.ingresarDatosEdicion(nueva);
            System.out.println("     alta de '" + nombreEd + "': OK");

            try {
                ctrl.ingresarDatosEdicion(nueva);
                System.out.println("     la misma otra vez: OK (MAL!)");
            } catch (ReglaNegocioException e) {
                System.out.println("     la misma otra vez: rechazada, correcto");
            }

            System.out.println("\n  -- Ediciones de JIAP segun el controlador --");
            for (DTEdicionEvento e : ctrl.listarEdicionesDeEvento("JIAP")) {
                System.out.println("     " + e.getNombre() + "  (" + e.getCiudad() + ")");
            }
        } catch (ReglaNegocioException e) {
            System.out.println("\n>>> RECHAZADO POR UNA REGLA: " + e.getMessage());
        } finally {
            Persistencia.cerrar();
            System.out.println("\nFin de la prueba.");
        }
    }

    private static Throwable causaRaiz(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) { t = t.getCause(); }
        return t;
    }
}
