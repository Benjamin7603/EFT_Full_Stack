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
     * @param reporteService Servicio de lógica de negocio que orquesta el procesamiento de reportes.
     */
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }
    /**
     * Endpoint para el ingreso y procesamiento de un nuevo reporte de incidente.
     * Recibe los datos validados del usuario y delega la lógica de creación e integración.
     * @param reporteDTO Objeto {@link ReporteDTO} con los datos de entrada del aviso (coordenadas, descripción).
     * Es validado formalmente mediante la anotación {@link Valid}.
     * @return El objeto {@link Reporte} ya persistido en el sistema con sus propiedades asignadas.
     */
    @Operation(summary = "Enviar un nuevo reporte")
    @PostMapping
    public Reporte crear(@Valid @RequestBody ReporteDTO reporteDTO) { // AHORA RECIBE EL DTO
        return reporteService.crearReporteProcesado(reporteDTO);
    }
    /**
     * Endpoint para recuperar el historial completo de reportes registrados en la base de datos.
     * @return Una {@link List} que contiene todas las entidades {@link Reporte} almacenadas.
     */
    @Operation(summary = "Listar reportes históricos")
    @GetMapping
    public List<Reporte> listar() {
        return reporteService.listarTodos();
    }
    /**
     * Endpoint para obtener de forma exclusiva los reportes de incidentes que se encuentran activos.
     * Utilizado comúnmente para la representación gráfica o mapeo de emergencias en curso.
     * @return Una {@link List} con los objetos {@link Reporte} en estado NUEVO o EN_PROGRESO.
     */
    @Operation(summary = "Obtener reportes activos")
    @GetMapping("/activos")
    public List<Reporte> obtenerActivos() {
        return reporteService.listarActivos();
    }
    /**
     * Endpoint para modificar parcialmente el estado operativo de un reporte específico.
     * Utiliza el mapeo por parche (Patch) para actualizar únicamente el atributo requerido.
     * @param id Identificador único del reporte que se desea actualizar, extraído de la URL.
     * @param nuevoEstado Cadena de texto enviada por parámetro que define la nueva situación del incidente.
     * @return La entidad {@link Reporte} modificada con el nuevo estado almacenado en la base de datos.
     */
    @Operation(summary = "Actualizar estado del reporte")
    @PatchMapping("/{id}/estado")
    public Reporte actualizarEstado(
            @PathVariable("id") Long id,
            @RequestParam("nuevoEstado") String nuevoEstado) {
        return reporteService.actualizarEstado(id, nuevoEstado);
    }
}