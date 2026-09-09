package logica;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Datos de prueba de la plataforma. NO es parte de ningun caso de uso.
 *
 * Antes esto vivia dentro de Sistema y corria en su constructor, asi que los
 * datos se recreaban en memoria en cada arranque. Ahora es un programa aparte
 * que se ejecuta a mano (ver persistencia.CargarDatos), por dos motivos:
 *
 *  1) Con JPA los datos van a quedar guardados en PostgreSQL. Volver a
 *     crearlos en cada arranque daria nombres repetidos y la aplicacion no
 *     abriria.
 *  2) Mientras se migran los Manejadores de Map a EntityManager de a uno,
 *     cargarlos automaticamente dejaria un estado mitad en memoria y mitad en
 *     la base.
 *
 * Vive en el paquete logica porque usa metodos de paquete de las entidades
 * (agregarEdicion, agregarTipoRegistro, agregarPatrocinio), que a proposito no
 * son publicos: no forman parte del contrato de la GUI.
 */
public final class DatosDePrueba {

    private DatosDePrueba() {
    }

    /**
     * Carga los datos de prueba. Es IDEMPOTENTE: si ya estan cargados no hace
     * nada, asi se puede correr las veces que sea sin romper nada.
     *
     * @return true si los cargo, false si ya estaban.
     */
    public static boolean cargar() {
        ManejadorUsuario manejadorUsuario = ManejadorUsuario.getInstancia();
        ManejadorCategoria manejadorCategoria = ManejadorCategoria.getInstancia();
        ManejadorInstitucion manejadorInstitucion = ManejadorInstitucion.getInstancia();
        ManejadorEvento manejadorEvento = ManejadorEvento.getInstancia();

        // Guarda de idempotencia: alcanza con mirar si esta el primer usuario.
        if (manejadorUsuario.buscar("pfernandez") != null) {
            return false;
        }

        // ORDEN DE CARGA: primero lo que no depende de nadie, y cada evento
        // se guarda RECIEN cuando ya tiene colgadas todas sus ediciones,
        // tipos de registro, patrocinios y registros.
        //
        // Por que importa: JPA sincroniza con la base al hacer commit. Si se
        // guarda el evento y despues se le cuelga una edicion, esa edicion
        // queda fuera de toda transaccion y NO se guarda. Guardando el evento
        // al final, la edicion (y todo lo que cuelga de ella) viaja sola por
        // el cascade = PERSIST.

        // ===== 1. Lo que no depende de nada =====
        Categoria catIngenieria = new Categoria("Ingeniería");
        manejadorCategoria.agregar(catIngenieria);
        Categoria catCharlas = new Categoria("Charlas");
        manejadorCategoria.agregar(catCharlas);
        Categoria catTalleres = new Categoria("Talleres");
        manejadorCategoria.agregar(catTalleres);
        Categoria catAplicaciones = new Categoria("Aplicaciones");
        manejadorCategoria.agregar(catAplicaciones);

        Institucion utec = new Institucion("UTEC", "Universidad Tecnologica",
                "https://utec.edu.uy");
        manejadorInstitucion.agregar(utec);

        // ===== 2. Usuarios, todavia SIN ediciones ni registros =====
        Asistente paloma = new Asistente("pfernandez", "Paloma", "paloma@example.com",
                "Fernandez", LocalDate.of(2000, 5, 14));
        manejadorUsuario.agregar(paloma);

        Organizador organizadorUtec = new Organizador("utec", "UTEC Eventos", "eventos@utec.edu.uy",
                "Organizador institucional de UTEC", "https://utec.edu.uy");
        manejadorUsuario.agregar(organizadorUtec);

        // ===== 3. JIAP: se arma entero y se guarda al final =====
        Evento jiap = new Evento(
                "JIAP",                                   // nombre
                "Jornadas de Ingeniería y Aplicaciones",  // descripcion
                LocalDate.of(2025, 1, 10),                // fechaAlta
                "JIAP",                                   // sigla
                Arrays.asList(catIngenieria, catAplicaciones)
        );

        EdicionEvento jiap2026 = new EdicionEvento("JIAP 2026", "JIAP26",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 1, 15), "Montevideo", "Uruguay", organizadorUtec);
        jiap.agregarEdicion(jiap2026);
        organizadorUtec.agregarEdicion(jiap2026);

        EdicionEvento jiap2025 = new EdicionEvento("JIAP 2025", "JIAP25",
                LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 3),
                LocalDate.of(2025, 1, 15), "Montevideo", "Uruguay", organizadorUtec);
        jiap.agregarEdicion(jiap2025);
        organizadorUtec.agregarEdicion(jiap2025);

        // Lo que cuelga de JIAP 2026, antes de guardar el evento.
        TipoRegistro entradaGeneral = new TipoRegistro("General", "Entrada general",
                50.0, 200);
        jiap2026.agregarTipoRegistro(entradaGeneral);

        Patrocinio patrocinioUtec = new Patrocinio(LocalDate.of(2026, 2, 1), 5000.0,
                10, 1001, NivelPatrocinio.ORO, utec, entradaGeneral);
        jiap2026.agregarPatrocinio(patrocinioUtec);

        jiap2026.altaRegistro(paloma, entradaGeneral, LocalDate.of(2026, 9, 1));

        manejadorEvento.agregar(jiap);   // <- una sola transaccion baja TODO el arbol

        // ===== 4. Semana de la Ingenieria, igual =====
        Evento semanaIngenieria = new Evento("Semana de la Ingeniería",
                "Charlas y talleres de ingeniería",
                LocalDate.of(2025, 3, 1),
                "SI",
                Arrays.asList(catIngenieria, catCharlas, catTalleres)
        );

        EdicionEvento si2026 = new EdicionEvento("SI 2026", "SI26",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14),
                LocalDate.of(2026, 6, 1), "Montevideo", "Uruguay", organizadorUtec);
        semanaIngenieria.agregarEdicion(si2026);
        organizadorUtec.agregarEdicion(si2026);

        manejadorEvento.agregar(semanaIngenieria);

        return true;
    }
}
