package com.duoc.ms_geografico;

import com.duoc.ms_geografico.controller.GeograficoController;
import com.duoc.ms_geografico.exception.GlobalExceptionHandler;
import com.duoc.ms_geografico.model.Ubicacion;
import com.duoc.ms_geografico.service.UbicacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests adicionales para GeograficoController.
 * Cubren los paths de ejecución que faltan para alcanzar >85 % de cobertura.
 */
@DisplayName("Pruebas Adicionales - GeograficoController")
class GeograficoControllerAdditionalTest {

    private MockMvc mockMvc;
    private UbicacionService ubicacionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ubicacionService = Mockito.mock(UbicacionService.class);
        GeograficoController controller = new GeograficoController(ubicacionService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================
    // GET /reporte/{id} — service lanza Exception genérica → 500
    // =========================================================
    @Test
    @DisplayName("GET /reporte/{id} - excepción genérica retorna 500")
    void testObtenerPorReporte_ErrorGeneral() throws Exception {
        when(ubicacionService.obtenerPorReporte(1L))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(get("/api/geografico/reporte/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado en ms-geografico"))
                .andExpect(jsonPath("$.detalle").value("Error de base de datos"));

        verify(ubicacionService, times(1)).obtenerPorReporte(1L);
    }

    // =========================================================
    // POST /guardar — con zonaRiesgo incluida
    // =========================================================
    @Test
    @DisplayName("POST /guardar - guarda ubicacion con zonaRiesgo y retorna 200")
    void testGuardarUbicacion_ConZonaRiesgo() throws Exception {
        Ubicacion input = new Ubicacion();
        input.setIdReporte(20L);
        input.setLatitud(-38.737);
        input.setLongitud(-72.590);
        input.setZonaRiesgo("ALTA");

        Ubicacion persistida = new Ubicacion();
        persistida.setId(2L);
        persistida.setIdReporte(20L);
        persistida.setLatitud(-38.737);
        persistida.setLongitud(-72.590);
        persistida.setZonaRiesgo("ALTA");

        when(ubicacionService.guardarUbicacion(any(Ubicacion.class))).thenReturn(persistida);

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.zonaRiesgo").value("ALTA"));
    }

    // =========================================================
    // POST /guardar — solo falta latitud → 400 con mensaje específico
    // =========================================================
    @Test
    @DisplayName("POST /guardar - solo latitud nula retorna 400 con mensaje de latitud")
    void testGuardarUbicacion_SoloLatitudNula() throws Exception {
        Ubicacion input = new Ubicacion();
        input.setIdReporte(5L);
        // latitud no se setea
        input.setLongitud(-70.0);

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.latitud").value("La latitud es obligatoria"));

        verify(ubicacionService, never()).guardarUbicacion(any());
    }

    // =========================================================
    // POST /guardar — solo falta longitud → 400 con mensaje específico
    // =========================================================
    @Test
    @DisplayName("POST /guardar - solo longitud nula retorna 400 con mensaje de longitud")
    void testGuardarUbicacion_SoloLongitudNula() throws Exception {
        Ubicacion input = new Ubicacion();
        input.setIdReporte(5L);
        input.setLatitud(-33.0);
        // longitud no se setea

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.longitud").value("La longitud es obligatoria"));

        verify(ubicacionService, never()).guardarUbicacion(any());
    }

    // =========================================================
    // POST /guardar — solo falta idReporte → 400 con mensaje específico
    // =========================================================
    @Test
    @DisplayName("POST /guardar - solo idReporte nulo retorna 400 con mensaje de idReporte")
    void testGuardarUbicacion_SoloIdReporteNulo() throws Exception {
        Ubicacion input = new Ubicacion();
        // idReporte no se setea
        input.setLatitud(-33.0);
        input.setLongitud(-70.0);

        mockMvc.perform(post("/api/geografico/guardar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.idReporte").value("El ID del reporte es obligatorio"));

        verify(ubicacionService, never()).guardarUbicacion(any());
    }

    // =========================================================
    // GET /reporte/{id} — ID de reporte = 0 (borde) → delega al service
    // =========================================================
    @Test
    @DisplayName("GET /reporte/0 - delega correctamente al service con idReporte=0")
    void testObtenerPorReporte_IdCero() throws Exception {
        when(ubicacionService.obtenerPorReporte(0L))
                .thenThrow(new EntityNotFoundException(
                        "No se encontró ubicación geográfica para el reporte con ID: 0"));

        mockMvc.perform(get("/api/geografico/reporte/0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("No se encontró ubicación geográfica para el reporte con ID: 0"));
    }
}