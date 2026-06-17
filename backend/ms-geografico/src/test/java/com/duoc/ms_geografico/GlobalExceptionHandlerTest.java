package com.duoc.ms_geografico;

import com.duoc.ms_geografico.exception.GlobalExceptionHandler;
import com.duoc.ms_geografico.model.Ubicacion;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.duoc.ms_geografico.controller.GeograficoController;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // =========================================================
    // manejarValidaciones — sin errores de campo
    // =========================================================
    @Test
    @DisplayName("manejarValidaciones - retorna 400 con mapa vacio si no hay field errors")
    void testManejarValidaciones_SinErrores() throws Exception {
        BindException bindException = new BindException(new Ubicacion(), "ubicacion");
        Method metodo = GeograficoController.class.getMethod("guardarUbicacion", Ubicacion.class);
        MethodParameter param = MethodParameter.forExecutable(metodo, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> response = handler.manejarValidaciones(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // =========================================================
    // manejarValidaciones — con errores de campo reales
    // =========================================================
    @Test
    @DisplayName("manejarValidaciones - retorna 400 con los mensajes de cada campo invalido")
    void testManejarValidaciones_ConErrores() throws Exception {
        BindException bindException = new BindException(new Ubicacion(), "ubicacion");
        bindException.addError(new FieldError("ubicacion", "idReporte", "El ID del reporte es obligatorio"));
        bindException.addError(new FieldError("ubicacion", "latitud",  "La latitud es obligatoria"));
        bindException.addError(new FieldError("ubicacion", "longitud", "La longitud es obligatoria"));

        Method metodo = GeograficoController.class.getMethod("guardarUbicacion", Ubicacion.class);
        MethodParameter param = MethodParameter.forExecutable(metodo, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> response = handler.manejarValidaciones(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El ID del reporte es obligatorio", response.getBody().get("idReporte"));
        assertEquals("La latitud es obligatoria",        response.getBody().get("latitud"));
        assertEquals("La longitud es obligatoria",       response.getBody().get("longitud"));
    }

    // =========================================================
    // manejarNoEncontrado — EntityNotFoundException
    // =========================================================
    @Test
    @DisplayName("manejarNoEncontrado - retorna 404 con mensaje de la excepcion")
    void testManejarNoEncontrado() {
        EntityNotFoundException ex = new EntityNotFoundException(
                "No se encontr\u00f3 ubicaci\u00f3n geogr\u00e1fica para el reporte con ID: 42");

        ResponseEntity<Map<String, String>> response = handler.manejarNoEncontrado(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "No se encontr\u00f3 ubicaci\u00f3n geogr\u00e1fica para el reporte con ID: 42",
                response.getBody().get("error"));
    }

    // =========================================================
    // manejarErrorGeneral — Exception generica
    // =========================================================
    @Test
    @DisplayName("manejarErrorGeneral - retorna 500 con mensaje y detalle")
    void testManejarErrorGeneral() {
        Exception ex = new RuntimeException("Fallo inesperado de BD");

        ResponseEntity<Map<String, String>> response = handler.manejarErrorGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error inesperado en ms-geografico", response.getBody().get("mensaje"));
        assertEquals("Fallo inesperado de BD",            response.getBody().get("detalle"));
    }
}