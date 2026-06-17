package com.duoc.ms_bff.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - FeignConfig")
class FeignConfigTest {

    private final FeignConfig feignConfig = new FeignConfig();

    @AfterEach
    void limpiarContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Propaga Authorization cuando viene con Bearer token")
    void testPropagaAuthorizationBearer() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Bearer token.jwt.valido");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().containsKey("Authorization"));
        assertTrue(template.headers().get("Authorization").contains("Bearer token.jwt.valido"));
    }

    @Test
    @DisplayName("No propaga Authorization cuando no existe header")
    void testNoPropagaAuthorizationCuandoNoExiste() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
    }

    @Test
    @DisplayName("No propaga Authorization cuando no es Bearer")
    void testNoPropagaAuthorizationNoBearer() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
    }

    @Test
    @DisplayName("No falla cuando no hay contexto de request")
    void testNoFallaSinRequestAttributes() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();

        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template = new RequestTemplate();

        assertDoesNotThrow(() -> interceptor.apply(template));
        assertTrue(template.headers().isEmpty());
    }

    @Test
    @DisplayName("Propaga headers X-Usuario cuando vienen con valor")
    void testPropagaHeadersUsuario() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Usuario-Username", "admin");
        httpRequest.addHeader("X-Usuario-Rol", "ADMIN");
        httpRequest.addHeader("X-Usuario-Id", "1");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().containsKey("X-Usuario-Username"));
        assertTrue(template.headers().get("X-Usuario-Username").contains("admin"));

        assertTrue(template.headers().containsKey("X-Usuario-Rol"));
        assertTrue(template.headers().get("X-Usuario-Rol").contains("ADMIN"));

        assertTrue(template.headers().containsKey("X-Usuario-Id"));
        assertTrue(template.headers().get("X-Usuario-Id").contains("1"));
    }

    @Test
    @DisplayName("Ignora headers X-Usuario cuando vienen en blanco")
    void testIgnoraHeadersUsuarioEnBlanco() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Usuario-Username", "   ");
        httpRequest.addHeader("X-Usuario-Rol", "");
        httpRequest.addHeader("X-Usuario-Id", "   ");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("X-Usuario-Username"));
        assertFalse(template.headers().containsKey("X-Usuario-Rol"));
        assertFalse(template.headers().containsKey("X-Usuario-Id"));
    }

    @Test
    @DisplayName("Propaga Authorization y headers X-Usuario al mismo tiempo")
    void testPropagaAuthorizationYHeadersUsuario() {
        RequestInterceptor interceptor = feignConfig.requestInterceptor();

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Bearer token.completo");
        httpRequest.addHeader("X-Usuario-Username", "bombero1");
        httpRequest.addHeader("X-Usuario-Rol", "BOMBERO");
        httpRequest.addHeader("X-Usuario-Id", "25");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().containsKey("Authorization"));
        assertTrue(template.headers().get("Authorization").contains("Bearer token.completo"));

        assertTrue(template.headers().containsKey("X-Usuario-Username"));
        assertTrue(template.headers().get("X-Usuario-Username").contains("bombero1"));

        assertTrue(template.headers().containsKey("X-Usuario-Rol"));
        assertTrue(template.headers().get("X-Usuario-Rol").contains("BOMBERO"));

        assertTrue(template.headers().containsKey("X-Usuario-Id"));
        assertTrue(template.headers().get("X-Usuario-Id").contains("25"));
    }
}