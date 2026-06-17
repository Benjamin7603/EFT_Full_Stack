package com.duoc.ms_gateway;

import com.duoc.ms_gateway.filter.JwtAuthFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas Unitarias - JwtAuthFilter")
class JwtAuthFilterTest {

    private JwtAuthFilterTestHelper filtro;
    private GatewayFilterChain chain;
    private final String secretVal =
            "mi_clave_secreta_super_segura_para_el_filtro_jwt_123456";

    @BeforeEach
    void setUp() {
        filtro = new JwtAuthFilterTestHelper();
        ReflectionTestUtils.setField(filtro, "secret", secretVal);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    /** Crea un exchange real con MockServerHttpRequest (sin mocks de clases finales). */
    private MockServerWebExchange exchange(HttpMethod method, String path, String authHeader) {
        MockServerHttpRequest.BaseBuilder<?> builder =
                MockServerHttpRequest.method(method, path);
        if (authHeader != null) {
            builder.header(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return MockServerWebExchange.from(builder.build());
    }

    private String tokenValido(String subject, String rol, Long usuarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);
        claims.put("usuarioId", usuarioId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(
                        Keys.hmacShaKeyFor(secretVal.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================================================
    // Orden del filtro
    // =========================================================
    @Test
    @DisplayName("getOrder() - retorna -1")
    void testGetOrder() {
        assertEquals(-1, filtro.getOrder());
    }

    // =========================================================
    // esRutaPublica — todas las ramas
    // =========================================================
    @Test
    @DisplayName("esRutaPublica - /api/auth/login POST es publica")
    void testRutaPublica_AuthLogin() {
        assertTrue(filtro.esRutaPublicaPublica("/api/auth/login", HttpMethod.POST));
    }

    @Test
    @DisplayName("esRutaPublica - /api/auth/* GET tambien es publica (startsWith)")
    void testRutaPublica_AuthGet() {
        assertTrue(filtro.esRutaPublicaPublica("/api/auth/register", HttpMethod.GET));
    }

    @Test
    @DisplayName("esRutaPublica - POST /api/usuarios es registro, publica")
    void testRutaPublica_UsuariosPost() {
        assertTrue(filtro.esRutaPublicaPublica("/api/usuarios", HttpMethod.POST));
    }

    @Test
    @DisplayName("esRutaPublica - GET /api/usuarios es privada")
    void testRutaPrivada_UsuariosGet() {
        assertFalse(filtro.esRutaPublicaPublica("/api/usuarios", HttpMethod.GET));
    }

    @Test
    @DisplayName("esRutaPublica - /swagger-ui/* es publica")
    void testRutaPublica_SwaggerUi() {
        assertTrue(filtro.esRutaPublicaPublica("/swagger-ui/index.html", HttpMethod.GET));
    }

    @Test
    @DisplayName("esRutaPublica - /v3/api-docs es publica")
    void testRutaPublica_ApiDocs() {
        assertTrue(filtro.esRutaPublicaPublica("/v3/api-docs", HttpMethod.GET));
    }

    @Test
    @DisplayName("esRutaPublica - /actuator/health es publica")
    void testRutaPublica_Actuator() {
        assertTrue(filtro.esRutaPublicaPublica("/actuator/health", HttpMethod.GET));
    }

    @Test
    @DisplayName("esRutaPublica - /api/incendios GET es privada")
    void testRutaPrivada_Incendios() {
        assertFalse(filtro.esRutaPublicaPublica("/api/incendios", HttpMethod.GET));
    }

    @Test
    @DisplayName("esRutaPublica - /api/incendios/reporte POST es privada")
    void testRutaPrivada_IncendiosPost() {
        assertFalse(filtro.esRutaPublicaPublica("/api/incendios/reporte", HttpMethod.POST));
    }

    // =========================================================
    // filter() — ruta publica pasa sin token
    // =========================================================
    @Test
    @DisplayName("filter() - ruta publica pasa directamente al chain")
    void testFilter_RutaPublica_PasaDirecto() {
        MockServerWebExchange ex = exchange(HttpMethod.POST, "/api/auth/login", null);

        filtro.filter(ex, chain).block();

        verify(chain, times(1)).filter(any());
    }

    // =========================================================
    // filter() — ruta privada sin header Authorization → 401
    // =========================================================
    @Test
    @DisplayName("filter() - ruta privada sin token retorna 401 con X-Auth-Error")
    void testFilter_SinToken_Unauthorized() {
        MockServerWebExchange ex = exchange(HttpMethod.GET, "/api/incendios", null);

        filtro.filter(ex, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        assertEquals("Token no proporcionado",
                ex.getResponse().getHeaders().getFirst("X-Auth-Error"));
        verify(chain, never()).filter(any());
    }

    // =========================================================
    // filter() — header Authorization sin prefijo "Bearer " → 401
    // =========================================================
    @Test
    @DisplayName("filter() - Authorization sin Bearer retorna 401")
    void testFilter_AuthSinBearer_Unauthorized() {
        MockServerWebExchange ex = exchange(HttpMethod.GET, "/api/incendios", "Basic dXNlcjpwYXNz");

        filtro.filter(ex, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        assertEquals("Token no proporcionado",
                ex.getResponse().getHeaders().getFirst("X-Auth-Error"));
        verify(chain, never()).filter(any());
    }

    // =========================================================
    // filter() — token mal formado → catch → 401
    // =========================================================
    @Test
    @DisplayName("filter() - token invalido entra al catch y retorna 401")
    void testFilter_TokenInvalido_CatchUnauthorized() {
        MockServerWebExchange ex = exchange(HttpMethod.GET, "/api/incendios",
                "Bearer token_malformado_incorrecto");

        filtro.filter(ex, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        assertEquals("Token inv\u00e1lido o expirado",
                ex.getResponse().getHeaders().getFirst("X-Auth-Error"));
        verify(chain, never()).filter(any());
    }

    // =========================================================
    // filter() — token valido → muta request con headers internos y pasa al chain
    // =========================================================
    @Test
    @DisplayName("filter() - token valido muta la peticion y llama al chain")
    void testFilter_TokenValido_MutarPeticionYPasar() {
        String token = tokenValido("admin@incendios.cl", "ADMIN", 123L);
        MockServerWebExchange ex = exchange(HttpMethod.GET, "/api/incendios/reporte",
                "Bearer " + token);

        // Necesitamos capturar el exchange mutado que llega al chain
        // para verificar los headers internos propagados
        final ServerWebExchange[] exchangeCapturado = new ServerWebExchange[1];
        when(chain.filter(any())).thenAnswer(inv -> {
            exchangeCapturado[0] = inv.getArgument(0);
            return Mono.empty();
        });

        filtro.filter(ex, chain).block();

        verify(chain, times(1)).filter(any());

        // Verifica los 3 headers internos que el filtro propaga a los microservicios
        ServerHttpRequest reqMutado = exchangeCapturado[0].getRequest();
        assertEquals("admin@incendios.cl",
                reqMutado.getHeaders().getFirst("X-Usuario-Username"));
        assertEquals("ADMIN",
                reqMutado.getHeaders().getFirst("X-Usuario-Rol"));
        assertEquals("123",
                reqMutado.getHeaders().getFirst("X-Usuario-Id"));
    }

    // =========================================================
    // Clase helper para acceder al metodo privado esRutaPublica
    // =========================================================
    private static class JwtAuthFilterTestHelper extends JwtAuthFilter {
        public boolean esRutaPublicaPublica(String path, HttpMethod method) {
            return (boolean) ReflectionTestUtils.invokeMethod(this, "esRutaPublica", path, method);
        }
    }
}