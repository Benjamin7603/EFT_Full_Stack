package com.duoc.ms_notificaciones;

import com.duoc.ms_notificaciones.controller.NotificacionController;
import com.duoc.ms_notificaciones.exception.GlobalExceptionHandler;
import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas Unitarias - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private NotificacionService notificacionService;
    private GlobalExceptionHandler exceptionHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        notificacionService = mock(NotificacionService.class);
        exceptionHandler = new GlobalExceptionHandler();
        objectMapper = new ObjectMapper();

        NotificacionController controllerReal =
                new NotificacionController(notificacionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controllerReal)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    private Notificacion crearValida() {
        Notificacion notificacion = new Notificacion();
        notificacion.setTitulo("Nuevo reporte");
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");
        notificacion.setMensaje("Alerta activa");
        notificacion.setTipo("REPORTE");
        notificacion.setPrioridad("ALTA");
        notificacion.setLeida(false);
        return notificacion;
    }

    @Test
    void testManejarValidaciones_Estructura() throws Exception {
        BindException bindException =
                new BindException(new Notificacion(), "notificacion");

        bindException.rejectValue("mensaje", "NotBlank", "El mensaje no puede estar vacío");

        Method metodoReal =
                NotificacionController.class.getMethod("enviarAlerta", Notificacion.class);

        MethodParameter param =
                MethodParameter.forExecutable(metodoReal, 0);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> respuesta =
                exceptionHandler.manejarValidaciones(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El mensaje no puede estar vacío", respuesta.getBody().get("mensaje"));
    }

    @Test
    void testManejarNoEncontrado() throws Exception {
        Notificacion validadorDto = crearValida();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenThrow(new EntityNotFoundException("Registro ausente"));

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validadorDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Registro ausente"));
    }

    @Test
    void testManejarBadRequest() throws Exception {
        Notificacion validadorDto = crearValida();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenThrow(new IllegalArgumentException("El destinatario es obligatorio."));

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validadorDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El destinatario es obligatorio."));
    }

    @Test
    void testManejarErrorGeneral() throws Exception {
        Notificacion validadorDto = crearValida();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenThrow(new RuntimeException("Fallo del sistema"));

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validadorDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"))
                .andExpect(jsonPath("$.detalle").value("Fallo del sistema"));
    }
}