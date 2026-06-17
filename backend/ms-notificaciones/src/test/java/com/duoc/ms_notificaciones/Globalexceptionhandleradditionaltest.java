package com.duoc.ms_notificaciones;

import com.duoc.ms_notificaciones.controller.NotificacionController;
import com.duoc.ms_notificaciones.exception.GlobalExceptionHandler;
import com.duoc.ms_notificaciones.model.Notificacion;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios adicionales para GlobalExceptionHandler.
 * Cubren los handlers invocados directamente sin pasar por MockMvc,
 * garantizando cobertura de línea en cada método del handler.
 */
@DisplayName("Pruebas Adicionales - GlobalExceptionHandler")
class GlobalExceptionHandlerAdditionalTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // =========================================================
    // manejarValidaciones — sin errores de campo (mapa vacío)
    // =========================================================
    @Test
    @DisplayName("manejarValidaciones - sin errores retorna 400 con mapa vacío")
    void testManejarValidaciones_SinErrores() throws Exception {
        BindException bindException = new BindException(new Notificacion(), "notificacion");
        Method metodo = NotificacionController.class.getMethod("enviarAlerta", Notificacion.class);
        MethodParameter param = MethodParameter.forExecutable(metodo, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> response = handler.manejarValidaciones(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // =========================================================
    // manejarValidaciones — con múltiples errores de campo
    // =========================================================
    @Test
    @DisplayName("manejarValidaciones - múltiples errores retorna todos los mensajes")
    void testManejarValidaciones_MultiplesCampos() throws Exception {
        BindException bindException = new BindException(new Notificacion(), "notificacion");
        bindException.rejectValue("mensaje",     "NotBlank", "El mensaje no puede estar vacío");
        bindException.rejectValue("destinatario","NotBlank", "El destinatario no puede estar vacío");

        Method metodo = NotificacionController.class.getMethod("enviarAlerta", Notificacion.class);
        MethodParameter param = MethodParameter.forExecutable(metodo, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> response = handler.manejarValidaciones(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El mensaje no puede estar vacío",        response.getBody().get("mensaje"));
        assertEquals("El destinatario no puede estar vacío",   response.getBody().get("destinatario"));
    }

    // =========================================================
    // manejarNoEncontrado — EntityNotFoundException directo
    // =========================================================
    @Test
    @DisplayName("manejarNoEncontrado - retorna 404 con mensaje de la excepción")
    void testManejarNoEncontrado_Directo() {
        EntityNotFoundException ex =
                new EntityNotFoundException("La notificación con ID 42 no existe.");

        ResponseEntity<Map<String, String>> response = handler.manejarNoEncontrado(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("La notificación con ID 42 no existe.", response.getBody().get("error"));
    }

    // =========================================================
    // manejarBadRequest — IllegalArgumentException directo
    // =========================================================
    @Test
    @DisplayName("manejarBadRequest - retorna 400 con mensaje de la excepción")
    void testManejarBadRequest_Directo() {
        IllegalArgumentException ex =
                new IllegalArgumentException("El destinatario es obligatorio.");

        ResponseEntity<Map<String, String>> response = handler.manejarBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El destinatario es obligatorio.", response.getBody().get("error"));
    }

    // =========================================================
    // manejarBadRequest — mensaje vacío (caso borde)
    // =========================================================
    @Test
    @DisplayName("manejarBadRequest - mensaje vacío retorna 400 con error null")
    void testManejarBadRequest_MensajeNull() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);

        ResponseEntity<Map<String, String>> response = handler.manejarBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().get("error"));
    }

    // =========================================================
    // manejarErrorGeneral — Exception genérica directo
    // =========================================================
    @Test
    @DisplayName("manejarErrorGeneral - retorna 500 con mensaje y detalle")
    void testManejarErrorGeneral_Directo() {
        Exception ex = new RuntimeException("Fallo inesperado de BD");

        ResponseEntity<Map<String, String>> response = handler.manejarErrorGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error inesperado en ms-notificaciones", response.getBody().get("mensaje"));
        assertEquals("Fallo inesperado de BD",                response.getBody().get("detalle"));
    }

    // =========================================================
    // manejarErrorGeneral — excepción con mensaje null
    // =========================================================
    @Test
    @DisplayName("manejarErrorGeneral - excepción sin mensaje retorna detalle null")
    void testManejarErrorGeneral_SinMensaje() {
        Exception ex = new RuntimeException();

        ResponseEntity<Map<String, String>> response = handler.manejarErrorGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error inesperado en ms-notificaciones", response.getBody().get("mensaje"));
        assertNull(response.getBody().get("detalle"));
    }
}