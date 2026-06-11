package com.duoc.ms_usuarios;

import com.duoc.ms_usuarios.controller.UsuarioController;
import com.duoc.ms_usuarios.exception.GlobalExceptionHandler;
import com.duoc.ms_usuarios.model.Usuario;
import com.duoc.ms_usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private UsuarioService usuarioService;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        UsuarioController usuarioController = new UsuarioController(usuarioService);

        exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders
                .standaloneSetup(usuarioController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("Debe procesar MethodArgumentNotValidException retornando errores de validación")
    void testManejarValidaciones() throws Exception {
        BindException bindException = new BindException(new Usuario(), "usuario");
        bindException.rejectValue("email", "NotBlank", "El email es obligatorio");

        Method metodoReal = UsuarioController.class.getMethod("agregar", Usuario.class);
        MethodParameter parametroMetodo = new MethodParameter(metodoReal, 0);

        MethodArgumentNotValidException excepcionValidacion =
                new MethodArgumentNotValidException(parametroMetodo, bindException);

        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarValidaciones(excepcionValidacion);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El email es obligatorio", respuesta.getBody().get("email"));
    }

    @Test
    @DisplayName("Debe capturar EntityNotFoundException y responder 404")
    void testManejarNoEncontrado() throws Exception {
        when(usuarioService.listar())
                .thenThrow(new EntityNotFoundException("Usuario no encontrado en el sistema"));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado en el sistema"));
    }

    @Test
    @DisplayName("Debe capturar duplicado de correo y responder 409")
    void testManejarRestriccionUnica_correoDuplicado() throws Exception {
        String mensajeBd = "Error en el índice: usuarios_email_key";

        when(usuarioService.listar())
                .thenThrow(new DataIntegrityViolationException(mensajeBd));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Este correo electrónico ya está registrado en el sistema."));
    }

    @Test
    @DisplayName("Debe capturar duplicado de username y responder 409")
    void testManejarRestriccionUnica_usernameDuplicado() throws Exception {
        String mensajeBd = "Error en el índice: usuarios_username_key";

        when(usuarioService.listar())
                .thenThrow(new DataIntegrityViolationException(mensajeBd));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Este nombre de usuario ya está en uso."));
    }

    @Test
    @DisplayName("Debe capturar otra restricción única genérica y responder 409")
    void testManejarRestriccionUnica_generica() throws Exception {
        String mensajeBd = "Error en otra restricción de llave primaria";

        when(usuarioService.listar())
                .thenThrow(new DataIntegrityViolationException(mensajeBd));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Error: Un dato único ya existe en el sistema."));
    }

    @Test
    @DisplayName("Debe capturar DataIntegrityViolationException con mensaje null y responder 409 genérico")
    void testManejarRestriccionUnica_mensajeNull() throws Exception {
        when(usuarioService.listar())
                .thenThrow(new DataIntegrityViolationException(null));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Error: Un dato único ya existe en el sistema."));
    }

    @Test
    @DisplayName("Debe capturar IllegalArgumentException y responder 400")
    void testManejarBadRequest() throws Exception {
        when(usuarioService.listar())
                .thenThrow(new IllegalArgumentException("No puedes eliminar tu propia cuenta."));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No puedes eliminar tu propia cuenta."));
    }

    @Test
    @DisplayName("Debe capturar cualquier otra excepción genérica y responder 500")
    void testManejarErrorGeneral() throws Exception {
        when(usuarioService.listar())
                .thenThrow(new RuntimeException("Fallo crítico de hardware"));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en el servidor"))
                .andExpect(jsonPath("$.detalle").value("Fallo crítico de hardware"));
    }
}