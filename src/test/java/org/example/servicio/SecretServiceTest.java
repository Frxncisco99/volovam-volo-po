package org.example.servicio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretServiceTest {

    @Test
    void cifraYDescifraSecretos() {
        String encrypted = SecretService.encrypt("app-password-demo");

        assertTrue(encrypted.startsWith("enc:v1:"));
        assertNotEquals("app-password-demo", encrypted);
        assertEquals("app-password-demo", SecretService.decrypt(encrypted));
    }

    @Test
    void conservaValoresLegacySinPrefijo() {
        assertEquals("texto-plano-legacy", SecretService.decrypt("texto-plano-legacy"));
    }
}
