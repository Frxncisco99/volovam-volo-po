package org.example.servicio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    @Test
    void hashNoGuardaTextoPlanoYVerificaPasswordCorrecto() {
        PasswordService service = new PasswordService();

        String hash = service.hash("ClaveSegura123");

        assertNotEquals("ClaveSegura123", hash);
        assertTrue(service.verificar("ClaveSegura123", hash));
        assertFalse(service.verificar("otraClave", hash));
    }

    @Test
    void noAceptaHashInvalido() {
        PasswordService service = new PasswordService();

        assertFalse(service.verificar("admin", "admin"));
        assertFalse(service.verificar("admin", ""));
        assertFalse(service.verificar(null, "$2a$12$hash"));
    }
}
