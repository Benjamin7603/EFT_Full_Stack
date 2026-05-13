package com.duoc.ms_reportes.service;

import com.duoc.ms_reportes.client.GeograficoClient;
import com.duoc.ms_reportes.client.NotificacionClient;
import com.duoc.ms_reportes.dto.NotificacionDTO;
import com.duoc.ms_reportes.dto.ReporteDTO;
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

    // AHORA RECIBE EL DTO
    public Reporte crearReporteProcesado(ReporteDTO datosEntrada) {

        // 1. Mapeamos los datos del DTO a la Entidad
        Reporte nuevoReporte = new Reporte();
        nuevoReporte.setLatitud(datosEntrada.getLatitud());
        nuevoReporte.setLongitud(datosEntrada.getLongitud());
        nuevoReporte.setDescripcion(datosEntrada.getDescripcion());
        nuevoReporte.setUrlMedia(datosEntrada.getUrlMedia());
        nuevoReporte.setTipoUsuario(datosEntrada.getTipoUsuario());
        nuevoReporte.setUsuarioId(datosEntrada.getUsuarioId());

        // Valores por defecto
        nuevoReporte.setEstado("NUEVO");
        nuevoReporte.setPrioridad("ALTA"); // Puedes ajustarlo según la lógica de tu app

        // 2. Guardamos en Base de Datos
        Reporte reporteGuardado = reporteRepository.save(nuevoReporte);

        // 3. Enviamos las coordenadas a ms-geografico de forma asíncrona o síncrona
        try {
            UbicacionDTO ubicacion = new UbicacionDTO(
                    reporteGuardado.getId(),
                    reporteGuardado.getLatitud(),
                    reporteGuardado.getLongitud()
            );
            geograficoClient.guardarUbicacion(ubicacion);
        } catch (Exception e) {
            System.err.println("Atención: ms-geografico no está disponible en este momento.");
        }

        // 4. Enviamos los datos validados a ms-notificaciones de forma directa
        try {
            NotificacionDTO alerta = new NotificacionDTO(
                    "¡NUEVO INCENDIO REPORTADO! ID: " + reporteGuardado.getId() + " - Prioridad: " + reporteGuardado.getPrioridad(),
                    "BRIGADAS_ZONA_SUR"
            );
            notificacionClient.enviarAlerta(alerta);
        } catch (Exception e) {
            System.err.println("Atención: ms-notificaciones no está disponible en este momento.");
        }

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

    public Reporte obtenerPorId(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }
}