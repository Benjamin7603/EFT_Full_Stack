package com.duoc.ms_notificaciones.controller;

import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Endpoints para el envío de alertas y registro de historial")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(summary = "Enviar Alerta", description = "Envía una notificación a los brigadistas y guarda el registro en la base de datos")
    @PostMapping("/enviar")
    public Notificacion enviarAlerta(@Valid @RequestBody Notificacion notificacion) {
        return notificacionService.enviarAlerta(notificacion);
    }

    @Operation(summary = "Obtener historial", description = "Recupera todas las alertas enviadas históricamente")
    @GetMapping
    public List<Notificacion> listarHistorial() {
        return notificacionService.listarHistorial();
    }

    @Operation(summary = "Obtener por ID", description = "Busca una notificación específica")
    @GetMapping("/{id}")
    public Notificacion obtenerPorId(@PathVariable Long id) {
        return notificacionService.obtenerPorId(id);
    }
}