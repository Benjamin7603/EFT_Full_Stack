package com.duoc.ms_reportes;

import com.duoc.ms_reportes.controller.ReporteController;
import com.duoc.ms_reportes.exception.GlobalExceptionHandler;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.service.ReporteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ReporteService reporteService;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        reporteService = mock(ReporteService.class);

        ReporteController reporteController = new ReporteController(reporteService);
        exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders
                .standaloneSetup(reporteController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("Maneja MethodArgumentNotValidException")
    void testManejarValidaciones() throws Exception {
        BindException bindException = new BindException(new Reporte(), "reporte");
        bindException.rejectValue("descripcion", "NotBlank", "La descripción es obligatoria");

        Method metodoReal = ReporteController.class.getMethod("listar");
        MethodParameter param = MethodParameter.forExecutable(metodoReal, -1);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarValidaciones(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "La descripción es obligatoria",
                respuesta.getBody().get("descripcion")
        );
    }

    @Test
    @DisplayName("Maneja EntityNotFoundException desde controller")
    void testManejarNoEncontrado() throws Exception {
        when(reporteService.listarTodos())
                .thenThrow(new EntityNotFoundException("El reporte no existe."));

        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El reporte no existe."));
    }

    @Test
    @DisplayName("Maneja EntityNotFoundException directamente")
    void testManejarNoEncontradoDirecto() {
        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarNoEncontrado(
                        new EntityNotFoundException("No encontrado directo")
                );

        assertNotNull(respuesta);
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("No encontrado directo", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Maneja IllegalArgumentException")
    void testManejarBadRequest() {
        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarBadRequest(
                        new IllegalArgumentException("Solicitud inválida")
                );

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Solicitud inválida", respuesta.getBody().get("error"));
    }

    @Test
    @DisplayName("Maneja ResponseStatusException")
    void testManejarResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para gestionar reportes."
        );

        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarResponseStatus(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "No tienes permisos para gestionar reportes.",
                respuesta.getBody().get("error")
        );
    }

    @Test
    @DisplayName("Maneja Exception genérica desde controller")
    void testManejarErrorGeneral() throws Exception {
        when(reporteService.listarTodos())
                .thenThrow(new RuntimeException("Fallo en base de datos"));

        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en el servidor"))
                .andExpect(jsonPath("$.detalle").value("Fallo en base de datos"));
    }

    @Test
    @DisplayName("Maneja Exception genérica directamente")
    void testManejarErrorGeneralDirecto() {
        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarErrorGeneral(
                        new RuntimeException("Fallo directo")
                );

        assertNotNull(respuesta);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Error inesperado en el servidor", respuesta.getBody().get("mensaje"));
        assertEquals("Fallo directo", respuesta.getBody().get("detalle"));
    }
}