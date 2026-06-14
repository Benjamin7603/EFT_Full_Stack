package com.duoc.ms_reportes;

import com.duoc.ms_reportes.controller.ReporteController;
import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.exception.GlobalExceptionHandler;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

        mockMvc = MockMvcBuilders
                .standaloneSetup(reporteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================
    // POST /api/reportes
    // =========================================================
    @Test
    void testCrear_Exitoso_RolOperativo() throws Exception {
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
        reporteSimulado.setPrioridad("MEDIA");

        when(reporteService.crearReporteProcesado(any(ReporteDTO.class), eq("BRIGADISTA")))
                .thenReturn(reporteSimulado);

        mockMvc.perform(post("/api/reportes")
                        .header("X-Usuario-Rol", "BRIGADISTA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descripcion").value("Foco detectado"))
                .andExpect(jsonPath("$.prioridad").value("MEDIA"));

        verify(reporteService, times(1))
                .crearReporteProcesado(any(ReporteDTO.class), eq("BRIGADISTA"));
    }

    @Test
    void testCrear_Exitoso_UserSinPrioridad() throws Exception {
        ReporteDTO dto = new ReporteDTO();
        dto.setLatitud(-33.45);
        dto.setLongitud(-70.66);
        dto.setDescripcion("Humo cerca del cerro");
        dto.setUrlMedia("");
        dto.setTipoUsuario("CIUDADANO");
        dto.setUsuarioId(2L);
        dto.setPrioridad(null);

        Reporte reporteSimulado = new Reporte();
        reporteSimulado.setId(2L);
        reporteSimulado.setDescripcion("Humo cerca del cerro");
        reporteSimulado.setPrioridad("BAJA");

        when(reporteService.crearReporteProcesado(any(ReporteDTO.class), eq("USER")))
                .thenReturn(reporteSimulado);

        mockMvc.perform(post("/api/reportes")
                        .header("X-Usuario-Rol", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.prioridad").value("BAJA"));

        verify(reporteService, times(1))
                .crearReporteProcesado(any(ReporteDTO.class), eq("USER"));
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

        verify(reporteService, times(1)).listarTodos();
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

        verify(reporteService, times(1)).listarActivos();
    }

    // =========================================================
    // GET /api/reportes/{id}
    // =========================================================
    @Test
    void testObtenerPorId_Exitoso() throws Exception {
        Reporte reporte = new Reporte();
        reporte.setId(5L);
        reporte.setDescripcion("Incendio puntual");

        when(reporteService.obtenerPorId(5L)).thenReturn(reporte);

        mockMvc.perform(get("/api/reportes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.descripcion").value("Incendio puntual"));

        verify(reporteService, times(1)).obtenerPorId(5L);
    }

    // =========================================================
    // PATCH /api/reportes/{id}/estado
    // =========================================================
    @Test
    void testActualizarEstado_Exitoso_Admin() throws Exception {
        Reporte reporteActualizado = new Reporte();
        reporteActualizado.setId(1L);
        reporteActualizado.setEstado("EN_PROGRESO");

        when(reporteService.actualizarEstado(eq(1L), eq("EN_PROGRESO")))
                .thenReturn(reporteActualizado);

        mockMvc.perform(patch("/api/reportes/1/estado")
                        .header("X-Usuario-Rol", "ADMIN")
                        .param("nuevoEstado", "EN_PROGRESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));

        verify(reporteService, times(1))
                .actualizarEstado(1L, "EN_PROGRESO");
    }

    @Test
    void testActualizarEstado_Exitoso_Brigadista() throws Exception {
        Reporte reporteActualizado = new Reporte();
        reporteActualizado.setId(1L);
        reporteActualizado.setEstado("RESUELTO");

        when(reporteService.actualizarEstado(eq(1L), eq("RESUELTO")))
                .thenReturn(reporteActualizado);

        mockMvc.perform(patch("/api/reportes/1/estado")
                        .header("X-Usuario-Rol", "BRIGADISTA")
                        .param("nuevoEstado", "RESUELTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));

        verify(reporteService, times(1))
                .actualizarEstado(1L, "RESUELTO");
    }

    @Test
    void testActualizarEstado_UserRetornaForbidden() throws Exception {
        mockMvc.perform(patch("/api/reportes/1/estado")
                        .header("X-Usuario-Rol", "USER")
                        .param("nuevoEstado", "EN_PROGRESO"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para gestionar reportes."));

        verify(reporteService, never()).actualizarEstado(anyLong(), anyString());
    }

    // =========================================================
    // PATCH /api/reportes/{id}/prioridad
    // =========================================================
    @Test
    void testActualizarPrioridad_Exitoso_Funcionario() throws Exception {
        Reporte reporteActualizado = new Reporte();
        reporteActualizado.setId(1L);
        reporteActualizado.setPrioridad("ALTA");

        when(reporteService.actualizarPrioridad(eq(1L), eq("ALTA")))
                .thenReturn(reporteActualizado);

        mockMvc.perform(patch("/api/reportes/1/prioridad")
                        .header("X-Usuario-Rol", "FUNCIONARIO")
                        .param("nuevaPrioridad", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridad").value("ALTA"));

        verify(reporteService, times(1))
                .actualizarPrioridad(1L, "ALTA");
    }

    @Test
    void testActualizarPrioridad_UserRetornaForbidden() throws Exception {
        mockMvc.perform(patch("/api/reportes/1/prioridad")
                        .header("X-Usuario-Rol", "USER")
                        .param("nuevaPrioridad", "ALTA"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para gestionar reportes."));

        verify(reporteService, never()).actualizarPrioridad(anyLong(), anyString());
    }
}