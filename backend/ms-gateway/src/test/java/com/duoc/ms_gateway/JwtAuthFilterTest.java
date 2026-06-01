package com.duoc.ms_gateway;

import com.duoc.ms_gateway.filter.JwtAuthFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas Unitarias - JwtAuthFilter")
class JwtAuthFilterTest {

    private JwtAuthFilterTestHelper jwtAuthFilterHelper;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private ServerHttpRequest request;
    private ServerHttpResponse response;
    private HttpHeaders headers;
    private String secretVal = "mi_clave_secreta_super_segura_para_el_filtro_jwt_123456";

    @BeforeEach
    void setUp() {
        jwtAuthFilterHelper = new JwtAuthFilterTestHelper();
        ReflectionTestUtils.setField(jwtAuthFilterHelper, "secret", secretVal);

        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);

        // El truco definitivo: RETURNS_DEEP_STUBS permite encadenar métodos mockeados sin declarar sus clases intermedias
        request = mock(ServerHttpRequest.class, Mockito.RETURNS_DEEP_STUBS);
        response = mock(ServerHttpResponse.class);
        headers = new HttpHeaders();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(headers);
    }

    private void configurarMockPath(String pathStr) {
        // Al usar RETURNS_DEEP_STUBS arriba, podemos simular la cadena completa de métodos en una sola línea
        when(request.getPath().value()).thenReturn(pathStr);
    }

    // =========================================================
    // Orden de Ejecución del Filtro
    // =========================================================
    @Test
    void testGetOrder() {
        int order = jwtAuthFilterHelper.getOrder();
        assertEquals(-1, order);
    }

    // =========================================================
    // Verificación de Rutas Públicas (Filtro Exitoso)
    // =========================================================
    @Test
    void testRutasPublicas() {
        assertTrue(jwtAuthFilterHelper.esRutaPublicaPublica("/api/auth/login", HttpMethod.POST));
        assertTrue(jwtAuthFilterHelper.esRutaPublicaPublica("/api/auth/register", HttpMethod.POST));
        assertTrue(jwtAuthFilterHelper.esRutaPublicaPublica("/api/usuarios", HttpMethod.POST));
        assertTrue(jwtAuthFilterHelper.esRutaPublicaPublica("/swagger-ui/index.html", HttpMethod.GET));
        assertTrue(jwtAuthFilterHelper.esRutaPublicaPublica("/v3/api-docs", HttpMethod.GET));
        assertTrue(jwtAuthFilterHelper.esRutaPublicaPublica("/actuator/health", HttpMethod.GET));
    }

    // =========================================================
    // Verificación de Rutas Privadas (Bloqueo Requerido)
    // =========================================================
    @Test
    void testRutasPrivadas() {
        assertFalse(jwtAuthFilterHelper.esRutaPublicaPublica("/api/usuarios", HttpMethod.GET));
        assertFalse(jwtAuthFilterHelper.esRutaPublicaPublica("/api/incendios/reporte", HttpMethod.POST));
    }

    // =========================================================
    // Flujo del Filtro: Ruta Pública Completa
    // =========================================================
    @Test
    void testFilter_RutaPublica_PasaDirecto() {
        configurarMockPath("/api/auth/login");
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = jwtAuthFilterHelper.filter(exchange, chain);
        assertTrue(result != null);
    }

    // =========================================================
    // Flujo del Filtro: Ruta Privada sin Token (Rechazo 401)
    // =========================================================
    @Test
    void testFilter_RutaPrivadaSinToken_RetornaUnauthorized() {
        configurarMockPath("/api/incendios");
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
        when(response.setComplete()).thenReturn(Mono.empty());

        jwtAuthFilterHelper.filter(exchange, chain);
        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    // =========================================================
    // Flujo del Filtro: Token Mal Formado (Bloque catch)
    // =========================================================
    @Test
    void testFilter_TokenInvalido_ExcepcionCatch() {
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer token_incorrecto");
        configurarMockPath("/api/incendios");
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(headers);
        when(response.setComplete()).thenReturn(Mono.empty());

        jwtAuthFilterHelper.filter(exchange, chain);
        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    // =========================================================
    // Flujo del Filtro: TOKEN VÁLIDO REAL (Cubre mutación de Request)
    // =========================================================
    @Test
    void testFilter_TokenValido_MutarPeticionYPasar() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", "ADMIN");
        claims.put("usuarioId", 123L);

        String tokenReal = Jwts.builder()
                .setClaims(claims)
                .setSubject("admin@incendios.cl")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(secretVal.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenReal);
        when(request.getHeaders()).thenReturn(headers);
        configurarMockPath("/api/incendios/reporte");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        ServerHttpRequest.Builder builderMock = mock(ServerHttpRequest.Builder.class);
        when(request.mutate()).thenReturn(builderMock);
        when(builderMock.header(anyString(), anyString())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(request);

        ServerWebExchange.Builder exchangeBuilderMock = mock(ServerWebExchange.Builder.class);
        when(exchange.mutate()).thenReturn(exchangeBuilderMock);
        when(exchangeBuilderMock.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilderMock);
        when(exchangeBuilderMock.build()).thenReturn(exchange);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        jwtAuthFilterHelper.filter(exchange, chain);
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    // =========================================================
    // Clase Helper para Evitar Lógica Reactiva
    // =========================================================
    private static class JwtAuthFilterTestHelper extends JwtAuthFilter {
        public boolean esRutaPublicaPublica(String path, HttpMethod method) {
            return (boolean) ReflectionTestUtils.invokeMethod(this, "esRutaPublica", path, method);
        }
    }
}