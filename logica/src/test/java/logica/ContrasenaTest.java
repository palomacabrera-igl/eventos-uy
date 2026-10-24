package logica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ContrasenaTest {

    @Test
    void hashCoincideConContrasena() {
        String huella = Contrasena.hashear("ClaveSegura2026!");

        assertTrue(Contrasena.coincide("ClaveSegura2026!", huella));
    }

    @Test
    void contrasenaDistinta() {
        String huella = Contrasena.hashear("ClaveSegura2026!");

        assertFalse(Contrasena.coincide("OtraClave", huella));
    }

    @Test
    void hashsDistintosMismaContrasena() {
        String primeraHuella = Contrasena.hashear("ClaveSegura2026!");
        String segundaHuella = Contrasena.hashear("ClaveSegura2026!");

        assertNotEquals(primeraHuella, segundaHuella);
        assertTrue(Contrasena.coincide("ClaveSegura2026!", primeraHuella));
        assertTrue(Contrasena.coincide("ClaveSegura2026!", segundaHuella));
    }
}
