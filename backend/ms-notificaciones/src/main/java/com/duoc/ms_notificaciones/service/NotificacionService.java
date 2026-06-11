package com.duoc.ms_notificaciones.service;

import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.repository.NotificacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio encargado de gestionar las notificaciones del sistema GeoFire.
 * Permite registrar alertas, consultar historial, filtrar por destinatario,
 * obtener notificaciones no leídas y actualizar su estado de lectura.
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Registra una nueva notificación en el sistema.
     * Si no se envían campos opcionales, se asignan valores por defecto.
     *
     * @param notificacion datos de la alerta a registrar.
     * @return notificación persistida.
     */
    public Notificacion enviarAlerta(Notificacion notificacion) {
        normalizarNotificacion(notificacion);

        System.out.println("Alerta procesada para: " + notificacion.getDestinatario());

        return notificacionRepository.save(notificacion);
    }

    /**
     * Obtiene todas las notificaciones históricas.
     *
     * @return listado completo de notificaciones.
     */
    public List<Notificacion> listarHistorial() {
        return notificacionRepository.findAll();
    }

    /**
     * Busca una notificación específica por ID.
     *
     * @param id identificador de la notificación.
     * @return notificación encontrada.
     */
    public Notificacion obtenerPorId(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("La notificación con ID " + id + " no existe."));
    }

    /**
     * Lista las notificaciones asociadas a un destinatario.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return notificaciones del destinatario ordenadas por fecha descendente.
     */
    public List<Notificacion> listarPorDestinatario(String destinatario) {
        validarDestinatario(destinatario);
        return notificacionRepository.findByDestinatarioOrderByFechaEnvioDesc(destinatario);
    }

    /**
     * Lista las notificaciones no leídas de un destinatario.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return notificaciones no leídas.
     */
    public List<Notificacion> listarNoLeidas(String destinatario) {
        validarDestinatario(destinatario);
        return notificacionRepository.findByDestinatarioAndLeidaFalseOrderByFechaEnvioDesc(destinatario);
    }

    /**
     * Cuenta las notificaciones no leídas de un destinatario.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return cantidad de notificaciones pendientes.
     */
    public long contarNoLeidas(String destinatario) {
        validarDestinatario(destinatario);
        return notificacionRepository.countByDestinatarioAndLeidaFalse(destinatario);
    }

    /**
     * Marca una notificación individual como leída.
     *
     * @param id identificador de la notificación.
     * @return notificación actualizada.
     */
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = obtenerPorId(id);
        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }

    /**
     * Marca todas las notificaciones no leídas de un destinatario como leídas.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return listado de notificaciones actualizadas.
     */
    public List<Notificacion> marcarTodasComoLeidas(String destinatario) {
        validarDestinatario(destinatario);

        List<Notificacion> noLeidas =
                notificacionRepository.findByDestinatarioAndLeidaFalse(destinatario);

        noLeidas.forEach(notificacion -> notificacion.setLeida(true));

        return notificacionRepository.saveAll(noLeidas);
    }

    /**
     * Elimina una notificación por ID.
     *
     * @param id identificador de la notificación.
     */
    public void eliminar(Long id) {
        Notificacion notificacion = obtenerPorId(id);
        notificacionRepository.delete(notificacion);
    }

    private void normalizarNotificacion(Notificacion notificacion) {
        if (notificacion.getTitulo() == null || notificacion.getTitulo().isBlank()) {
            notificacion.setTitulo("Alerta GeoFire");
        } else {
            notificacion.setTitulo(notificacion.getTitulo().trim());
        }

        if (notificacion.getTipo() == null || notificacion.getTipo().isBlank()) {
            notificacion.setTipo("SISTEMA");
        } else {
            notificacion.setTipo(notificacion.getTipo().trim().toUpperCase());
        }

        if (notificacion.getPrioridad() == null || notificacion.getPrioridad().isBlank()) {
            notificacion.setPrioridad("MEDIA");
        } else {
            notificacion.setPrioridad(notificacion.getPrioridad().trim().toUpperCase());
        }

        if (notificacion.getLeida() == null) {
            notificacion.setLeida(false);
        }

        if (notificacion.getMensaje() != null) {
            notificacion.setMensaje(notificacion.getMensaje().trim());
        }

        if (notificacion.getDestinatario() != null) {
            notificacion.setDestinatario(notificacion.getDestinatario().trim());
        }
    }

    private void validarDestinatario(String destinatario) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("El destinatario es obligatorio.");
        }
    }
}