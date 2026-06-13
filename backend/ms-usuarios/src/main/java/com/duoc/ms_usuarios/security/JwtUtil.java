package com.duoc.ms_usuarios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // Se lee desde application.properties: jwt.secret
    @Value("${jwt.secret}")
    private String secret;

    // Token válido por 2 horas
    private static final long EXPIRATION_MS = 2 * 60 * 60 * 1000L;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT con el username y rol del usuario.
     */
    public String generarToken(String username, String rol, Long usuarioId) {
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("usuarioId", usuarioId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    /**
     * Valida el token y devuelve true si es correcto y no ha expirado.
     */
    public boolean validarToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameDesdeToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRolDesdeToken(String token) {
        return getClaims(token).get("rol", String.class);
    }

    public Long getUsuarioIdDesdeToken(String token) {
        return getClaims(token).get("usuarioId", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}