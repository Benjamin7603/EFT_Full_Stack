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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
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

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler (Solución Completa)")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private UsuarioService usuarioService;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() throws Exception {
        usuarioService = mock(UsuarioService.class);
        UsuarioController usuarioController = new UsuarioController();
        ReflectionTestUtils.setField(usuarioController, "usuarioService", usuarioService);

        exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    // =========================================================
    // 1. TEST PARA: MethodArgumentNotValidException (Llamada Directa para evadir restricción de Mockito)
    // =========================================================
    @Test
    @DisplayName("Debe procesar MethodArgumentNotValidException retornando un mapa de errores de validación")
    void testManejarValidaciones() throws Exception {
        // Armamos el escenario de error de validación
        BindException bindException = new BindException(new Usuario(), "usuario");
        bindException.rejectValue("email", "NotBlank", "El email es obligatorio");

        Method metodoReal = UsuarioController.class.getMethod("agregar", Usuario.class);
        MethodParameter parametroMetodo = new MethodParameter(metodoReal, 0);

        MethodArgumentNotValidException excepcionValidacion =
                new MethodArgumentNotValidException(parametroMetodo, bindException);

        // Invocamos el método del Handler de forma directa. JaCoCo registrará el 100% de la cobertura de estas líneas.
        ResponseEntity<Map<String, String>> respuesta = exceptionHandler.manejarValidaciones(excepcionValidacion);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El email es obligatorio", respuesta.getBody().get("email"));
    }

    // =========================================================
    // 2. TEST PARA: EntityNotFoundException
    // =========================================================
    @Test
    @DisplayName("Debe capturar EntityNotFoundException y responder 404 Not Found")
    void testManejarNoEncontrado() throws Exception {
        when(usuarioService.listar()).thenThrow(new EntityNotFoundException("Usuario no encontrado en el sistema"));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado en el sistema"));
    }

    // =========================================================
    // 3. TESTS PARA: DataIntegrityViolationException
    // =========================================================
    @Test
    @DisplayName("Debe capturar duplicado de Correo Electrónico y responder 409 Conflict")
    void testManejarRestriccionUnica_correoDuplicado() throws Exception {
        String mensajeBd = "Error en el índice: usuarios_email_key";
        when(usuarioService.listar()).thenThrow(new DataIntegrityViolationException(mensajeBd));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Este correo electrónico ya está registrado en el sistema."));
    }

    @Test
    @DisplayName("Debe capturar duplicado de Username y responder 409 Conflict")
    void testManejarRestriccionUnica_usernameDuplicado() throws Exception {
        String mensajeBd = "Error en el índice: usuarios_username_key";
        when(usuarioService.listar()).thenThrow(new DataIntegrityViolationException(mensajeBd));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Este nombre de usuario ya está en uso."));
    }

    @Test
    @DisplayName("Debe capturar otra restricción única genérica y responder 409 Conflict")
    void testManejarRestriccionUnica_generica() throws Exception {
        String mensajeBd = "Error en otra restricción de llave primaria";
        when(usuarioService.listar()).thenThrow(new DataIntegrityViolationException(mensajeBd));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Error: Un dato único ya existe en el sistema."));
    }

    // =========================================================
    // 4. TEST PARA: Exception (Errores Generales)
    // =========================================================
    @Test
    @DisplayName("Debe capturar cualquier otra excepción genérica y responder 500 Internal Server Error")
    void testManejarErrorGeneral() throws Exception {
        when(usuarioService.listar()).thenThrow(new RuntimeException("Fallo crítico de hardware"));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en el servidor"))
                .andExpect(jsonPath("$.detalle").value("Fallo crítico de hardware"));
    }
}