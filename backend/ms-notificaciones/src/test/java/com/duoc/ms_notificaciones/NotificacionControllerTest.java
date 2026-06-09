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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - NotificacionController")
class NotificacionControllerTest {

    private MockMvc mockMvc;
    private NotificacionService notificacionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        notificacionService = mock(NotificacionService.class);

        NotificacionController notificacionController =
                new NotificacionController(notificacionService);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(notificacionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Debe procesar el envio de alerta exitosamente y retornar HTTP 200")
    void testEnviarAlerta_Exitoso() throws Exception {
        Notificacion notificacionInput = new Notificacion();
        notificacionInput.setDestinatario("brigadista@incendios.cl");
        notificacionInput.setMensaje("Foco de incendio detectado en sector sur");

        Notificacion notificacionPersistida = new Notificacion();
        notificacionPersistida.setId(1L);
        notificacionPersistida.setDestinatario("brigadista@incendios.cl");
        notificacionPersistida.setMensaje("Foco de incendio detectado en sector sur");

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenReturn(notificacionPersistida);

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificacionInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.destinatario").value("brigadista@incendios.cl"))
                .andExpect(jsonPath("$.mensaje").value("Foco de incendio detectado en sector sur"));

        verify(notificacionService, times(1))
                .enviarAlerta(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe retornar HTTP 400 cuando el mensaje esta vacio")
    void testEnviarAlerta_MensajeVacio() throws Exception {
        Notificacion notificacionInvalida = new Notificacion();
        notificacionInvalida.setDestinatario("brigadista@incendios.cl");
        notificacionInvalida.setMensaje("");

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificacionInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El mensaje no puede estar vacío"));

        verify(notificacionService, never())
                .enviarAlerta(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe listar el historial de notificaciones")
    void testListarHistorial_Exitoso() throws Exception {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");
        notificacion.setMensaje("Alerta activa");

        when(notificacionService.listarHistorial())
                .thenReturn(List.of(notificacion));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].destinatario").value("BRIGADAS_ZONA_SUR"))
                .andExpect(jsonPath("$[0].mensaje").value("Alerta activa"));

        verify(notificacionService, times(1))
                .listarHistorial();
    }

    @Test
    @DisplayName("Debe obtener una notificacion por ID")
    void testObtenerPorId_Exitoso() throws Exception {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");
        notificacion.setMensaje("Alerta activa");

        when(notificacionService.obtenerPorId(1L))
                .thenReturn(notificacion);

        mockMvc.perform(get("/api/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.destinatario").value("BRIGADAS_ZONA_SUR"))
                .andExpect(jsonPath("$.mensaje").value("Alerta activa"));

        verify(notificacionService, times(1))
                .obtenerPorId(1L);
    }

    @Test
    @DisplayName("Debe retornar HTTP 404 cuando no existe la notificacion")
    void testObtenerPorId_NoEncontrado() throws Exception {
        when(notificacionService.obtenerPorId(99L))
                .thenThrow(new EntityNotFoundException(
                        "La notificación con ID 99 no existe."
                ));

        mockMvc.perform(get("/api/notificaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(
                        "La notificación con ID 99 no existe."
                ));

        verify(notificacionService, times(1))
                .obtenerPorId(99L);
    }
}