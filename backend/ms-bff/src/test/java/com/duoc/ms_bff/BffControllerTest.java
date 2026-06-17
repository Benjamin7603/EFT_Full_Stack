package com.duoc.ms_bff.controller;

import com.duoc.ms_bff.client.GeograficoClient;
import com.duoc.ms_bff.client.ReportesClient;
import com.duoc.ms_bff.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - BffController")
class BffControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReportesClient reportesClient;

    @Mock
    private GeograficoClient geograficoClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        BffController bffController = new BffController(reportesClient, geograficoClient);

        mockMvc = MockMvcBuilders
                .standaloneSetup(bffController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("GET /bff/estado - retorna mensaje de estado")
    void testEstado() throws Exception {
        mockMvc.perform(get("/bff/estado")
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BFF funcionando correctamente")));
    }

    @Test
    @DisplayName("GET /bff/reportes - retorna lista de reportes")
    void testObtenerReportes() throws Exception {
        List<Map<String, Object>> mockReportes = List.of(
                Map.of("id", 1, "descripcion", "Incendio forestal activo")
        );

        when(reportesClient.obtenerReportes()).thenReturn(mockReportes);

        mockMvc.perform(get("/bff/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].descripcion").value("Incendio forestal activo"));

        verify(reportesClient, times(1)).obtenerReportes();
        verifyNoInteractions(geograficoClient);
    }

    @Test
    @DisplayName("GET /bff/geografico/reporte/{id} - retorna ubicación por reporte")
    void testObtenerUbicacionPorReporte() throws Exception {
        Map<String, Object> mockUbicacion = Map.of(
                "latitud", -33.456,
                "longitud", -70.648
        );

        when(geograficoClient.obtenerUbicacionPorReporte(10L)).thenReturn(mockUbicacion);

        mockMvc.perform(get("/bff/geografico/reporte/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(geograficoClient, times(1)).obtenerUbicacionPorReporte(10L);
        verifyNoInteractions(reportesClient);
    }

    @Test
    @DisplayName("POST /bff/reportar-incendio - crea reporte exitosamente")
    void testReportarIncendio() throws Exception {
        Map<String, Object> nuevoReporte = Map.of(
                "severidad", "ALTA",
                "descripcion", "Foco norte"
        );

        Map<String, Object> respuesta = Map.of(
                "id", 99,
                "severidad", "ALTA"
        );

        when(reportesClient.crearReporte(any())).thenReturn(respuesta);

        mockMvc.perform(post("/bff/reportar-incendio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoReporte)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.severidad").value("ALTA"));

        verify(reportesClient, times(1)).crearReporte(any());
        verifyNoInteractions(geograficoClient);
    }

    @Test
    @DisplayName("GET /bff/incendio/{id} - retorna reporte y ubicación combinados")
    void testObtenerIncendioCompleto() throws Exception {
        Map<String, Object> mockReporte = Map.of(
                "id", 5,
                "severidad", "ALTA"
        );

        Map<String, Object> mockUbicacion = Map.of(
                "latitud", -36.826,
                "longitud", -73.049
        );

        when(reportesClient.obtenerReportePorId(5L)).thenReturn(mockReporte);
        when(geograficoClient.obtenerUbicacionPorReporte(5L)).thenReturn(mockUbicacion);

        mockMvc.perform(get("/bff/incendio/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reporte.id").value(5))
                .andExpect(jsonPath("$.reporte.severidad").value("ALTA"))
                .andExpect(jsonPath("$.ubicacion.latitud").value(-36.826))
                .andExpect(jsonPath("$.ubicacion.longitud").value(-73.049));

        verify(reportesClient, times(1)).obtenerReportePorId(5L);
        verify(geograficoClient, times(1)).obtenerUbicacionPorReporte(5L);
    }

    @Test
    @DisplayName("GET /bff/incendio/{id} - retorna alerta cuando geográfico falla")
    void testObtenerIncendioCompletoConFalloGeografico() throws Exception {
        Map<String, Object> mockReporte = Map.of(
                "id", 7,
                "severidad", "MEDIA"
        );

        when(reportesClient.obtenerReportePorId(7L)).thenReturn(mockReporte);
        when(geograficoClient.obtenerUbicacionPorReporte(7L))
                .thenThrow(new RuntimeException("MS-Geografico no disponible"));

        mockMvc.perform(get("/bff/incendio/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reporte.id").value(7))
                .andExpect(jsonPath("$.reporte.severidad").value("MEDIA"))
                .andExpect(jsonPath("$.ubicacion.alerta")
                        .value("Ubicaci\u00f3n temporalmente no disponible"));

        verify(reportesClient, times(1)).obtenerReportePorId(7L);
        verify(geograficoClient, times(1)).obtenerUbicacionPorReporte(7L);
    }
}