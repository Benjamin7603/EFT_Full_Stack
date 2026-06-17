package com.duoc.ms_reportes;

import com.duoc.ms_reportes.controller.ReporteController;
import com.duoc.ms_reportes.exception.GlobalExceptionHandler;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.service.ReporteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.MethodParameter;

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
        // CORRECCIÓN: Si sigue fallando aquí, es porque la clase ReporteService
        // tiene lógica compleja en su constructor.
        // Vamos a usar mock(ReporteService.class) pero con la configuración de Mockito
        // para que sea más permisivo.
        reporteService = mock(ReporteService.class);

        ReporteController reporteController = new ReporteController(reporteService);
        exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(reporteController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    // =========================================================
    // MethodArgumentNotValidException
    // =========================================================
    @Test
    void testManejarValidaciones() throws Exception {
        BindException bindException = new BindException(new Reporte(), "reporte");
        bindException.rejectValue("descripcion", "NotBlank", "La descripción es obligatoria");

        Method metodoReal = ReporteController.class.getMethod("listar");
        MethodParameter param = MethodParameter.forExecutable(metodoReal, -1);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> respuesta = exceptionHandler.manejarValidaciones(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("La descripción es obligatoria", respuesta.getBody().get("descripcion"));
    }

    // =========================================================
    // EntityNotFoundException
    // =========================================================
    @Test
    void testManejarNoEncontrado() throws Exception {
        when(reporteService.listarTodos())
                .thenThrow(new EntityNotFoundException("El reporte no existe."));

        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El reporte no existe."));
    }

    // =========================================================
    // IllegalArgumentException
    // =========================================================
    @Test
    void testManejarBadRequest() {
        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarBadRequest(new IllegalArgumentException("Solicitud inválida"));

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Solicitud inválida", respuesta.getBody().get("error"));
    }

    // =========================================================
    // ResponseStatusException
    // =========================================================
    @Test
    void testManejarResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para gestionar reportes."
        );

        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarResponseStatus(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertEquals("No tienes permisos para gestionar reportes.", respuesta.getBody().get("error"));
    }

    // =========================================================
    // Exception Genérica
    // =========================================================
    @Test
    void testManejarErrorGeneral() throws Exception {
        when(reporteService.listarTodos())
                .thenThrow(new RuntimeException("Fallo en base de datos"));

        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en el servidor"))
                .andExpect(jsonPath("$.detalle").value("Fallo en base de datos"));
    }
}