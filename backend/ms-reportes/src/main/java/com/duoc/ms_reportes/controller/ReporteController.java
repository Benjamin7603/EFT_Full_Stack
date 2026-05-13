package com.duoc.ms_reportes.controller;

import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Gestión de avisos de incendio y ubicación")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @Operation(summary = "Enviar un nuevo reporte")
    @PostMapping
    public Reporte crear(@Valid @RequestBody ReporteDTO reporteDTO) { // AHORA RECIBE EL DTO
        return reporteService.crearReporteProcesado(reporteDTO);
    }

    @Operation(summary = "Listar reportes históricos")
    @GetMapping
    public List<Reporte> listar() {
        return reporteService.listarTodos();
    }

    @Operation(summary = "Obtener reportes activos")
    @GetMapping("/activos")
    public List<Reporte> obtenerActivos() {
        return reporteService.listarActivos();
    }

    @Operation(summary = "Actualizar estado del reporte")
    @PatchMapping("/{id}/estado")
    public Reporte actualizarEstado(
            @PathVariable("id") Long id,
            @RequestParam("nuevoEstado") String nuevoEstado) {
        return reporteService.actualizarEstado(id, nuevoEstado);
    }
}