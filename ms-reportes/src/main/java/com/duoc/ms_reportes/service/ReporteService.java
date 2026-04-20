package com.duoc.ms_reportes.service;

import com.duoc.ms_reportes.client.GeograficoClient;
import com.duoc.ms_reportes.client.NotificacionClient;
import com.duoc.ms_reportes.dto.NotificacionDTO;
import com.duoc.ms_reportes.dto.UbicacionDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.repository.ReporteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final GeograficoClient geograficoClient;
    private final NotificacionClient notificacionClient;

    // Inyección de dependencias por constructor
    public ReporteService(ReporteRepository reporteRepository,
                          GeograficoClient geograficoClient,
                          NotificacionClient notificacionClient) {
        this.reporteRepository = reporteRepository;
        this.geograficoClient = geograficoClient;
        this.notificacionClient = notificacionClient;
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

        // 1. Guardamos el reporte en la BD
        Reporte reporteGuardado = reporteRepository.save(nuevoReporte);

        // 2. Enviamos los datos validados a ms-geografico de forma directa
        UbicacionDTO ubicacion = new UbicacionDTO(
                reporteGuardado.getId(),
                reporteGuardado.getLatitud(),
                reporteGuardado.getLongitud()
        );
        geograficoClient.guardarUbicacion(ubicacion);

        // 3. Enviamos los datos validados a ms-notificaciones de forma directa
        NotificacionDTO alerta = new NotificacionDTO(
                "¡NUEVO INCENDIO REPORTADO! ID: " + reporteGuardado.getId() + " - Prioridad: " + reporteGuardado.getPrioridad(),
                "BRIGADAS_ZONA_SUR"
        );
        notificacionClient.enviarAlerta(alerta);

        return reporteGuardado;
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
        }).orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }
}