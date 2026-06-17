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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests adicionales para NotificacionController.
 * Cubren los paths de error (500, 400, 404) en endpoints GET, PATCH y DELETE
 * que los tests existentes no ejercitan.
 */
@DisplayName("Pruebas Adicionales - NotificacionController")
class NotificacionControllerAdditionalTest {

    private MockMvc mockMvc;
    private NotificacionService notificacionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        notificacionService = mock(NotificacionService.class);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificacionController(notificacionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Notificacion crearNotificacion() {
        Notificacion n = new Notificacion();
        n.setId(1L);
        n.setTitulo("Nuevo reporte");
        n.setMensaje("Foco de incendio detectado");
        n.setDestinatario("BRIGADAS_ZONA_SUR");
        n.setTipo("REPORTE");
        n.setPrioridad("ALTA");
        n.setLeida(false);
        n.setReporteId(25L);
        return n;
    }

    // =========================================================
    // POST /enviar — IllegalArgumentException → 400
    // =========================================================
    @Test
    @DisplayName("POST /enviar - IllegalArgumentException del service retorna 400")
    void testEnviarAlerta_BadRequest() throws Exception {
        Notificacion input = crearNotificacion();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenThrow(new IllegalArgumentException("Destinatario inválido"));

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Destinatario inválido"));
    }

    // =========================================================
    // POST /enviar — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("POST /enviar - Exception genérica retorna 500")
    void testEnviarAlerta_ErrorGeneral() throws Exception {
        Notificacion input = crearNotificacion();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenThrow(new RuntimeException("Fallo de BD"));

        mockMvc.perform(post("/api/notificaciones/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"))
                .andExpect(jsonPath("$.detalle").value("Fallo de BD"));
    }

    // =========================================================
    // POST / (crear) — IllegalArgumentException → 400
    // =========================================================
    @Test
    @DisplayName("POST / - IllegalArgumentException retorna 400")
    void testCrear_BadRequest() throws Exception {
        Notificacion input = crearNotificacion();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenThrow(new IllegalArgumentException("Tipo de notificación inválido"));

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Tipo de notificación inválido"));
    }

    // =========================================================
    // POST / (crear) — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("POST / - Exception genérica retorna 500")
    void testCrear_ErrorGeneral() throws Exception {
        Notificacion input = crearNotificacion();

        when(notificacionService.enviarAlerta(any(Notificacion.class)))
                .thenThrow(new RuntimeException("Timeout de conexión"));

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // GET / (listarHistorial) — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("GET / - Exception genérica en listarHistorial retorna 500")
    void testListarHistorial_ErrorGeneral() throws Exception {
        when(notificacionService.listarHistorial())
                .thenThrow(new RuntimeException("Error de conexión"));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"))
                .andExpect(jsonPath("$.detalle").value("Error de conexión"));
    }

    // =========================================================
    // GET /{id} — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("GET /{id} - Exception genérica retorna 500")
    void testObtenerPorId_ErrorGeneral() throws Exception {
        when(notificacionService.obtenerPorId(1L))
                .thenThrow(new RuntimeException("Fallo inesperado"));

        mockMvc.perform(get("/api/notificaciones/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // GET /destinatario/{destinatario} — IllegalArgumentException → 400
    // =========================================================
    @Test
    @DisplayName("GET /destinatario/{dest} - IllegalArgumentException retorna 400")
    void testListarPorDestinatario_BadRequest() throws Exception {
        when(notificacionService.listarPorDestinatario("INVALIDO"))
                .thenThrow(new IllegalArgumentException("El destinatario es obligatorio."));

        mockMvc.perform(get("/api/notificaciones/destinatario/INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El destinatario es obligatorio."));
    }

    // =========================================================
    // GET /destinatario/{destinatario} — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("GET /destinatario/{dest} - Exception genérica retorna 500")
    void testListarPorDestinatario_ErrorGeneral() throws Exception {
        when(notificacionService.listarPorDestinatario("BRIGADAS_ZONA_SUR"))
                .thenThrow(new RuntimeException("Fallo de BD"));

        mockMvc.perform(get("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // GET /destinatario/{destinatario}/no-leidas — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("GET /destinatario/{dest}/no-leidas - Exception genérica retorna 500")
    void testListarNoLeidas_ErrorGeneral() throws Exception {
        when(notificacionService.listarNoLeidas("BRIGADAS_ZONA_SUR"))
                .thenThrow(new RuntimeException("Fallo inesperado"));

        mockMvc.perform(get("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR/no-leidas"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // GET /destinatario/{destinatario}/contador — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("GET /destinatario/{dest}/contador - Exception genérica retorna 500")
    void testContarNoLeidas_ErrorGeneral() throws Exception {
        when(notificacionService.contarNoLeidas("BRIGADAS_ZONA_SUR"))
                .thenThrow(new RuntimeException("Fallo inesperado"));

        mockMvc.perform(get("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR/contador"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // PATCH /{id}/leer — EntityNotFoundException → 404
    // =========================================================
    @Test
    @DisplayName("PATCH /{id}/leer - EntityNotFoundException retorna 404")
    void testMarcarComoLeida_NoEncontrado() throws Exception {
        when(notificacionService.marcarComoLeida(99L))
                .thenThrow(new EntityNotFoundException("La notificación con ID 99 no existe."));

        mockMvc.perform(patch("/api/notificaciones/99/leer"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La notificación con ID 99 no existe."));
    }

    // =========================================================
    // PATCH /{id}/leer — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("PATCH /{id}/leer - Exception genérica retorna 500")
    void testMarcarComoLeida_ErrorGeneral() throws Exception {
        when(notificacionService.marcarComoLeida(1L))
                .thenThrow(new RuntimeException("Fallo de BD"));

        mockMvc.perform(patch("/api/notificaciones/1/leer"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // PATCH /destinatario/{destinatario}/leer-todas — IllegalArgumentException → 400
    // =========================================================
    @Test
    @DisplayName("PATCH /destinatario/{dest}/leer-todas - IllegalArgumentException retorna 400")
    void testMarcarTodasComoLeidas_BadRequest() throws Exception {
        when(notificacionService.marcarTodasComoLeidas("INVALIDO"))
                .thenThrow(new IllegalArgumentException("El destinatario es obligatorio."));

        mockMvc.perform(patch("/api/notificaciones/destinatario/INVALIDO/leer-todas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El destinatario es obligatorio."));
    }

    // =========================================================
    // PATCH /destinatario/{destinatario}/leer-todas — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("PATCH /destinatario/{dest}/leer-todas - Exception genérica retorna 500")
    void testMarcarTodasComoLeidas_ErrorGeneral() throws Exception {
        when(notificacionService.marcarTodasComoLeidas("BRIGADAS_ZONA_SUR"))
                .thenThrow(new RuntimeException("Fallo de BD"));

        mockMvc.perform(patch("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR/leer-todas"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // DELETE /{id} — EntityNotFoundException → 404
    // =========================================================
    @Test
    @DisplayName("DELETE /{id} - EntityNotFoundException retorna 404")
    void testEliminar_NoEncontrado() throws Exception {
        doThrow(new EntityNotFoundException("La notificación con ID 99 no existe."))
                .when(notificacionService).eliminar(99L);

        mockMvc.perform(delete("/api/notificaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La notificación con ID 99 no existe."));
    }

    // =========================================================
    // DELETE /{id} — Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("DELETE /{id} - Exception genérica retorna 500")
    void testEliminar_ErrorGeneral() throws Exception {
        doThrow(new RuntimeException("Fallo de BD"))
                .when(notificacionService).eliminar(1L);

        mockMvc.perform(delete("/api/notificaciones/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-notificaciones"));
    }

    // =========================================================
    // GET / listarHistorial — lista vacía retorna 200 con array vacío
    // =========================================================
    @Test
    @DisplayName("GET / - lista vacía retorna 200 con array vacío")
    void testListarHistorial_Vacio() throws Exception {
        when(notificacionService.listarHistorial()).thenReturn(List.of());

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // =========================================================
    // GET /destinatario/{dest}/contador — 0 no leídas → retorna 0
    // =========================================================
    @Test
    @DisplayName("GET /destinatario/{dest}/contador - retorna 0 cuando no hay no leídas")
    void testContarNoLeidas_Cero() throws Exception {
        when(notificacionService.contarNoLeidas("BRIGADAS_ZONA_SUR")).thenReturn(0L);

        mockMvc.perform(get("/api/notificaciones/destinatario/BRIGADAS_ZONA_SUR/contador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noLeidas").value(0));
    }
}