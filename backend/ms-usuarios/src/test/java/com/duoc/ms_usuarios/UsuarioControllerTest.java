package com.duoc.ms_usuarios;

import com.duoc.ms_usuarios.controller.UsuarioController;
import com.duoc.ms_usuarios.exception.GlobalExceptionHandler;
import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - UsuarioController")
class UsuarioControllerTest {

    private UsuarioService usuarioService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        UsuarioController usuarioController = new UsuarioController(usuarioService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        usuarioBase = new Usuario();
        usuarioBase.setId(1L);
        usuarioBase.setNombre("Juan");
        usuarioBase.setApellido("Perez");
        usuarioBase.setEmail("juan@mail.com");
        usuarioBase.setTelefono("+56 9 1234 5678");
        usuarioBase.setUsername("juan123");
        usuarioBase.setPassword("ClaveSegura123");
        usuarioBase.setRol("USER");
        usuarioBase.setActivo(true);
    }

    @Test
    @DisplayName("GET /api/usuarios lista todos los usuarios")
    void testListar() throws Exception {
        when(usuarioService.listar())
                .thenReturn(List.of(usuarioBase));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("juan123"))
                .andExpect(jsonPath("$[0].email").value("juan@mail.com"));

        verify(usuarioService, times(1)).listar();
    }

    @Test
    @DisplayName("POST /api/usuarios guarda un nuevo usuario")
    void testAgregar() throws Exception {
        when(usuarioService.guardar(any(Usuario.class)))
                .thenReturn(usuarioBase);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioBase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("juan123"));

        verify(usuarioService, times(1)).guardar(any(Usuario.class));
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} actualiza un usuario usando headers del gateway")
    void testActualizar() throws Exception {
        when(usuarioService.actualizar(
                eq(1L),
                any(Usuario.class),
                eq(1L),
                eq("ADMIN")
        )).thenReturn(usuarioBase);

        mockMvc.perform(put("/api/usuarios/1")
                        .header("X-Usuario-Id", "1")
                        .header("X-Usuario-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioBase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("juan123"));

        verify(usuarioService, times(1))
                .actualizar(eq(1L), any(Usuario.class), eq(1L), eq("ADMIN"));
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} retorna 400 si regla de negocio falla")
    void testActualizar_ReglaNegocio() throws Exception {
        when(usuarioService.actualizar(
                eq(1L),
                any(Usuario.class),
                eq(1L),
                eq("ADMIN")
        )).thenThrow(new IllegalArgumentException("No puedes quitarte el rol ADMIN a tu propia cuenta."));

        mockMvc.perform(put("/api/usuarios/1")
                        .header("X-Usuario-Id", "1")
                        .header("X-Usuario-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioBase)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No puedes quitarte el rol ADMIN a tu propia cuenta."));
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} elimina un usuario usando header X-Usuario-Id")
    void testEliminar() throws Exception {
        when(usuarioService.eliminar(eq(2L), eq(1L)))
                .thenReturn(true);

        mockMvc.perform(delete("/api/usuarios/2")
                        .header("X-Usuario-Id", "1"))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).eliminar(2L, 1L);
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} retorna 400 si intenta eliminar su propia cuenta")
    void testEliminar_PropiaCuenta() throws Exception {
        when(usuarioService.eliminar(eq(1L), eq(1L)))
                .thenThrow(new IllegalArgumentException("No puedes eliminar tu propia cuenta."));

        mockMvc.perform(delete("/api/usuarios/1")
                        .header("X-Usuario-Id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No puedes eliminar tu propia cuenta."));
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} retorna 404 si usuario no existe")
    void testEliminar_NoEncontrado() throws Exception {
        when(usuarioService.eliminar(eq(99L), eq(1L)))
                .thenThrow(new EntityNotFoundException("Usuario no encontrado con ID: 99"));

        mockMvc.perform(delete("/api/usuarios/99")
                        .header("X-Usuario-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado con ID: 99"));
    }
}