package com.duoc.ms_usuarios;

import com.duoc.ms_usuarios.controller.UsuarioController;
import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - UsuarioController")
class UsuarioControllerTest {

    private UsuarioService usuarioService;
    private UsuarioController usuarioController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        usuarioController = new UsuarioController();

        ReflectionTestUtils.setField(usuarioController, "usuarioService", usuarioService);

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        usuarioBase = new Usuario();
        usuarioBase.setId(1L);
        usuarioBase.setNombre("Juan");
        usuarioBase.setApellido("Perez");
        usuarioBase.setEmail("juan@mail.com");
        usuarioBase.setUsername("juan123");
        usuarioBase.setPassword("ClaveSegura123");
        usuarioBase.setRol("USER");
    }

    @Test
    @DisplayName("GET /api/usuarios lista todos los usuarios")
    void testListar() throws Exception {
        when(usuarioService.listar()).thenReturn(Arrays.asList(usuarioBase));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("juan123"));
    }

    @Test
    @DisplayName("POST /api/usuarios guarda un nuevo usuario")
    void testAgregar() throws Exception {
        when(usuarioService.guardar(any(Usuario.class))).thenReturn(usuarioBase);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioBase)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} actualiza un usuario")
    void testActualizar() throws Exception {
        when(usuarioService.actualizar(eq(1L), any(Usuario.class))).thenReturn(usuarioBase);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioBase)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} elimina un usuario")
    void testEliminar() throws Exception {
        when(usuarioService.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).eliminar(1L);
    }
}