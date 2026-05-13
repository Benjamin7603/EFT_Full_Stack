package com.duoc.ms_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Permite que React (puerto 5173) se conecte
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        corsConfig.setMaxAge(3600L);
        // Permite todos los métodos HTTP
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta regla a TODAS las rutas del gateway (usuarios, reportes, etc.)
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}