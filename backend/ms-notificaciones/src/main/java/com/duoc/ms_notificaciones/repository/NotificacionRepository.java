package com.duoc.ms_notificaciones.repository;

import com.duoc.ms_notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatarioOrderByFechaEnvioDesc(String destinatario);
    List<Notificacion> findByDestinatarioAndLeidaFalseOrderByFechaEnvioDesc(String destinatario);
    List<Notificacion> findByDestinatarioAndLeidaFalse(String destinatario);
    long countByDestinatarioAndLeidaFalse(String destinatario);

}