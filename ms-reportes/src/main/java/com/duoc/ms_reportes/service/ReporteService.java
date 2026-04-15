package com.duoc.ms_reportes.service;

import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.repository.ReporteRepository;
import jakarta.persistence.EntityNotFoundException; // <-- Importación agregada para el manejo de errores
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;

    public ReporteService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public Reporte crearReporteProcesado(Reporte datosEntrada) {
        Reporte nuevoReporte = new Reporte();
        nuevoReporte.setLatitud(datosEntrada.getLatitud());
        nuevoReporte.setLongitud(datosEntrada.getLongitud());
        nuevoReporte.setDescripcion(datosEntrada.getDescripcion());
        nuevoReporte.setUrlMedia(datosEntrada.getUrlMedia());
        nuevoReporte.setUsuarioId(datosEntrada.getUsuarioId());
        nuevoReporte.setTipoUsuario(datosEntrada.getTipoUsuario());
        nuevoReporte.setEstado("NUEVO"); // Nos aseguramos que nazca como NUEVO

        if ("OFICIAL".equalsIgnoreCase(datosEntrada.getTipoUsuario())) {
            nuevoReporte.setPrioridad("ALTA");
        } else {
            nuevoReporte.setPrioridad("MEDIA");
        }

        return reporteRepository.save(nuevoReporte);
    }

    public List<Reporte> listarTodos() {
        return reporteRepository.findAll();
    }

    public List<Reporte> listarActivos() {
        // Filtramos para mostrar en el mapa SOLO los que están ocurriendo ahora
        return reporteRepository.findByEstadoIn(List.of("NUEVO", "EN_PROGRESO"));
    }

    public Reporte actualizarEstado(Long id, String nuevoEstado) {
        return reporteRepository.findById(id).map(reporte -> {
            reporte.setEstado(nuevoEstado);
            return reporteRepository.save(reporte);
        }).orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe.")); // <-- Cambio aplicado aquí
    }
}