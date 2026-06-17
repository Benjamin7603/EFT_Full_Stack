package com.duoc.ms_geografico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cubre el contexto de arranque de Spring Boot.
 * Usa el perfil "test" (application-test.yml) para sustituir
 * PostgreSQL por H2 en memoria y deshabilitar Eureka.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Prueba de arranque - MsGeograficoApplication")
class MsGeograficoApplicationTest {

    @Test
    @DisplayName("El contexto de Spring Boot carga correctamente")
    void contextLoads() {
        // Si el contexto no levanta, el test falla automáticamente.
    }
}