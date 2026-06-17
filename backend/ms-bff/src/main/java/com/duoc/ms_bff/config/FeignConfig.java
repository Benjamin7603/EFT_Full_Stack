package com.duoc.ms_bff.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Obtenemos la petición web original que llegó al BFF
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");

                // Si la petición traía un Token JWT, lo clonamos y lo enviamos al microservicio destino
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    requestTemplate.header("Authorization", authHeader);
                }

                copiarHeader(requestTemplate, request, "X-Usuario-Username");
                copiarHeader(requestTemplate, request, "X-Usuario-Rol");
                copiarHeader(requestTemplate, request, "X-Usuario-Id");
            }
        };
    }

    private void copiarHeader(RequestTemplate requestTemplate, HttpServletRequest request, String nombreHeader) {
        String valor = request.getHeader(nombreHeader);

        if (valor != null && !valor.isBlank()) {
            requestTemplate.header(nombreHeader, valor);
        }
    }
}
