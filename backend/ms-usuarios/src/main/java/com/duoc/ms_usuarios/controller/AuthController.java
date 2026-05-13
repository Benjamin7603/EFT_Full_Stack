package com.duoc.ms_usuarios.controller;

import com.duoc.ms_usuarios.dto.AuthDTO.LoginRequest;
import com.duoc.ms_usuarios.dto.AuthDTO.LoginResponse;
import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.security.JwtUtil;
import com.duoc.ms_usuarios.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioService usuarioService,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /api/auth/login
     * Body: { "username": "juan", "password": "123456" }
     * Response: { "token": "eyJ...", "username": "juan", "rol": "USER", ... }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. Buscar usuario por username
        Usuario usuario = usuarioService.buscarPorUsername(request.username());

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        // 2. Verificar contraseña
        // NOTA: Si las contraseñas aún no están encriptadas en BD, usar:
        //   if (!request.password().equals(usuario.getPassword()))
        // Cuando migren a BCrypt, cambiar a:
        //   if (!passwordEncoder.matches(request.password(), usuario.getPassword()))
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Contraseña incorrecta"));
        }

        // 3. Generar token
        String rol = usuario.getRol() != null ? usuario.getRol() : "USER";
        String token = jwtUtil.generarToken(usuario.getUsername(), rol, usuario.getId());

        return ResponseEntity.ok(new LoginResponse(
                token,
                usuario.getUsername(),
                rol,
                usuario.getId(),
                usuario.getNombre()
        ));
    }
}