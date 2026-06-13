package com.duoc.ms_reportes.controller;

import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de exponer las operaciones de gestión de reportes de incidentes.
 * Permite la recepción, consulta histórica, filtrado en tiempo real de focos activos
 * y la actualización de los estados operativos de cada reporte.
 *
 * @author Carlos Moil
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Gestión de avisos de incendio y ubicación")
public class ReporteController {

    private final ReporteService reporteService;

    /**
     * Constructor para la inyección del servicio requerido por el controlador.
     *
     * @param reporteService Servicio de lógica de negocio que orquesta el procesamiento de reportes.
     */
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * Endpoint para el ingreso y procesamiento de un nuevo reporte de incidente.
     *
     * @param reporteDTO Objeto con los datos de entrada del reporte.
     * @return Reporte persistido.
     */
    @Operation(summary = "Enviar un nuevo reporte")
    @PostMapping
    public Reporte crear(@Valid @RequestBody ReporteDTO reporteDTO) {
        return reporteService.crearReporteProcesado(reporteDTO);
    }

    /**
     * Endpoint para recuperar el historial completo de reportes registrados.
     *
     * @return Lista de reportes.
     */
    @Operation(summary = "Listar reportes históricos")
    @GetMapping
    public List<Reporte> listar() {
        return reporteService.listarTodos();
    }

    /**
     * Endpoint para obtener reportes activos.
     *
     * @return Lista de reportes en estado NUEVO o EN_PROGRESO.
     */
    @Operation(summary = "Obtener reportes activos")
    @GetMapping("/activos")
    public List<Reporte> obtenerActivos() {
        return reporteService.listarActivos();
    }

    /**
     * Endpoint para obtener un reporte por ID.
     *
     * @param id Identificador del reporte.
     * @return Reporte encontrado.
     */
    @Operation(summary = "Obtener reporte por ID")
    @GetMapping("/{id}")
    public Reporte obtenerPorId(@PathVariable Long id) {
        return reporteService.obtenerPorId(id);
    }

    /**
     * Endpoint para modificar el estado operativo de un reporte.
     * Solo ADMIN puede cambiar estados.
     *
     * @param id Identificador del reporte.
     * @param nuevoEstado Nuevo estado del reporte.
     * @param rolSesion Rol recibido desde el Gateway.
     * @return Reporte actualizado.
     */
    @Operation(summary = "Actualizar estado del reporte")
    @PatchMapping("/{id}/estado")
    public Reporte actualizarEstado(
            @PathVariable("id") Long id,
            @RequestParam("nuevoEstado") String nuevoEstado,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSesion
    ) {
        if (rolSesion == null || !"ADMIN".equalsIgnoreCase(rolSesion)) {
            throw new IllegalArgumentException("Solo un administrador puede cambiar el estado de un reporte.");
        }

        return reporteService.actualizarEstado(id, nuevoEstado);
    }
}