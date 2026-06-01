package com.duoc.ms_notificaciones;

import com.duoc.ms_notificaciones.controller.NotificacionController;
import com.duoc.ms_notificaciones.exception.GlobalExceptionHandler;
import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.repository.NotificacionRepository;
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
    private NotificacionRepository notificacionRepository;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() throws Exception {
        notificacionRepository = mock(NotificacionRepository.class);
        exceptionHandler = new GlobalExceptionHandler();

        NotificacionController controllerReal = new NotificacionController();
        java.lang.reflect.Field field = NotificacionController.class.getDeclaredField("notificacionRepository");
        field.setAccessible(true);
        field.set(controllerReal, notificacionRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controllerReal)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void testManejarValidaciones_Estructura() throws Exception {
        BindException bindException = new BindException(new Notificacion(), "notificacion");
        Method metodoReal = NotificacionController.class.getMethod("enviarAlerta", Notificacion.class);
        MethodParameter param = MethodParameter.forExecutable(metodoReal, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindException);

        ResponseEntity<Map<String, String>> respuesta = exceptionHandler.manejarValidaciones(ex);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void testManejarNoEncontrado() throws Exception {
        Notificacion validadorDto = new Notificacion();
        validadorDto.setDestinatario("central@incendios.cl");
        validadorDto.setMensaje("Alerta activa");

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String jsonValido = mapper.writeValueAsString(validadorDto);

        when(notificacionRepository.save(any(Notificacion.class))).thenThrow(new EntityNotFoundException("Registro ausente"));

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Registro ausente"));
    }

    @Test
    void testManejarErrorGeneral() throws Exception {
        Notificacion validadorDto = new Notificacion();
        validadorDto.setDestinatario("central@incendios.cl");
        validadorDto.setMensaje("Alerta activa");

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String jsonValido = mapper.writeValueAsString(validadorDto);

        when(notificacionRepository.save(any(Notificacion.class))).thenThrow(new RuntimeException("Fallo del sistema"));

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"))
                .andExpect(jsonPath("$.detalle").value("Fallo del sistema"));
    }
}