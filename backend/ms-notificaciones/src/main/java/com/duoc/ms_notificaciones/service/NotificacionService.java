package com.duoc.ms_notificaciones.service;

import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.repository.NotificacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public Notificacion enviarAlerta(Notificacion notificacion) {
        System.out.println("Alerta procesada para: " + notificacion.getDestinatario());
        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> listarHistorial() {
        return notificacionRepository.findAll();
    }

    public Notificacion obtenerPorId(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("La notificación con ID " + id + " no existe."));
    }
}