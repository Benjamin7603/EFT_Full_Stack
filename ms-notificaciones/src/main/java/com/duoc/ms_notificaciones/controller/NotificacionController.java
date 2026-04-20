package com.duoc.ms_notificaciones.controller;

import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.repository.NotificacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Endpoints para el envío de alertas y registro de historial")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Operation(summary = "Enviar Alerta", description = "Envía una notificación a los brigadistas y guarda el registro en la base de datos de Supabase")
    @PostMapping("/enviar")
    public Notificacion enviarAlerta(@Valid @RequestBody Notificacion notificacion) {
        // Log para consola
        System.out.println("🚨 Alerta procesada para: " + notificacion.getDestinatario());

        // Guardar en DB
        return notificacionRepository.save(notificacion);
    }
}