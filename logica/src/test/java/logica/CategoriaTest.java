package logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CategoriaTest {

    @Test
    void jerarquizacionCategorias() {
        Categoria tecnologia = new Categoria("Tecnología");
        Categoria inteligenciaArtificial = new Categoria("Inteligencia artificial");
        Categoria aprendizajeAutomatico = new Categoria("Aprendizaje automático");

        tecnologia.agregarHija(inteligenciaArtificial);
        inteligenciaArtificial.agregarHija(aprendizajeAutomatico);

        DTCategoria resultado = tecnologia.obtenerDT();

        assertTrue(tecnologia.esRaiz());
        assertFalse(inteligenciaArtificial.esRaiz());
        assertEquals("Tecnología", resultado.getNombre());
        assertEquals(1, resultado.getHijas().size());
        assertEquals("Inteligencia artificial", resultado.getHijas().getFirst().getNombre());
        assertEquals("Aprendizaje automático", resultado.getHijas().getFirst().getHijas().getFirst().getNombre());
    }
}
