package logica;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Calculo y verificacion de la huella de una contrasena. No se guarda la
 * contrasena sino su huella (un hash: no se puede volver a la contrasena); para
 * verificar un login se recalcula la huella de lo que se escribio y se comparan.
 *
 * Usa PBKDF2, que viene en el JDK, y es lento a proposito para que probar muchas
 * contrasenas sea caro. Cada contrasena lleva su propia sal (un valor al azar), asi
 * dos personas con la misma contrasena no tienen la misma huella.
 *
 * Se guarda en una sola columna con el formato iteraciones:sal:huella (sal y huella
 * en Base64, 76 caracteres: entra en el varchar(100) de Usuario). Es de paquete a
 * proposito: solo la logica calcula huellas.
 */
final class Contrasena {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";

    /** Recomendacion vigente de OWASP para PBKDF2 con HMAC-SHA256. */
    private static final int ITERACIONES = 210_000;

    private static final int BYTES_DE_SAL = 16;
    private static final int BITS_DE_HUELLA = 256;

    /** SecureRandom y no Random: la sal tiene que ser imprevisible. */
    private static final SecureRandom AZAR = new SecureRandom();

    private Contrasena() {}

    /** Devuelve "iteraciones:sal:huella" listo para guardar. */
    static String hashear(String texto) {
        byte[] sal = new byte[BYTES_DE_SAL];
        AZAR.nextBytes(sal);
        byte[] huella = calcular(texto, sal, ITERACIONES);
        Base64.Encoder base64 = Base64.getEncoder();
        return ITERACIONES + ":" + base64.encodeToString(sal) + ":" + base64.encodeToString(huella);
    }

    /**
     * true si el intento corresponde a la huella guardada. Devuelve false, y
     * no explota, cuando no hay nada guardado o lo guardado no tiene el
     * formato esperado.
     */
    static boolean coincide(String intento, String guardado) {
        if (intento == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 3) {
            return false;
        }

        int iteraciones;
        byte[] sal;
        byte[] huellaGuardada;
        try {
            iteraciones = Integer.parseInt(partes[0]);
            sal = Base64.getDecoder().decode(partes[1]);
            huellaGuardada = Base64.getDecoder().decode(partes[2]);
        } catch (IllegalArgumentException datoRaro) {
            return false;
        }

        byte[] huellaDelIntento = calcular(intento, sal, iteraciones);

        // isEqual y no Arrays.equals: compara en tiempo constante, sin cortar
        // en el primer byte distinto.
        return MessageDigest.isEqual(huellaGuardada, huellaDelIntento);
    }

    private static byte[] calcular(String texto, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec receta = new PBEKeySpec(texto.toCharArray(), sal, iteraciones, BITS_DE_HUELLA);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(receta).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException falla) {
            // Las dos son imposibles con un JDK completo: PBKDF2 es estandar.
            throw new IllegalStateException("No se pudo calcular la huella de la contrasena", falla);
        }
    }
}
