package com.duoc.ms_bff;

import com.duoc.ms_bff.client.GeograficoClient;
import com.duoc.ms_bff.client.ReportesClient;
import com.duoc.ms_bff.controller.BffController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pruebas Unitarias - BffController")
class BffControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportesClient reportesClient;

    @Mock
    private GeograficoClient geograficoClient;

    @InjectMocks
    private BffController bffController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bffController).build();
    }

    // =========================================================
    // GET /bff/estado
    // =========================================================
    @Test
    void testEstado() throws Exception {
        mockMvc.perform(get("/bff/estado")
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BFF funcionando correctamente")));
    }

    // =========================================================
    // GET /bff/reportes
    // =========================================================
    @Test
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
    }

    // =========================================================
    // GET /bff/geografico/reporte/{idReporte}
    // =========================================================
    @Test
    void testObtenerUbicacionPorReporte() throws Exception {
        Map<String, Object> mockUbicacion = Map.of("latitud", -33.456, "longitud", -70.648);

        when(geograficoClient.obtenerUbicacionPorReporte(10L)).thenReturn(mockUbicacion);

        mockMvc.perform(get("/bff/geografico/reporte/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitud").value(-33.456))
                .andExpect(jsonPath("$.longitud").value(-70.648));

        verify(geograficoClient, times(1)).obtenerUbicacionPorReporte(10L);
    }

    // =========================================================
    // GET /bff/incendio/{id}
    // =========================================================
    @Test
    void testObtenerIncendioCompleto() throws Exception {
        Map<String, Object> mockReporte = Map.of("id", 5, "severidad", "ALTA");
        Map<String, Object> mockUbicacion = Map.of("latitud", -36.826, "longitud", -73.049);

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
}