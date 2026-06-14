package com.duoc.ms_reportes.service;

import com.duoc.ms_reportes.client.GeograficoClient;
import com.duoc.ms_reportes.client.NotificacionClient;
import com.duoc.ms_reportes.dto.NotificacionDTO;
import com.duoc.ms_reportes.dto.ReporteDTO;
import com.duoc.ms_reportes.dto.UbicacionDTO;
import com.duoc.ms_reportes.model.Reporte;
import com.duoc.ms_reportes.repository.ReporteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final GeograficoClient geograficoClient;
    private final NotificacionClient notificacionClient;

    public ReporteService(ReporteRepository reporteRepository,
                          GeograficoClient geograficoClient,
                          NotificacionClient notificacionClient) {
        this.reporteRepository = reporteRepository;
        this.geograficoClient = geograficoClient;
        this.notificacionClient = notificacionClient;
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte crearReporteProcesado(ReporteDTO datosEntrada) {
        return crearReporteProcesado(datosEntrada, null);
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte crearReporteProcesado(ReporteDTO datosEntrada, String rolSesion) {
        Reporte nuevoReporte = new Reporte();

        nuevoReporte.setLatitud(datosEntrada.getLatitud());
        nuevoReporte.setLongitud(datosEntrada.getLongitud());
        nuevoReporte.setDescripcion(datosEntrada.getDescripcion());
        nuevoReporte.setUrlMedia(datosEntrada.getUrlMedia());
        nuevoReporte.setTipoUsuario(datosEntrada.getTipoUsuario());
        nuevoReporte.setUsuarioId(datosEntrada.getUsuarioId());

        String prioridad;

        if (esRolOperativo(rolSesion)) {
            prioridad = normalizarPrioridad(datosEntrada.getPrioridad());
        } else {
            prioridad = "BAJA";
        }

        nuevoReporte.setEstado("NUEVO");
        nuevoReporte.setPrioridad(prioridad);

        Reporte reporteGuardado = reporteRepository.save(nuevoReporte);

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

        try {
            NotificacionDTO alerta = new NotificacionDTO(
                    "¡NUEVO INCENDIO REPORTADO! ID: " + reporteGuardado.getId() + " - Prioridad: " + reporteGuardado.getPrioridad(),
                    "ADMIN"
            );
            notificacionClient.enviarAlerta(alerta);
        } catch (Exception e) {
            System.err.println("Atención: ms-notificaciones no está disponible en este momento.");
        }

        return reporteGuardado;
    }

    @Cacheable(value = "reportesTodos", key = "'all'")
    public List<Reporte> listarTodos() {
        return reporteRepository.findAll();
    }

    @Cacheable(value = "reportesActivos", key = "'active'")
    public List<Reporte> listarActivos() {
        return reporteRepository.findByEstadoIn(List.of("NUEVO", "EN_PROGRESO"));
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte actualizarEstado(Long id, String nuevoEstado) {
        String estadoNormalizado = normalizarEstado(nuevoEstado);

        return reporteRepository.findById(id).map(reporte -> {
            reporte.setEstado(estadoNormalizado);
            return reporteRepository.save(reporte);
        }).orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }

    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte actualizarPrioridad(Long id, String nuevaPrioridad) {
        String prioridadNormalizada = normalizarPrioridad(nuevaPrioridad);

        return reporteRepository.findById(id).map(reporte -> {
            reporte.setPrioridad(prioridadNormalizada);
            return reporteRepository.save(reporte);
        }).orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }

    public Reporte obtenerPorId(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
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

    private String normalizarPrioridad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) {
            throw new IllegalArgumentException("La prioridad es obligatoria para usuarios operativos.");
        }

        String prioridadNormalizada = prioridad.trim().toUpperCase();

        if (
                !prioridadNormalizada.equals("ALTA") &&
                        !prioridadNormalizada.equals("MEDIA") &&
                        !prioridadNormalizada.equals("BAJA")
        ) {
            throw new IllegalArgumentException("La prioridad debe ser ALTA, MEDIA o BAJA");
        }

        return prioridadNormalizada;
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado del reporte es obligatorio.");
        }

        String estadoNormalizado = estado.trim().toUpperCase();

        if (
                !estadoNormalizado.equals("NUEVO") &&
                        !estadoNormalizado.equals("EN_PROGRESO") &&
                        !estadoNormalizado.equals("RESUELTO")
        ) {
            throw new IllegalArgumentException("El estado debe ser NUEVO, EN_PROGRESO o RESUELTO.");
        }

        return estadoNormalizado;
    }
}