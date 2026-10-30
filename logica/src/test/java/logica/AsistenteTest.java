package logica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de caja negra de la regla de la fecha de nacimiento de
 * {@link Asistente}.
 *
 * <p>Una persona no puede haber nacido despues de hoy. Hasta que aparecio
 * esta regla el unico control era que la fecha no viniera vacia, asi que el
 * formulario aceptaba el dia de maniana.
 *
 * <p>La regla recibe el "hoy" como parametro justamente para poder probarla:
 * si leyera el reloj de la maquina, la prueba de "maniana" dependeria del dia
 * en que se corre y seria distinta cada vez.
 *
 * <pre>
 *  fechaDeNacimientoEnElFuturo(fechaNacimiento, hoy)
 *
 *  Clase de equivalencia                 Caso
 *  ------------------------------------  --------------------------------
 *  CV  nacio bastante antes de hoy       hace 30 anios
 *  CV  nacio ayer         (valor limite) el dia anterior
 *  CV  nacio hoy          (valor limite) el mismo dia
 *  CI  nace maniana       (valor limite) el dia siguiente
 *  CI  nace mucho despues                dentro de 10 anios
 *  CV  falta la fecha                    null
 *  CV  falta el hoy                      null
 * </pre>
 *
 * <p>Los dos valores limite que importan son hoy y maniana: ahi esta el corte.
 * Nacer hoy vale, porque un recien nacido existe; nacer maniana no.
 */
@DisplayName("Asistente: la fecha de nacimiento no puede ser futura")
class AsistenteTest {

    /** Un "hoy" fijo, para que la prueba no dependa del dia en que se corre. */
    private static final LocalDate HOY = LocalDate.of(2026, 3, 15);

    @ParameterizedTest(name = "nacido el {0} no es futuro")
    @ValueSource(strings = {"1996-03-15", "2026-03-14", "2026-03-15"})
    @DisplayName("acepta cualquier fecha hasta hoy, hoy incluido")
    void aceptaHastaHoy(String fecha) {
        assertFalse(Asistente.fechaDeNacimientoEnElFuturo(LocalDate.parse(fecha), HOY));
    }

    @ParameterizedTest(name = "nacido el {0} es futuro")
    @ValueSource(strings = {"2026-03-16", "2036-03-15"})
    @DisplayName("rechaza cualquier fecha posterior a hoy")
    void rechazaDespuesDeHoy(String fecha) {
        assertTrue(Asistente.fechaDeNacimientoEnElFuturo(LocalDate.parse(fecha), HOY));
    }

    @Test
    @DisplayName("sin fecha no dice que sea futura: eso lo controla el obligatorio")
    void sinFecha() {
        assertFalse(Asistente.fechaDeNacimientoEnElFuturo(null, HOY));
    }

    @Test
    @DisplayName("sin hoy tampoco puede decir que sea futura")
    void sinHoy() {
        assertFalse(Asistente.fechaDeNacimientoEnElFuturo(HOY, null));
    }
}
