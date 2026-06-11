package com.duoc.ms_reportes;

import com.duoc.ms_reportes.controller.ReporteController;
import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.service.ReporteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Pruebas Unitarias - ReporteController")
class ReporteControllerTest {

    private MockMvc mockMvc;
    private ReporteService reporteService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reporteService = mock(ReporteService.class);
        ReporteController reporteController = new ReporteController(reporteService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(reporteController).build();
    }

    // =========================================================
    // POST /api/reportes
    // =========================================================
    @Test
    void testCrear_Exitoso() throws Exception {
        ReporteDTO dto = new ReporteDTO();
        dto.setLatitud(-33.45);
        dto.setLongitud(-70.66);
        dto.setDescripcion("Foco detectado");
        dto.setUrlMedia("http://media.com/foto.jpg");
        dto.setTipoUsuario("BRIGADISTA");
        dto.setUsuarioId(1L);
        dto.setPrioridad("MEDIA");

        Reporte reporteSimulado = new Reporte();
        reporteSimulado.setId(1L);
        reporteSimulado.setDescripcion("Foco detectado");

        when(reporteService.crearReporteProcesado(any(ReporteDTO.class))).thenReturn(reporteSimulado);

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descripcion").value("Foco detectado"));
    }

    // =========================================================
    // GET /api/reportes
    // =========================================================
    @Test
    void testListar_Exitoso() throws Exception {
        Reporte r1 = new Reporte();
        r1.setId(1L);

        when(reporteService.listarTodos()).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // =========================================================
    // GET /api/reportes/activos
    // =========================================================
    @Test
    void testObtenerActivos_Exitoso() throws Exception {
        Reporte activo = new Reporte();
        activo.setId(10L);
        activo.setEstado("NUEVO");

        when(reporteService.listarActivos()).thenReturn(List.of(activo));

        mockMvc.perform(get("/api/reportes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    // =========================================================
    // PATCH /api/reportes/{id}/estado
    // =========================================================
    @Test
    void testActualizarEstado_Exitoso() throws Exception {
        Reporte reporteActualizado = new Reporte();
        reporteActualizado.setId(1L);
        reporteActualizado.setEstado("EN_PROGRESO");

        when(reporteService.actualizarEstado(eq(1L), eq("EN_PROGRESO"))).thenReturn(reporteActualizado);

        mockMvc.perform(patch("/api/reportes/1/estado")
                        .param("nuevoEstado", "EN_PROGRESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }
}