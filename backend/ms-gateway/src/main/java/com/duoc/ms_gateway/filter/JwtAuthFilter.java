package com.duoc.ms_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Rutas que NO requieren token.
     * El Gateway las deja pasar sin validar.
     */
    private static final List<String> RUTAS_PUBLICAS = List.of(
            "/api/auth/login",        // login
            "/api/usuarios"           // registro (POST) — ver lógica abajo
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path   = request.getPath().value();
        HttpMethod method = request.getMethod();

        // 1. Dejar pasar rutas públicas
        if (esRutaPublica(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Buscar header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rechazar(exchange, "Token no proporcionado");
        }

        // 3. Validar el token
        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 4. Propagar datos del usuario como headers internos
            //    Los microservicios pueden leerlos con @RequestHeader
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Usuario-Username", claims.getSubject())
                    .header("X-Usuario-Rol",      claims.get("rol", String.class))
                    .header("X-Usuario-Id",        String.valueOf(claims.get("usuarioId", Long.class)))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            return rechazar(exchange, "Token inválido o expirado");
        }
    }

    private boolean esRutaPublica(String path, HttpMethod method) {
        // /api/auth/login siempre pública
        if (path.startsWith("/api/auth/")) return true;

        // POST /api/usuarios es registro → público
        // GET /api/usuarios requiere token (listar usuarios es privado)
        if (path.equals("/api/usuarios") && HttpMethod.POST.equals(method)) return true;

        // Swagger / actuator
        if (path.contains("/swagger-ui") || path.contains("/api-docs") ||
                path.startsWith("/actuator")) return true;

        return false;
    }

    private Mono<Void> rechazar(ServerWebExchange exchange, String motivo) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", motivo);
        return exchange.getResponse().setComplete();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return -1; // Ejecutarse antes que cualquier otro filtro
    }
}