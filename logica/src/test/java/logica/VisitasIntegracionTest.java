package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import persistencia.Persistencia;

/**
 * Visitas y eventos mas visitados (letra, 7.7 y 6.1) contra la base.
 *
 * <pre>
 *  Escenario que arma cada prueba, y que borra al terminar:
 *      cuatro eventos "Test VI A", "Test VI B", "Test VI C" y "Test VI D", sin visitas
 *
 *  registrarVisita(nombreEvento)
 *  CV  suma una visita y queda guardada; tres visitas suman tres
 *  CV  muchas visitas a la vez no se pierden (UPDATE atomico)
 *  CV  otra operacion que modifica el evento no pisa las visitas
 *  CI  evento que no existe                              -&gt; ReglaNegocioException
 *  Valor limite: null, vacio, solo espacios; espacios a los costados
 *
 *  listarEventosMasVisitados(cantidad)
 *  CV  del que mas al que menos, y a igual cantidad por nombre
 *  CV  no pasa de "cantidad"; los que nadie consulto no entran
 *  Valor limite: cantidad 0 o negativa -&gt; lista vacia
 * </pre>
 */
@DisplayName("Visitas y eventos mas visitados contra la base")
class VisitasIntegracionTest extends PruebaConBase {

    private static final String A = "Test VI A";
    private static final String B = "Test VI B";
    private static final String C = "Test VI C";
    private static final String D = "Test VI D";

    /** Mas que cualquier evento real: los de prueba quedan primeros del ranking. */
    private static final long BASE = 1_000_000L;

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @BeforeEach
    void armarLosEventos() {
        sinRastros();
        Persistencia.enTransaccion(em -> {
            Categoria categoria = em.createQuery("SELECT c FROM Categoria c", Categoria.class)
                    .getResultStream().findFirst().orElseThrow();
            for (String nombre : List.of(A, B, C, D)) {
                em.persist(new Evento(nombre, "Evento de prueba", LocalDate.now(), "TVI",
                        List.of(categoria)));
            }
        });
    }

    @AfterEach
    void sinRastros() {
        borrar("evento_categoria",
                "evento_id IN (SELECT id FROM evento WHERE nombre LIKE 'Test VI %')");
        borrar("evento", "nombre LIKE 'Test VI %'");
    }

    private long visitas(String nombre) throws ReglaNegocioException {
        return sistema.consultarEvento(nombre).getVisitas();
    }

    /** Fija las visitas con SQL directo, como lo haria otro programa. */
    private static void fijarVisitas(String nombre, long cantidad) {
        Persistencia.enTransaccion(em -> em.createNativeQuery(
                "UPDATE evento SET visitas = " + cantidad + " WHERE nombre = '" + nombre + "'")
                .executeUpdate());
    }

    // ================================================== registrarVisita

    @Test
    @DisplayName("CV · un evento nuevo no tiene visitas")
    void alPrincipioNinguna() throws ReglaNegocioException {
        assertEquals(0, visitas(A));
    }

    @Test
    @DisplayName("CV · una visita suma uno y queda guardada")
    void unaVisita() throws ReglaNegocioException {
        sistema.registrarVisita(A);

        // Cada operacion lee la base de verdad: si se ve, es que se guardo.
        assertEquals(1, visitas(A));
        assertEquals(0, visitas(B));
    }

    @Test
    @DisplayName("CV · cada consulta cuenta, tambien las repetidas")
    void lasRepetidasCuentan() throws ReglaNegocioException {
        sistema.registrarVisita(A);
        sistema.registrarVisita(A);
        sistema.registrarVisita(A);

        assertEquals(3, visitas(A));
    }

    @Test
    @DisplayName("valor limite: los espacios a los costados del nombre no importan")
    void conEspacios() throws ReglaNegocioException {
        sistema.registrarVisita("  " + A + " ");

        assertEquals(1, visitas(A));
    }

    @Test
    @DisplayName("CV · la visita aparece tambien en los listados de eventos")
    void aparecenEnElListado() throws ReglaNegocioException {
        sistema.registrarVisita(A);

        long enElListado = sistema.listarEventos(null).stream()
                .filter(e -> A.equals(e.getNombre())).findFirst().orElseThrow().getVisitas();

        assertEquals(1, enElListado);
    }

    @Test
    @DisplayName("CV · cien visitas a la vez, desde diez hilos, suman exactamente cien")
    void visitasSimultaneasNoSePierden() throws Exception {
        int hilos = 10;
        int porHilo = 10;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Void>> resultados = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            Callable<Void> visitante = () -> {
                largada.await();   // todos arrancan en el mismo instante
                for (int j = 0; j < porHilo; j++) {
                    sistema.registrarVisita(A);
                }
                return null;
            };
            resultados.add(pool.submit(visitante));
        }
        largada.countDown();
        for (Future<Void> resultado : resultados) {
            resultado.get(60, TimeUnit.SECONDS);   // si alguna fallo, falla aca
        }
        pool.shutdown();

        assertEquals((long) hilos * porHilo, visitas(A),
                "leer el numero, sumarle uno y guardarlo pisaria visitas: tiene que ser un UPDATE");
    }

    @Test
    @DisplayName("CV · otra operacion que modifica el evento no pisa las visitas que entraron en el medio")
    void modificarElEventoNoPisaLasVisitas() throws Exception {
        ExecutorService otroHilo = Executors.newSingleThreadExecutor();
        try {
            Persistencia.enTransaccion(em -> {
                // Esta operacion lee el evento con 0 visitas...
                Evento evento = ManejadorEvento.getInstancia().buscar(A);
                assertEquals(0, evento.getVisitas());
                try {
                    // ...mientras otra peticion, con su propia transaccion, suma una y la confirma...
                    otroHilo.submit(() -> {
                        sistema.registrarVisita(A);
                        return null;
                    }).get(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new AssertionError("la visita de la otra peticion fallo", e);
                }
                // ...y cuando esta termina, guarda su cambio (como lo haria finalizar el evento).
                evento.setDescripcion("Descripcion cambiada");
            });
        } finally {
            otroHilo.shutdown();
        }

        assertEquals(1, visitas(A), "el UPDATE de la otra operacion no tiene que pisar la visita");
        assertEquals("Descripcion cambiada", sistema.consultarEvento(A).getDescripcion());
    }

    @Test
    @DisplayName("CI · un evento que no existe: error, y no se crea nada")
    void eventoInexistente() {
        assertThrows(ReglaNegocioException.class,
                () -> sistema.registrarVisita("Test VI no existe"));

        assertFalse(sistema.listarEventos(null).stream()
                .anyMatch(e -> "Test VI no existe".equals(e.getNombre())));
    }

    @ParameterizedTest(name = "nombre \"{0}\"")
    @DisplayName("valor limite: null, vacio o solo espacios es un error")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void sinNombre(String nombre) {
        assertThrows(ReglaNegocioException.class, () -> sistema.registrarVisita(nombre));
    }

    // ================================================== listarEventosMasVisitados

    private void armarElRanking() {
        fijarVisitas(A, BASE + 5);
        fijarVisitas(B, BASE + 2);
        fijarVisitas(C, BASE + 5);   // empata con A
        // D no tiene visitas
    }

    private List<String> nombresDeLosMios(List<DTEvento> ranking) {
        return ranking.stream().map(DTEvento::getNombre)
                .filter(n -> n.startsWith("Test VI ")).toList();
    }

    @Test
    @DisplayName("CV · del que mas visitas tiene al que menos, y a igual cantidad por nombre")
    void ordenado() {
        armarElRanking();

        List<String> mios = nombresDeLosMios(sistema.listarEventosMasVisitados(1000));

        assertEquals(List.of(A, C, B), mios);
    }

    @Test
    @DisplayName("CV · cada evento del ranking trae su cantidad de visitas")
    void conSuCantidad() {
        armarElRanking();

        List<DTEvento> ranking = sistema.listarEventosMasVisitados(3);

        assertEquals(List.of(A, C, B), ranking.stream().map(DTEvento::getNombre).toList());
        assertEquals(List.of(BASE + 5, BASE + 5, BASE + 2),
                ranking.stream().map(DTEvento::getVisitas).toList());
    }

    @Test
    @DisplayName("CV · no devuelve mas que la cantidad pedida")
    void respetaLaCantidad() {
        armarElRanking();

        List<DTEvento> ranking = sistema.listarEventosMasVisitados(2);

        assertEquals(List.of(A, C), ranking.stream().map(DTEvento::getNombre).toList());
    }

    @Test
    @DisplayName("CV · un evento que nadie consulto no es de los mas visitados")
    void sinVisitasNoEntra() {
        armarElRanking();

        assertFalse(nombresDeLosMios(sistema.listarEventosMasVisitados(1000)).contains(D));
    }

    @Test
    @DisplayName("CV · si se pide mas de los que hay con visitas, devuelve los que hay")
    void pedirDeMas() {
        armarElRanking();

        List<DTEvento> ranking = sistema.listarEventosMasVisitados(10_000);

        assertTrue(ranking.size() >= 3);
        assertTrue(ranking.size() <= 10_000);
    }

    @ParameterizedTest(name = "cantidad {0}")
    @DisplayName("valor limite: cantidad cero o negativa devuelve la lista vacia")
    @ValueSource(ints = {0, -1, -100})
    void cantidadInvalida(int cantidad) {
        armarElRanking();

        assertTrue(sistema.listarEventosMasVisitados(cantidad).isEmpty());
    }

    @Test
    @DisplayName("CV · una visita nueva cambia el ranking")
    void unaVisitaCambiaElRanking() throws ReglaNegocioException {
        armarElRanking();
        sistema.registrarVisita(B);
        sistema.registrarVisita(B);
        sistema.registrarVisita(B);   // B pasa de BASE+2 a BASE+5: empata con A y C

        assertEquals(List.of(A, B, C), nombresDeLosMios(sistema.listarEventosMasVisitados(1000)));
    }
}
