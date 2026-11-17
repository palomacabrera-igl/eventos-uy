package logica;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas de caja negra de {@link Institucion} y del dato de transporte que
 * entrega a la presentacion.
 *
 * <p>La letra, CU 14: «El organizador ingresa nombre (unico en la
 * plataforma), descripcion, sitio web y, de forma opcional, una imagen».
 *
 * <pre>
 *  obtenerDT()
 *  CV  una institucion con todos sus datos  -&gt; el DT los lleva todos
 *  CV  con imagen                           -&gt; tieneImagen true
 *  CV  sin imagen                           -&gt; tieneImagen false
 *
 *  tieneImagen()
 *  Clase de equivalencia              Caso
 *  ---------------------------------  --------------------------------
 *  CV  tiene imagen con contenido     bytes de un PNG
 *  CI  sin imagen                     null
 *  CI  imagen con contenido vacio     arreglo de cero bytes
 * </pre>
 */
@DisplayName("Institucion")
class InstitucionTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G'};

    private Institucion utec;

    @BeforeEach
    void crearInstitucion() {
        utec = new Institucion("UTEC", "Universidad Tecnologica", "https://utec.edu.uy");
    }

    // ----------------------------------------- lo que lleva el DT

    @Test
    @DisplayName("el DT lleva el nombre, la descripcion y el sitio web")
    void elDtLlevaLosDatos() {
        // Datos de prueba: la institucion creada arriba
        // Ejecucion
        DTInstitucion dt = utec.obtenerDT();

        // Resultado esperado
        assertEquals("UTEC", dt.getNombre());
        assertEquals("Universidad Tecnologica", dt.getDescripcion());
        assertEquals("https://utec.edu.uy", dt.getSitioWeb());
    }

    @Test
    @DisplayName("sin imagen, el DT lo dice y la pagina pone el poster de color")
    void sinImagen() {
        assertFalse(utec.tieneImagen());
        assertFalse(utec.obtenerDT().getTieneImagen());
        assertNull(utec.getImagen());
    }

    @Test
    @DisplayName("con imagen, el DT lo dice y los bytes quedan guardados")
    void conImagen() {
        utec.asignarImagen(new Imagen(PNG, "image/png"));

        assertTrue(utec.tieneImagen());
        assertTrue(utec.obtenerDT().getTieneImagen());
        assertArrayEquals(PNG, utec.getImagen().getContenido());
        assertEquals("image/png", utec.getImagen().getTipo());
    }

    @Test
    @DisplayName("valor limite: una imagen de cero bytes igual cuenta como imagen")
    void imagenVacia() {
        utec.asignarImagen(new Imagen(new byte[0], "image/png"));

        // tieneImagen() solo mira que haya una imagen con contenido NO NULO,
        // y un arreglo vacio no es nulo. Las cuatro entidades con imagen se
        // comportan igual, asi que no es una inconsistencia.
        //
        // Que esto no moleste depende de quien guarda: los servlets descartan
        // los archivos de tamanio cero antes de armar el DTImagen, y
        // altaUsuario y modificarUsuario comprueban length > 0. O sea que una
        // imagen vacia no llega a guardarse por ninguno de los caminos del
        // sitio. Si algun dia llegara, la pagina pondria un <img> que
        // /imagen responde con 404.
        assertTrue(utec.tieneImagen());
    }

    @ParameterizedTest(name = "imagen nula")
    @DisplayName("valor limite: asignar null deja la institucion sin imagen")
    @NullSource
    void imagenNula(Imagen ninguna) {
        utec.asignarImagen(new Imagen(PNG, "image/png"));
        assertTrue(utec.tieneImagen());

        utec.asignarImagen(ninguna);

        assertFalse(utec.tieneImagen());
    }

    // ------------------------------------------- los datos se modifican

    @ParameterizedTest(name = "nuevo nombre \"{0}\"")
    @DisplayName("los datos se pueden modificar y el DT refleja el cambio")
    @ValueSource(strings = {"UTEC Durazno", "Universidad Tecnologica del Uruguay"})
    void modificarLosDatos(String nombreNuevo) {
        utec.setNombre(nombreNuevo);
        utec.setDescripcion("Otra descripcion");
        utec.setSitioWeb("https://otro.uy");

        DTInstitucion dt = utec.obtenerDT();
        assertEquals(nombreNuevo, dt.getNombre());
        assertEquals("Otra descripcion", dt.getDescripcion());
        assertEquals("https://otro.uy", dt.getSitioWeb());
    }

    @Test
    @DisplayName("el DT corto, sin imagen, es el que se usa cuando todavia no existe")
    void dtSinImagen() {
        // Es el constructor que usan los formularios de alta, donde la
        // institucion todavia no se creo y la imagen no tiene valor posible.
        DTInstitucion dt = new DTInstitucion("Nueva", "Descripcion", "https://nueva.uy");

        assertEquals("Nueva", dt.getNombre());
        assertFalse(dt.getTieneImagen());
    }
}
