package logica;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Calculo y verificacion de la huella de una contrasena.
 *
 * No se guarda la contrasena, se guarda su huella. Un hash va en un solo
 * sentido: de la contrasena sale la huella, pero de la huella no se puede
 * volver a la contrasena. Para verificar un login se recalcula la huella de
 * lo que la persona escribio y se comparan las dos huellas.
 *
 * Se usa PBKDF2, que viene en el JDK y no agrega ninguna dependencia. Es un
 * hash LENTO a proposito: un hash rapido como SHA-256 a secas permitiria
 * probar millones de contrasenas por segundo contra las huellas robadas.
 *
 * Cada contrasena lleva su propia SAL: un valor al azar que se mezcla antes
 * de calcular la huella. Sin sal, dos personas con la misma contrasena
 * tendrian la misma huella (se notaria mirando la tabla) y servirian las
 * tablas de huellas ya calculadas que circulan para las contrasenas comunes.
 *
 * Formato de lo que se guarda, en una sola columna:
 *
 *     iteraciones:sal:huella        con la sal y la huella en Base64
 *
 * Son 76 caracteres, asi que entran en el varchar(100) de Usuario. Guardar
 * las iteraciones adentro permite subirlas mas adelante sin invalidar las
 * contrasenas ya guardadas.
 *
 * De paquete (sin public) a proposito: solo la logica calcula huellas. Un
 * servlet o una JSP no tienen nada que hacer aca.
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
