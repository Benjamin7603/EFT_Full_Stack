package com.duoc.ms_reportes;

import com.duoc.ms_reportes.client.GeograficoClient;
import com.duoc.ms_reportes.client.NotificacionClient;
import com.duoc.ms_reportes.dto.NotificacionDTO;
import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.dto.UbicacionDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.repository.ReporteRepository;
import com.duoc.ms_reportes.service.ReporteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas Unitarias - ReporteService")
class ReporteServiceTest {

    private ReporteRepository reporteRepository;
    private GeograficoClient geograficoClient;
    private NotificacionClient notificacionClient;
    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteRepository = mock(ReporteRepository.class);
        geograficoClient = mock(GeograficoClient.class);
        notificacionClient = mock(NotificacionClient.class);
        reporteService = new ReporteService(reporteRepository, geograficoClient, notificacionClient);
    }

    // =========================================================
    // crearReporteProcesado
    // =========================================================
    @Test
    void testCrearReporteProcesado_Exitoso() {
        ReporteDTO dto = new ReporteDTO();
        dto.setLatitud(-33.45);
        dto.setLongitud(-70.66);
        dto.setDescripcion("Incendio");

        Reporte reporteSimulado = new Reporte();
        reporteSimulado.setId(100L);
        reporteSimulado.setEstado("NUEVO");

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);

        Reporte resultado = reporteService.crearReporteProcesado(dto);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    @Test
    void testCrearReporteProcesado_FallaMsGeografico() {
        ReporteDTO dto = new ReporteDTO();
        Reporte reporteSimulado = new Reporte();
        reporteSimulado.setId(100L);

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);
        doThrow(new RuntimeException("Error Geográfico")).when(geograficoClient).guardarUbicacion(any(UbicacionDTO.class));

        Reporte resultado = reporteService.crearReporteProcesado(dto);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    @Test
    void testCrearReporteProcesado_FallaMsNotificaciones() {
        ReporteDTO dto = new ReporteDTO();
        Reporte reporteSimulado = new Reporte();
        reporteSimulado.setId(100L);

        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteSimulado);
        doThrow(new RuntimeException("Error Notificación")).when(notificacionClient).enviarAlerta(any(NotificacionDTO.class));

        Reporte resultado = reporteService.crearReporteProcesado(dto);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    // =========================================================
    // Listados
    // =========================================================
    @Test
    void testListarTodos() {
        when(reporteRepository.findAll()).thenReturn(List.of(new Reporte()));

        List<Reporte> lista = reporteService.listarTodos();

        assertEquals(1, lista.size());
    }

    @Test
    void testListarActivos() {
        when(reporteRepository.findByEstadoIn(anyList())).thenReturn(List.of(new Reporte()));

        List<Reporte> lista = reporteService.listarActivos();

        assertEquals(1, lista.size());
    }

    // =========================================================
    // obtenerPorId
    // =========================================================
    @Test
    void testObtenerPorId_Exitoso() {
        Reporte reporte = new Reporte();
        reporte.setId(5L);
        when(reporteRepository.findById(5L)).thenReturn(Optional.of(reporte));

        Reporte resultado = reporteService.obtenerPorId(5L);

        assertNotNull(resultado);
        assertEquals(5L, resultado.getId());
    }

    @Test
    void testObtenerPorId_NoEncontrado() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reporteService.obtenerPorId(99L);
        });
    }

    // =========================================================
    // actualizarEstado
    // =========================================================
    @Test
    void testActualizarEstado_Exitoso() {
        Reporte reporteOriginal = new Reporte();
        reporteOriginal.setId(1L);
        reporteOriginal.setEstado("NUEVO");

        Reporte reporteModificado = new Reporte();
        reporteModificado.setId(1L);
        reporteModificado.setEstado("EN_PROGRESO");

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporteOriginal));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteModificado);

        Reporte resultado = reporteService.actualizarEstado(1L, "EN_PROGRESO");

        assertNotNull(resultado);
        assertEquals("EN_PROGRESO", resultado.getEstado());
    }

    @Test
    void testActualizarEstado_NoEncontrado() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reporteService.actualizarEstado(1L, "FINALIZADO");
        });
    }
}