package com.duoc.ms_usuarios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener el header Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        String rol = null;

        // 2. Verificar que tenga el prefijo Bearer
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Quitar "Bearer "
            try {
                if (jwtUtil.validarToken(token)) {
                    username = jwtUtil.getUsernameDesdeToken(token);
                    rol = jwtUtil.getRolDesdeToken(token);
                }
            } catch (Exception e) {
                System.out.println("Error validando el token: " + e.getMessage());
            }
        }

        // 3. Si el token es válido y no hay autenticación actual, autenticar en Spring Security
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Le damos al usuario el ROL que venía dentro del token (ej: ROLE_CIUDADANO o ROLE_USER)
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase());

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(authority)
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Establecer el usuario como autenticado en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 4. Continuar con la petición
        filterChain.doFilter(request, response);
    }
}