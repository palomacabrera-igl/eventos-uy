package logica;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Alta de Institucion contra la base (CU 14) y Alta de Categoria.
 *
 * <p>InstitucionTest ya prueba la entidad y su DT. Esta prueba los dos casos
 * de uso, que hasta ahora no tenian ninguna prueba sobre la base.
 *
 * <p>La letra, CU 14: «El organizador ingresa nombre (unico en la plataforma),
 * descripcion, sitio web y, de forma opcional, una imagen. Si ya existe una
 * institucion con ese nombre, puede corregir los datos o cancelar».
 *
 * <pre>
 *  altaInstitucion(solicitante, nombre, descripcion, sitioWeb, imagen)
 *  CV  un organizador, sin imagen      -&gt; queda creada
 *  CV  un organizador, con imagen      -&gt; la imagen se guarda y se recupera
 *  CI  1. nombre repetido en la plataforma
 *  CI  2. un asistente o un visitante
 *
 *  altaCategoria(nombre, nombrePadre)
 *  CV  una categoria suelta
 *  CV  una categoria colgada de otra
 *  CI  nombre repetido
 * </pre>
 */
@DisplayName("Alta de Institucion y de Categoria contra la base (CU 14)")
class InstitucionIntegracionTest extends PruebaConBase {

    private static final String NOMBRE = "IT CU14 Instituto";
    private static final String CATEGORIA = "IT CU14 Categoria";

    private final IControladorSistema sistema =
            Fabrica.getInstancia().getControladorSistema();

    @AfterEach
    void sinRastros() {
        borrar("institucion", "nombre LIKE 'IT CU14%'");
        borrar("categoria", "nombre LIKE 'IT CU14%'");
    }

    // ----- altaInstitucion -----

    @Test
    @DisplayName("un organizador la crea con todos sus datos")
    void laCreaUnOrganizador() throws ReglaNegocioException {
        sistema.altaInstitucion("imm", NOMBRE, "Instituto de prueba",
                "https://prueba.uy", null);

        DTInstitucion dt = sistema.consultarInstitucion(NOMBRE);
        assertEquals(NOMBRE, dt.getNombre());
        assertEquals("Instituto de prueba", dt.getDescripcion());
        assertEquals("https://prueba.uy", dt.getSitioWeb());
        assertTrue(sistema.listarNombresInstituciones().contains(NOMBRE));
    }

    @Test
    @DisplayName("con imagen: queda guardada y se puede pedir")
    void conImagen() throws ReglaNegocioException {
        byte[] bytes = {1, 2, 3, 4};

        sistema.altaInstitucion("imm", NOMBRE, "Instituto de prueba",
                "https://prueba.uy", new DTImagen(bytes, "image/png"));

        assertTrue(sistema.consultarInstitucion(NOMBRE).getTieneImagen());
        assertArrayEquals(bytes, sistema.obtenerImagenInstitucion(NOMBRE).getContenido());
    }

    @Test
    @DisplayName("CI 1 · el nombre es unico en la plataforma")
    void nombreRepetido() throws ReglaNegocioException {
        sistema.altaInstitucion("imm", NOMBRE, "Instituto de prueba",
                "https://prueba.uy", null);

        assertThrows(ReglaNegocioException.class, () -> sistema.altaInstitucion(
                "imm", NOMBRE, "Otra descripcion", "https://otra.uy", null));
    }

    @ParameterizedTest(name = "solicitante \"{0}\"")
    @DisplayName("CI 2 · un asistente o un visitante no puede crear instituciones")
    @NullSource
    @ValueSource(strings = {"vale23"})
    void soloOrganizadores(String solicitante) {
        assertThrows(ReglaNegocioException.class, () -> sistema.altaInstitucion(
                solicitante, NOMBRE, "Instituto de prueba", "https://prueba.uy", null));
    }

    // ----- altaCategoria -----

    @Test
    @DisplayName("una categoria suelta queda creada")
    void categoriaSuelta() throws ReglaNegocioException {
        sistema.altaCategoria(CATEGORIA, null);

        assertTrue(sistema.listarCategorias().stream()
                .anyMatch(c -> c.getNombre().equals(CATEGORIA)));
    }

    @Test
    @DisplayName("una categoria se puede colgar de otra que ya existe")
    void categoriaConPadre() throws ReglaNegocioException {
        sistema.altaCategoria(CATEGORIA, null);
        sistema.altaCategoria(CATEGORIA + " hija", CATEGORIA);

        assertTrue(sistema.listarCategorias().stream()
                .anyMatch(c -> c.getNombre().equals(CATEGORIA + " hija")));
    }

    @Test
    @DisplayName("CI · el nombre de la categoria no se repite")
    void categoriaRepetida() throws ReglaNegocioException {
        sistema.altaCategoria(CATEGORIA, null);

        assertThrows(ReglaNegocioException.class,
                () -> sistema.altaCategoria(CATEGORIA, null));
    }
}
