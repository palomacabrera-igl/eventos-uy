package logica;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatosDePrueba {

    private DatosDePrueba() {
    }

    public static boolean cargar() {
        ManejadorUsuario manejadorUsuario =
                ManejadorUsuario.getInstancia();
        ManejadorCategoria manejadorCategoria =
                ManejadorCategoria.getInstancia();
        ManejadorInstitucion manejadorInstitucion =
                ManejadorInstitucion.getInstancia();
        ManejadorEvento manejadorEvento =
                ManejadorEvento.getInstancia();

        // Si ya fueron cargados, no vuelve a insertar datos.
        if (manejadorUsuario.buscar("vale23") != null) {
            return false;
        }

        // Si falta UNA imagen, lanza error antes de guardar datos.
        Map<String, Imagen> imagenes =
                cargarImagenesAntesDePersistir();

        // =========================
        // Categorías
        // =========================

        Categoria tecnologia = new Categoria("Tecnología");
        Categoria cultura = new Categoria("Cultura");
        Categoria deporte = new Categoria("Deporte");
        Categoria musica = new Categoria("Música");
        Categoria negocios = new Categoria("Negocios");

        manejadorCategoria.agregar(tecnologia);
        manejadorCategoria.agregar(cultura);
        manejadorCategoria.agregar(deporte);
        manejadorCategoria.agregar(musica);
        manejadorCategoria.agregar(negocios);

        // =========================
        // Instituciones
        // =========================

        Institucion utec = new Institucion(
                "UTEC",
                "Universidad Tecnológica del Uruguay.",
                "https://utec.edu.uy"
        );
        utec.asignarImagen(imagenes.get(
                "imagenes/instituciones/avatarInstitucion.png"
        ));

        Institucion udelar = new Institucion(
                "Universidad de la República",
                "Universidad pública del Uruguay.",
                "https://udelar.edu.uy"
        );
        udelar.asignarImagen(imagenes.get(
                "imagenes/instituciones/avatarInstitucion.png"
        ));

        Institucion ancap = new Institucion(
                "ANCAP",
                "Administración Nacional de Combustibles, Alcohol y Portland.",
                "https://ancap.com.uy"
        );
        ancap.asignarImagen(imagenes.get(
                "imagenes/instituciones/avatarInstitucion.png"
        ));

        manejadorInstitucion.agregar(utec);
        manejadorInstitucion.agregar(udelar);
        manejadorInstitucion.agregar(ancap);

        // =========================
        // Asistentes
        // =========================

        Asistente vale23 = new Asistente(
                "vale23",
                "Valentina",
                "valentina@mail.com",
                "Díaz",
                LocalDate.of(2001, 3, 22),
                "Vale2026!"
        );
        vale23.setInstitucion(utec);
        vale23.asignarImagen(imagenes.get(
                "imagenes/usuarios/avatarFemenino.png"
        ));

        Asistente jperez = new Asistente(
                "jperez",
                "Juan",
                "jperez@mail.com",
                "Pérez",
                LocalDate.of(1998, 11, 5),
                "Juan2026!"
        );
        jperez.asignarImagen(imagenes.get(
                "imagenes/usuarios/avatarMasculino.png"
        ));

        Asistente lgomez = new Asistente(
                "lgomez",
                "Lucía",
                "lucia@mail.com",
                "Gómez",
                LocalDate.of(1999, 7, 30),
                "Lucia2026!"
        );
        lgomez.setInstitucion(utec);
        lgomez.asignarImagen(imagenes.get(
                "imagenes/usuarios/avatarFemenino.png"
        ));

        manejadorUsuario.agregar(vale23);
        manejadorUsuario.agregar(jperez);
        manejadorUsuario.agregar(lgomez);

        // =========================
        // Organizadores
        // =========================

        Organizador imm = new Organizador(
                "imm",
                "Intendencia de Montevideo",
                "eventos@imm.gub.uy",
                "Organismo que coordina los eventos culturales y deportivos de la capital.",
                "https://montevideo.gub.uy",
                "Imm2026!"
        );
        imm.asignarImagen(imagenes.get(
                "imagenes/instituciones/avatarInstitucion.png"
        ));

        Organizador antel = new Organizador(
                "antel",
                "ANTEL",
                "eventos@antel.com.uy",
                "Empresa de telecomunicaciones del Estado uruguayo.",
                "",
                "Antel2026!"
        );
        antel.asignarImagen(imagenes.get(
                "imagenes/instituciones/avatarInstitucion.png"
        ));

        manejadorUsuario.agregar(imm);
        manejadorUsuario.agregar(antel);

        // =========================
        // Evento: JIAP
        // =========================

        Evento jiap = new Evento(
                "JIAP",
                "Jornadas de Ingeniería",
                LocalDate.of(2025, 1, 10),
                "JIAP",
                Arrays.asList(tecnologia)
        );
        jiap.asignarImagen(imagenes.get(
                "imagenes/eventos/JIAP/eventoIng.png"
        ));

        EdicionEvento jiap2026 = new EdicionEvento(
                "JIAP 2026",
                "JIAP26",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 1, 15),
                "Montevideo",
                "Uruguay",
                imm
        );
        jiap2026.asignarImagen(imagenes.get(
                "imagenes/eventos/JIAP/ediciones/edicionIng2026.png"
        ));
        try {
            jiap2026.aceptar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(e);
        }

        EdicionEvento jiap2027 = new EdicionEvento(
                "JIAP 2027",
                "JIAP27",
                LocalDate.of(2027, 10, 2),
                LocalDate.of(2027, 10, 4),
                LocalDate.of(2026, 9, 25),
                "Maldonado",
                "Uruguay",
                imm
        );
        jiap2027.asignarImagen(imagenes.get(
                "imagenes/eventos/JIAP/ediciones/edicionIng2027.png"
        ));
        // Queda INGRESADA, que es el estado inicial.

        EdicionEvento jiap2025 = new EdicionEvento(
                "JIAP 2025",
                "JIAP25",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 3),
                LocalDate.of(2025, 1, 15),
                "Montevideo",
                "Uruguay",
                imm
        );
        jiap2025.asignarImagen(imagenes.get(
                "imagenes/eventos/JIAP/ediciones/edicionIng2025.png"
        ));
        try {
            jiap2025.rechazar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(e);
        }

        jiap.agregarEdicion(jiap2026);
        jiap.agregarEdicion(jiap2027);
        jiap.agregarEdicion(jiap2025);

        imm.agregarEdicion(jiap2026);
        imm.agregarEdicion(jiap2027);
        imm.agregarEdicion(jiap2025);

        TipoRegistro jiapGeneral = new TipoRegistro(
                "General",
                "Acceso a todas las charlas.",
                1500.0,
                200
        );

        TipoRegistro jiapEstudiante = new TipoRegistro(
                "Estudiante",
                "Acceso con tarifa estudiantil.",
                800.0,
                80
        );

        TipoRegistro jiapTaller = new TipoRegistro(
                "Taller intensivo",
                "Incluye talleres prácticos con cupo limitado.",
                2200.0,
                30
        );

        jiap2026.agregarTipoRegistro(jiapGeneral);
        jiap2026.agregarTipoRegistro(jiapEstudiante);
        jiap2027.agregarTipoRegistro(jiapTaller);

        Patrocinio patrocinioJiap2026 = new Patrocinio(
                LocalDate.of(2026, 2, 1),
                20000.0,
                1,
                "1001",
                NivelPatrocinio.ORO,
                utec,
                jiapGeneral
        );

        Patrocinio patrocinioJiap2027 = new Patrocinio(
                LocalDate.of(2025, 2, 10),
                25000.0,
                2,
                "1002",
                NivelPatrocinio.PLATA,
                utec,
                jiapTaller
        );

        jiap2026.agregarPatrocinio(patrocinioJiap2026);
        jiap2027.agregarPatrocinio(patrocinioJiap2027);

        jiap2026.altaRegistro(
                vale23,
                jiapGeneral,
                LocalDate.of(2026, 9, 1),
                patrocinioJiap2026
        );

        manejadorEvento.agregar(jiap);

        // =========================
        // Evento: Feria del libro
        // =========================

        Evento feriaLibro = new Evento(
                "Feria del libro",
                "Semana del libro",
                LocalDate.of(2017, 1, 1),
                "FL",
                Arrays.asList(cultura)
        );
        feriaLibro.asignarImagen(imagenes.get(
                "imagenes/eventos/FeriaDelLibro/eventoLibro.png"
        ));

        EdicionEvento libro2026 = new EdicionEvento(
                "Libro1",
                "L1",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 7),
                LocalDate.of(2026, 1, 15),
                "San José de Mayo",
                "Uruguay",
                antel
        );
        libro2026.asignarImagen(imagenes.get(
                "imagenes/eventos/FeriaDelLibro/ediciones/edicionLib2026.png"
        ));
        try {
            libro2026.aceptar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(e);
        }

        feriaLibro.agregarEdicion(libro2026);
        antel.agregarEdicion(libro2026);

        TipoRegistro libroGeneral = new TipoRegistro(
                "General",
                "Acceso a charlas y talleres de la semana.",
                1200.0,
                150
        );
        libro2026.agregarTipoRegistro(libroGeneral);

        Patrocinio patrocinioLibro = new Patrocinio(
                LocalDate.of(2025, 5, 1),
                8000.0,
                1,
                "1003",
                NivelPatrocinio.BRONCE,
                utec,
                libroGeneral
        );
        libro2026.agregarPatrocinio(patrocinioLibro);

        manejadorEvento.agregar(feriaLibro);

        // =========================
        // Evento: Maratón de Montevideo
        // =========================

        Evento maraton = new Evento(
                "Maratón de Montevideo",
                "Competencia deportiva anual en la capital.",
                LocalDate.of(2024, 3, 12),
                "MARATON",
                Arrays.asList(deporte)
        );
        maraton.asignarImagen(imagenes.get(
                "imagenes/eventos/MaratonDeMontevideo/maraton.png"
        ));

        EdicionEvento maraton2026 = new EdicionEvento(
                "Maratón 2026",
                "MVD26",
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 3, 1),
                "Montevideo",
                "Uruguay",
                imm
        );
        maraton2026.asignarImagen(imagenes.get(
                "imagenes/eventos/MaratonDeMontevideo/ediciones/maraton2026.png"
        ));
        try {
            maraton2026.aceptar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(e);
        };

        EdicionEvento maraton2025 = new EdicionEvento(
                "Maratón 2025",
                "MVD25",
                LocalDate.of(2025, 11, 16),
                LocalDate.of(2025, 11, 16),
                LocalDate.of(2025, 2, 20),
                "Montevideo",
                "Uruguay",
                imm
        );
        maraton2025.asignarImagen(imagenes.get(
                "imagenes/eventos/MaratonDeMontevideo/ediciones/maraton2025.png"
        ));
        try {
            maraton2025.aceptar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(e);
        }

        maraton.agregarEdicion(maraton2026);
        maraton.agregarEdicion(maraton2025);
        imm.agregarEdicion(maraton2026);
        imm.agregarEdicion(maraton2025);

        TipoRegistro corredor10K = new TipoRegistro(
                "Corredor 10K",
                "Recorrido de 10 kilómetros por la rambla.",
                1200.0,
                500
        );

        TipoRegistro corredor21K = new TipoRegistro(
                "Corredor 21K",
                "Media maratón.",
                1800.0,
                300
        );

        maraton2026.agregarTipoRegistro(corredor10K);
        maraton2026.agregarTipoRegistro(corredor21K);

        Patrocinio patrocinioMaraton = new Patrocinio(
                LocalDate.of(2026, 4, 10),
                50000.0,
                8,
                "1004",
                NivelPatrocinio.ORO,
                ancap,
                corredor10K
        );
        maraton2026.agregarPatrocinio(patrocinioMaraton);

        maraton2026.altaRegistro(
                jperez,
                corredor10K,
                LocalDate.of(2026, 5, 2)
        );

        manejadorEvento.agregar(maraton);

        // =========================
        // Evento: Montevideo Rock
        // =========================

        Evento rock = new Evento(
                "Montevideo Rock",
                "Festival con artistas nacionales e internacionales.",
                LocalDate.of(2023, 8, 20),
                "MVDROCK",
                Arrays.asList(musica)
        );
        rock.asignarImagen(imagenes.get(
                "imagenes/eventos/MontevideoRock/rock.png"
        ));

        EdicionEvento rock2026 = new EdicionEvento(
                "Montevideo Rock 2026",
                "ROCK26",
                LocalDate.of(2026, 12, 5),
                LocalDate.of(2026, 12, 6),
                LocalDate.of(2026, 4, 18),
                "Montevideo",
                "Uruguay",
                antel
        );
        rock2026.asignarImagen(imagenes.get(
                "imagenes/eventos/MontevideoRock/ediciones/rock2026.webp"
        ));
        try {
            rock2026.aceptar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(e);
        }

        rock.agregarEdicion(rock2026);
        antel.agregarEdicion(rock2026);

        TipoRegistro entradaRock = new TipoRegistro(
                "Entrada general",
                "Acceso a los dos días del festival.",
                2500.0,
                1000
        );
        rock2026.agregarTipoRegistro(entradaRock);

        Patrocinio patrocinioRock = new Patrocinio(
                LocalDate.of(2026, 5, 12),
                30000.0,
                2,
                "1005",
                NivelPatrocinio.PLATA,
                udelar,
                entradaRock
        );
        rock2026.agregarPatrocinio(patrocinioRock);

        rock2026.altaRegistro(
                vale23,
                entradaRock,
                LocalDate.of(2026, 6, 1)
        );

        manejadorEvento.agregar(rock);



        // =========================
        // Evento: Expo Negocios UY
        // =========================

        Evento expoNegocios = new Evento(
                "Expo Negocios UY",
                "Encuentro de empresas y emprendedores del país.",
                LocalDate.of(2025, 2, 5),
                "EXPONEG",
                Arrays.asList(negocios, tecnologia)
        );
        expoNegocios.asignarImagen(imagenes.get(
                "imagenes/eventos/ExpoNegociosUY/negocios.png"
        ));

        EdicionEvento expo2026 = new EdicionEvento(
                "Expo Negocios 2026",
                "EXPO26",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 20),
                LocalDate.of(2026, 5, 30),
                "Punta del Este",
                "Uruguay",
                antel
        );
        expo2026.asignarImagen(imagenes.get(
                "imagenes/eventos/ExpoNegociosUY/ediciones/negocios2026.png"
        ));
        // Queda INGRESADA.

        expoNegocios.agregarEdicion(expo2026);
        antel.agregarEdicion(expo2026);

        manejadorEvento.agregar(expoNegocios);

        // =========================
        // Evento: Montevideo Comics
        // =========================

        Evento comics = new Evento(
                "Montevideo Comics",
                "Convención de historietas, cine y cultura geek.",
                LocalDate.of(2022, 6, 30),
                "MVDCOM",
                Arrays.asList(cultura)
        );
        comics.asignarImagen(imagenes.get(
                "imagenes/eventos/MontevideoComics/eventoComic.png"
        ));

        EdicionEvento comics2026 = new EdicionEvento(
                "Comics 2026",
                "COM26",
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 10, 4),
                LocalDate.of(2026, 1, 22),
                "Montevideo",
                "Uruguay",
                imm
        );
        comics2026.asignarImagen(imagenes.get(
                "imagenes/eventos/MontevideoComics/ediciones/comic2026.jpg"
        ));
        try {
            comics2026.aceptar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(e);
        }

        comics.agregarEdicion(comics2026);
        imm.agregarEdicion(comics2026);

        TipoRegistro entradaComics = new TipoRegistro(
                "Entrada general",
                "Acceso a la convención.",
                900.0,
                400
        );
        comics2026.agregarTipoRegistro(entradaComics);

        manejadorEvento.agregar(comics);

        return true;
    }

    private static void aceptar(EdicionEvento edicion) {
        try {
            edicion.aceptar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(
                    "No se pudo aceptar la edición "
                            + edicion.getNombre(),
                    e
            );
        }
    }

    private static void rechazar(EdicionEvento edicion) {
        try {
            edicion.rechazar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(
                    "No se pudo rechazar la edición "
                            + edicion.getNombre(),
                    e
            );
        }
    }

    /**
     * Carga todas las imagenes de los datos de prueba, antes de guardar nada:
     * si falta una, se corta sin dejar la base a medio llenar.
     *
     * <p>No es privado para que DatosDePruebaTest pueda llamarlo y comprobar
     * que las 18 imagenes siguen estando dentro del jar. Antes vivian en la
     * carpeta del servidor web, y al separar los repositorios desaparecieron
     * sin que nada avisara hasta que alguien vaciaba la base.
     */
    static Map<String, Imagen>
    cargarImagenesAntesDePersistir() {

        Map<String, Imagen> imagenes =
                new LinkedHashMap<>();

        String[] rutas = {
                "imagenes/instituciones/avatarInstitucion.png",
                "imagenes/usuarios/avatarFemenino.png",
                "imagenes/usuarios/avatarMasculino.png",

                "imagenes/eventos/JIAP/eventoIng.png",
                "imagenes/eventos/FeriaDelLibro/eventoLibro.png",
                "imagenes/eventos/MaratonDeMontevideo/maraton.png",
                "imagenes/eventos/MontevideoRock/rock.png",
                "imagenes/eventos/ExpoNegociosUY/negocios.png",
                "imagenes/eventos/MontevideoComics/eventoComic.png",

                "imagenes/eventos/JIAP/ediciones/edicionIng2026.png",
                "imagenes/eventos/JIAP/ediciones/edicionIng2027.png",
                "imagenes/eventos/JIAP/ediciones/edicionIng2025.png",

                "imagenes/eventos/FeriaDelLibro/ediciones/edicionLib2026.png",

                "imagenes/eventos/MaratonDeMontevideo/ediciones/maraton2026.png",
                "imagenes/eventos/MaratonDeMontevideo/ediciones/maraton2025.png",

                "imagenes/eventos/MontevideoRock/ediciones/rock2026.webp",

                "imagenes/eventos/ExpoNegociosUY/ediciones/negocios2026.png",

                "imagenes/eventos/MontevideoComics/ediciones/comic2026.jpg"
        };

        for (String ruta : rutas) {
            imagenes.put(ruta, leerImagenObligatoria(ruta));
        }

        return imagenes;
    }

    /**
     * Lee una imagen de las que viajan DENTRO del jar de la logica, en
     * src/main/resources/imagenes.
     *
     * <p>Antes se leian del disco, con una ruta relativa que entraba en la
     * carpeta del servidor web. Eso tenia dos problemas: ataba el Servidor
     * Central a archivos de otro componente, justo al reves de como va la
     * dependencia, y solo funcionaba si el programa se corria parado en la
     * raiz del proyecto. Desde el classpath anda igual corriendolo desde
     * IntelliJ, desde Maven o desde el jar ya empaquetado.
     */
    private static Imagen leerImagenObligatoria(String recurso) {
        byte[] contenido;

        try (InputStream entrada = DatosDePrueba.class
                .getClassLoader()
                .getResourceAsStream(recurso)) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontró la imagen obligatoria: " + recurso
                );
            }

            contenido = entrada.readAllBytes();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo leer la imagen: " + recurso, e
            );
        }

        if (contenido.length == 0) {
            throw new IllegalStateException(
                    "La imagen está vacía: " + recurso
            );
        }

        String nombre = recurso.toLowerCase();
        String tipo;

        if (nombre.endsWith(".png")) {
            tipo = "image/png";
        } else if (nombre.endsWith(".jpg")
                || nombre.endsWith(".jpeg")) {
            tipo = "image/jpeg";
        } else if (nombre.endsWith(".webp")) {
            tipo = "image/webp";
        } else {
            throw new IllegalStateException(
                    "Formato de imagen no permitido: " + recurso
            );
        }

        return new Imagen(contenido, tipo);
    }
}