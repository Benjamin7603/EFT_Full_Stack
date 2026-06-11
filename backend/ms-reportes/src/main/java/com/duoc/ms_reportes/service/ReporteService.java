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

/**
 * Servicio encargado de la gestión y procesamiento de reportes de incendios.
 * Coordina la persistencia de incidentes, el uso de caché Redis y la comunicación
 * inter-servicio con los microservicios geográfico y de notificaciones.
 */
@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final GeograficoClient geograficoClient;
    private final NotificacionClient notificacionClient;

    /**
     * Constructor para la inyección de dependencias requeridas por el servicio de reportes.
     *
     * @param reporteRepository Repositorio para el acceso a datos y persistencia de reportes.
     * @param geograficoClient Cliente de comunicación para el microservicio geográfico.
     * @param notificacionClient Cliente de comunicación para el microservicio de notificaciones.
     */
    public ReporteService(ReporteRepository reporteRepository,
                          GeograficoClient geograficoClient,
                          NotificacionClient notificacionClient) {
        this.reporteRepository = reporteRepository;
        this.geograficoClient = geograficoClient;
        this.notificacionClient = notificacionClient;
    }

    /**
     * Procesa y crea un nuevo reporte de incendio en el sistema.
     * Al crear un nuevo reporte se limpian los cachés de listados, ya que el dashboard
     * y el historial deben reflejar el nuevo incidente.
     *
     * @param datosEntrada Objeto {@link ReporteDTO} con la información del incidente enviado por el usuario.
     * @return Entidad {@link Reporte} guardada en la base de datos con su ID asignado.
     */
    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte crearReporteProcesado(ReporteDTO datosEntrada) {

        Reporte nuevoReporte = new Reporte();
        nuevoReporte.setLatitud(datosEntrada.getLatitud());
        nuevoReporte.setLongitud(datosEntrada.getLongitud());
        nuevoReporte.setDescripcion(datosEntrada.getDescripcion());
        nuevoReporte.setUrlMedia(datosEntrada.getUrlMedia());
        nuevoReporte.setTipoUsuario(datosEntrada.getTipoUsuario());
        nuevoReporte.setUsuarioId(datosEntrada.getUsuarioId());

        String prioridad = datosEntrada.getPrioridad().toUpperCase();

        if (!prioridad.equals("ALTA") && !prioridad.equals("MEDIA") && !prioridad.equals("BAJA")) {
            throw new IllegalArgumentException("La prioridad debe ser ALTA, MEDIA o BAJA");
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
                    "BRIGADAS_ZONA_SUR"
            );
            notificacionClient.enviarAlerta(alerta);
        } catch (Exception e) {
            System.err.println("Atención: ms-notificaciones no está disponible en este momento.");
        }

        return reporteGuardado;
    }

    /**
     * Obtiene el listado de todos los reportes registrados en el sistema.
     * Se cachea en Redis porque es una consulta frecuente desde dashboard, perfil y panel admin.
     *
     * @return Una {@link List} que contiene todos los objetos de {@link Reporte}.
     */
    @Cacheable(value = "reportesTodos", key = "'all'")
    public List<Reporte> listarTodos() {
        return reporteRepository.findAll();
    }

    /**
     * Obtiene los reportes que se encuentran actualmente activos.
     * Se cachea en Redis porque alimenta el dashboard principal y el mapa de incidentes.
     *
     * @return Una {@link List} con los objetos {@link Reporte} en estado NUEVO o EN_PROGRESO.
     */
    @Cacheable(value = "reportesActivos", key = "'active'")
    public List<Reporte> listarActivos() {
        return reporteRepository.findByEstadoIn(List.of("NUEVO", "EN_PROGRESO"));
    }

    /**
     * Actualiza el estado de un reporte específico.
     * Al modificar el estado se limpian los cachés para evitar mostrar reportes obsoletos.
     *
     * @param id Identificador único del reporte a modificar.
     * @param nuevoEstado Nueva etiqueta de estado a asignar.
     * @return Objeto {@link Reporte} modificado con su nuevo estado.
     * @throws EntityNotFoundException Si no se encuentra un reporte asociado al identificador provisto.
     */
    @CacheEvict(value = {"reportesTodos", "reportesActivos"}, allEntries = true)
    public Reporte actualizarEstado(Long id, String nuevoEstado) {
        return reporteRepository.findById(id).map(reporte -> {
            reporte.setEstado(nuevoEstado);
            return reporteRepository.save(reporte);
        }).orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }

    /**
     * Obtiene un reporte en base a su identificador único de registro.
     *
     * @param id Identificador único del reporte a consultar.
     * @return Objeto {@link Reporte} correspondiente al ID suministrado.
     * @throws EntityNotFoundException Si el reporte buscado no existe en los registros.
     */
    public Reporte obtenerPorId(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El reporte con ID " + id + " no existe."));
    }

}