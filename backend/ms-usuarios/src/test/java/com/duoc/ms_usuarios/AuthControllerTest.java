package com.duoc.ms_usuarios;

import com.duoc.ms_usuarios.controller.AuthController;
import com.duoc.ms_usuarios.dto.AuthDTO.LoginRequest;
import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.security.JwtUtil;
import com.duoc.ms_usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - AuthController")
class AuthControllerTest {

    private UsuarioService usuarioService;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private AuthController authController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        jwtUtil = mock(JwtUtil.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authController = new AuthController(usuarioService, jwtUtil, passwordEncoder);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setNombre("Juan");
        usuarioValido.setUsername("juan123");
        usuarioValido.setPassword("passwordEncriptado");
        usuarioValido.setRol("USER");
    }

    @Test
    @DisplayName("Login con credenciales correctas retorna Token JWT")
    void testLogin_exitoso() throws Exception {
        LoginRequest request = new LoginRequest("juan123", "123456");

        when(usuarioService.buscarPorUsername("juan123")).thenReturn(usuarioValido);
        when(passwordEncoder.matches("123456", "passwordEncriptado")).thenReturn(true);
        when(jwtUtil.generarToken(anyString(), anyString(), anyLong())).thenReturn("token-valido-123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-valido-123"));
    }

    @Test
    @DisplayName("Login con rol nulo asigna por defecto el rol USER")
    void testLogin_rolNulo_asignaDefault() throws Exception {
        LoginRequest request = new LoginRequest("juan123", "123456");
        usuarioValido.setRol(null); // Gatilla la rama 'usuario.getRol() != null' como falsa

        when(usuarioService.buscarPorUsername("juan123")).thenReturn(usuarioValido);
        when(passwordEncoder.matches("123456", "passwordEncriptado")).thenReturn(true);
        when(jwtUtil.generarToken(anyString(), eq("USER"), anyLong())).thenReturn("token-default");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    @DisplayName("Login con usuario inexistente retorna 401 Unauthorized")
    void testLogin_usuarioNoExiste() throws Exception {
        LoginRequest request = new LoginRequest("erroneo", "123456");

        when(usuarioService.buscarPorUsername("erroneo")).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName("Login con contraseña incorrecta retorna 401 Unauthorized")
    void testLogin_contrasenaIncorrecta() throws Exception {
        LoginRequest request = new LoginRequest("juan123", "claveMala");

        when(usuarioService.buscarPorUsername("juan123")).thenReturn(usuarioValido);
        when(passwordEncoder.matches("claveMala", "passwordEncriptado")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Contraseña incorrecta"));
    }
}