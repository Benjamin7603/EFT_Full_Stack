package com.duoc.ms_reportes.controller;

import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public Reporte crear(
            @Valid @RequestBody ReporteDTO reporteDTO,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        return reporteService.crearReporteProcesado(reporteDTO, rolSesion);
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

    @Operation(summary = "Obtener reporte por ID")
    @GetMapping("/{id}")
    public Reporte obtenerPorId(@PathVariable Long id) {
        return reporteService.obtenerPorId(id);
    }

    @Operation(summary = "Actualizar estado del reporte")
    @PatchMapping("/{id}/estado")
    public Reporte actualizarEstado(
            @PathVariable("id") Long id,
            @RequestParam("nuevoEstado") String nuevoEstado,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        validarRolOperativo(rolSesion);
        return reporteService.actualizarEstado(id, nuevoEstado);
    }

    @Operation(summary = "Actualizar prioridad del reporte")
    @PatchMapping("/{id}/prioridad")
    public Reporte actualizarPrioridad(
            @PathVariable("id") Long id,
            @RequestParam("nuevaPrioridad") String nuevaPrioridad,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        validarRolOperativo(rolSesion);
        return reporteService.actualizarPrioridad(id, nuevaPrioridad);
    }

    private void validarRolOperativo(String rolSesion) {
        if (!esRolOperativo(rolSesion)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para gestionar reportes."
            );
        }
    }

    private boolean esRolOperativo(String rol) {
        if (rol == null || rol.isBlank()) {
            return false;
        }

        String rolNormalizado = rol.trim().toUpperCase();

        return rolNormalizado.equals("ADMIN")
                || rolNormalizado.equals("BOMBERO")
                || rolNormalizado.equals("BRIGADISTA")
                || rolNormalizado.equals("FUNCIONARIO");
    }
}