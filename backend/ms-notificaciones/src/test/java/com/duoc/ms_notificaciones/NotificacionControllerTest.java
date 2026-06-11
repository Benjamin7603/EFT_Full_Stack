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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private Notificacion crearNotificacion() {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setTitulo("Nuevo reporte");
        notificacion.setMensaje("Foco de incendio detectado en sector sur");
        notificacion.setDestinatario("BRIGADAS_ZONA_SUR");
        notificacion.setTipo("REPORTE");
        notificacion.setPrioridad("ALTA");
        notificacion.setLeida(false);
        notificacion.setReporteId(25L);
        return notificacion;
    }

    @Test
    @DisplayName("POST /api/notificaciones/enviar crea notificación")
    void testEnviarAlerta_Exitoso() throws Exception {
        Notificacion input = crearNotificacion();
        input.setId(null);

        Notificacion persistida = crearNotificacion();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenReturn(persistida);

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Nuevo reporte"))
                .andExpect(jsonPath("$.destinatario").value("BRIGADAS_ZONA_SUR"))
                .andExpect(jsonPath("$.mensaje").value("Foco de incendio detectado en sector sur"))
                .andExpect(jsonPath("$.leida").value(false));

        verify(notificacionService, times(1))
                .enviarAlerta(any(Notificacion.class));
    }

    @Test
    @DisplayName("POST /api/notificaciones crea notificación")
    void testCrear_Exitoso() throws Exception {
        Notificacion input = crearNotificacion();
        input.setId(null);

        Notificacion persistida = crearNotificacion();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenReturn(persistida);

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(notificacionService, times(1))
                .enviarAlerta(any(Notificacion.class));
    }

    @Test
    @DisplayName("POST retorna 400 cuando mensaje está vacío")
    void testEnviarAlerta_MensajeVacio() throws Exception {
        Notificacion invalida = crearNotificacion();
        invalida.setMensaje("");

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El mensaje no puede estar vacío"));

        verify(notificacionService, never())
                .enviarAlerta(any(Notificacion.class));
    }

    @Test
    @DisplayName("POST retorna 400 cuando destinatario está vacío")
    void testEnviarAlerta_DestinatarioVacio() throws Exception {
        Notificacion invalida = crearNotificacion();
        invalida.setDestinatario("");

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.destinatario").value("El destinatario no puede estar vacío"));

        verify(notificacionService, never())
                .enviarAlerta(any(Notificacion.class));
    }

    @Test
    @DisplayName("GET /api/notificaciones lista historial")
    void testListarHistorial_Exitoso() throws Exception {
        Notificacion notificacion = crearNotificacion();

        when(notificacionService.listarHistorial())
                .thenReturn(List.of(notificacion));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].destinatario").value("BRIGADAS_ZONA_SUR"))
                .andExpect(jsonPath("$[0].mensaje").value("Foco de incendio detectado en sector sur"));

        verify(notificacionService, times(1))
                .listarHistorial();
    }

    @Test
    @DisplayName("GET /api/notificaciones/{id} obtiene por ID")
    void testObtenerPorId_Exitoso() throws Exception {
        Notificacion notificacion = crearNotificacion();

        when(notificacionService.obtenerPorId(1L))
                .thenReturn(notificacion);

        mockMvc.perform(get("/api/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.destinatario").value("BRIGADAS_ZONA_SUR"));

        verify(notificacionService, times(1))
                .obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/notificaciones/{id} retorna 404 si no existe")
    void testObtenerPorId_NoEncontrado() throws Exception {
        when(notificacionService.obtenerPorId(99L))
                .thenThrow(new EntityNotFoundException("La notificación con ID 99 no existe."));

        mockMvc.perform(get("/api/notificaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La notificación con ID 99 no existe."));

        verify(notificacionService, times(1))
                .obtenerPorId(99L);
    }

    @Test
    @DisplayName("GET /destinatario/{destinatario} lista por destinatario")
    void testListarPorDestinatario() throws Exception {
        when(notificacionService.listarPorDestinatario("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of(crearNotificacion()));

        mockMvc.perform(get("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].destinatario").value("BRIGADAS_ZONA_SUR"));

        verify(notificacionService, times(1))
                .listarPorDestinatario("BRIGADAS_ZONA_SUR");
    }

    @Test
    @DisplayName("GET /destinatario/{destinatario}/no-leidas lista no leídas")
    void testListarNoLeidas() throws Exception {
        when(notificacionService.listarNoLeidas("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of(crearNotificacion()));

        mockMvc.perform(get("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR/no-leidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leida").value(false));

        verify(notificacionService, times(1))
                .listarNoLeidas("BRIGADAS_ZONA_SUR");
    }

    @Test
    @DisplayName("GET /destinatario/{destinatario}/contador cuenta no leídas")
    void testContarNoLeidas() throws Exception {
        when(notificacionService.contarNoLeidas("BRIGADAS_ZONA_SUR"))
                .thenReturn(3L);

        mockMvc.perform(get("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR/contador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noLeidas").value(3));

        verify(notificacionService, times(1))
                .contarNoLeidas("BRIGADAS_ZONA_SUR");
    }

    @Test
    @DisplayName("PATCH /{id}/leer marca como leída")
    void testMarcarComoLeida() throws Exception {
        Notificacion leida = crearNotificacion();
        leida.setLeida(true);

        when(notificacionService.marcarComoLeida(1L))
                .thenReturn(leida);

        mockMvc.perform(patch("/api/notificaciones/1/leer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));

        verify(notificacionService, times(1))
                .marcarComoLeida(1L);
    }

    @Test
    @DisplayName("PATCH /destinatario/{destinatario}/leer-todas marca todas como leídas")
    void testMarcarTodasComoLeidas() throws Exception {
        Notificacion leida = crearNotificacion();
        leida.setLeida(true);

        when(notificacionService.marcarTodasComoLeidas("BRIGADAS_ZONA_SUR"))
                .thenReturn(List.of(leida));

        mockMvc.perform(patch("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR/leer-todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leida").value(true));

        verify(notificacionService, times(1))
                .marcarTodasComoLeidas("BRIGADAS_ZONA_SUR");
    }

    @Test
    @DisplayName("DELETE /{id} elimina notificación")
    void testEliminar() throws Exception {
        doNothing().when(notificacionService).eliminar(1L);

        mockMvc.perform(delete("/api/notificaciones/1"))
                .andExpect(status().isOk());

        verify(notificacionService, times(1))
                .eliminar(1L);
    }
}