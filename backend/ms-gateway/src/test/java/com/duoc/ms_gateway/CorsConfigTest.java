package com.duoc.ms_gateway;

import com.duoc.ms_gateway.config.CorsConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Pruebas Unitarias - CorsConfig")
class CorsConfigTest {

    @Test
    @DisplayName("corsWebFilter() - retorna filtro CORS inicializado correctamente")
    void testCorsWebFilterInitialization() {
        CorsConfig corsConfig = new CorsConfig();
        CorsWebFilter filter = corsConfig.corsWebFilter();
        assertNotNull(filter);
    }
}