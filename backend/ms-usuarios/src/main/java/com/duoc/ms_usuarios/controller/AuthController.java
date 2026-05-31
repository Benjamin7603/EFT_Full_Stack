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
/**
 * Controlador encargado de los procesos de autenticación y seguridad.
 * Provee los endpoints necesarios para validar credenciales y emitir tokens de acceso.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    /**
     * Constructor para la inyección de dependencias requeridas por el controlador.
     * @param usuarioService Servicio para la gestión de usuarios.
     * @param jwtUtil Utilidad para la generación y validación de tokens JWT.
     * @param passwordEncoder Componente para verificar la coincidencia de contraseñas encriptadas.
     */
    public AuthController(UsuarioService usuarioService,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Endpoint para la autenticación de usuarios (Login).
     * Verifica la existencia del usuario, valida que la contraseña coincida utilizando
     * el encoder seguro y, si todo es correcto, genera un token JWT de acceso.
     * @param request Objeto {@link LoginRequest} que contiene las credenciales (username y password).
     * @return Un {@link ResponseEntity} que contiene el JSON con los datos del usuario autenticado
     * y su token, o un mensaje de error si las credenciales son inválidas.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.buscarPorUsername(request.username());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Contraseña incorrecta"));
        }
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