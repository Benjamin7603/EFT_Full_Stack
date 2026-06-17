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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    @DisplayName("POST /api/reportes - crea reporte con rol operativo")
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
    @DisplayName("POST /api/reportes - crea reporte con rol USER sin prioridad")
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

    @Test
    @DisplayName("GET /api/reportes - lista reportes")
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

    @Test
    @DisplayName("GET /api/reportes/activos - lista reportes activos")
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

    @Test
    @DisplayName("GET /api/reportes/{id} - obtiene reporte por ID")
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

    @Test
    @DisplayName("PATCH /api/reportes/{id}/estado - actualiza estado con ADMIN")
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
    @DisplayName("PATCH /api/reportes/{id}/estado - actualiza estado con BOMBERO")
    void testActualizarEstado_Exitoso_Bombero() throws Exception {
        Reporte reporteActualizado = new Reporte();
        reporteActualizado.setId(2L);
        reporteActualizado.setEstado("EN_PROGRESO");

        when(reporteService.actualizarEstado(eq(2L), eq("EN_PROGRESO")))
                .thenReturn(reporteActualizado);

        mockMvc.perform(patch("/api/reportes/2/estado")
                        .header("X-Usuario-Rol", "BOMBERO")
                        .param("nuevoEstado", "EN_PROGRESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));

        verify(reporteService, times(1))
                .actualizarEstado(2L, "EN_PROGRESO");
    }

    @Test
    @DisplayName("PATCH /api/reportes/{id}/estado - actualiza estado con BRIGADISTA")
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
    @DisplayName("PATCH /api/reportes/{id}/estado - USER retorna forbidden")
    void testActualizarEstado_UserRetornaForbidden() throws Exception {
        mockMvc.perform(patch("/api/reportes/1/estado")
                        .header("X-Usuario-Rol", "USER")
                        .param("nuevoEstado", "EN_PROGRESO"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para gestionar reportes."));

        verify(reporteService, never()).actualizarEstado(anyLong(), anyString());
    }

    @Test
    @DisplayName("PATCH /api/reportes/{id}/estado - rol vacío retorna forbidden")
    void testActualizarEstado_RolVacio() throws Exception {
        mockMvc.perform(patch("/api/reportes/1/estado")
                        .header("X-Usuario-Rol", "   ")
                        .param("nuevoEstado", "EN_PROGRESO"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para gestionar reportes."));

        verify(reporteService, never()).actualizarEstado(anyLong(), anyString());
    }

    @Test
    @DisplayName("PATCH /api/reportes/{id}/prioridad - actualiza prioridad con FUNCIONARIO")
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
    @DisplayName("PATCH /api/reportes/{id}/prioridad - USER retorna forbidden")
    void testActualizarPrioridad_UserRetornaForbidden() throws Exception {
        mockMvc.perform(patch("/api/reportes/1/prioridad")
                        .header("X-Usuario-Rol", "USER")
                        .param("nuevaPrioridad", "ALTA"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para gestionar reportes."));

        verify(reporteService, never()).actualizarPrioridad(anyLong(), anyString());
    }

    @Test
    @DisplayName("PATCH /api/reportes/{id}/prioridad - sin rol retorna forbidden")
    void testActualizarPrioridad_SinRolRetornaForbidden() throws Exception {
        mockMvc.perform(patch("/api/reportes/1/prioridad")
                        .param("nuevaPrioridad", "ALTA"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para gestionar reportes."));

        verify(reporteService, never()).actualizarPrioridad(anyLong(), anyString());
    }

    @Test
    @DisplayName("GET /api/reportes/auditoria/excel - descarga Excel con ADMIN")
    void testDescargarExcelAuditoria_ExitoAdmin() throws Exception {
        byte[] mockExcel = "excel-dummy-data".getBytes();

        when(reporteService.generarExcelAuditoriaReportes()).thenReturn(mockExcel);

        mockMvc.perform(get("/api/reportes/auditoria/excel")
                        .header("X-Usuario-Rol", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        matchesPattern("attachment; filename=auditoria_reportes_geofire_\\d{8}_\\d{6}\\.xlsx")
                ))
                .andExpect(content().contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                ))
                .andExpect(content().bytes(mockExcel));

        verify(reporteService, times(1)).generarExcelAuditoriaReportes();
    }

    @Test
    @DisplayName("GET /api/reportes/auditoria/excel - descarga Excel con FUNCIONARIO")
    void testDescargarExcelAuditoria_ExitoFuncionario() throws Exception {
        byte[] mockExcel = "excel-funcionario".getBytes();

        when(reporteService.generarExcelAuditoriaReportes()).thenReturn(mockExcel);

        mockMvc.perform(get("/api/reportes/auditoria/excel")
                        .header("X-Usuario-Rol", "FUNCIONARIO"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        matchesPattern("attachment; filename=auditoria_reportes_geofire_\\d{8}_\\d{6}\\.xlsx")
                ))
                .andExpect(content().bytes(mockExcel));

        verify(reporteService, times(1)).generarExcelAuditoriaReportes();
    }

    @Test
    @DisplayName("GET /api/reportes/auditoria/excel - USER retorna forbidden")
    void testDescargarExcelAuditoria_UserForbidden() throws Exception {
        mockMvc.perform(get("/api/reportes/auditoria/excel")
                        .header("X-Usuario-Rol", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para descargar auditoría de reportes."));

        verify(reporteService, never()).generarExcelAuditoriaReportes();
    }

    @Test
    @DisplayName("GET /api/reportes/auditoria/excel - sin rol o rol vacío retorna forbidden")
    void testDescargarExcelAuditoria_RolNuloOVacio() throws Exception {
        mockMvc.perform(get("/api/reportes/auditoria/excel"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para descargar auditoría de reportes."));

        mockMvc.perform(get("/api/reportes/auditoria/excel")
                        .header("X-Usuario-Rol", "   "))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes permisos para descargar auditoría de reportes."));

        verify(reporteService, never()).generarExcelAuditoriaReportes();
    }
}