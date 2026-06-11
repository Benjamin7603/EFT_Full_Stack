package com.duoc.ms_notificaciones.controller;

import com.duoc.ms_notificaciones.model.Notificacion;
import com.duoc.ms_notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de notificaciones del sistema GeoFire.
 * Permite registrar alertas, consultar historial, filtrar por destinatario,
 * revisar notificaciones no leídas y actualizar su estado de lectura.
 */
@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Gestión de alertas, historial y estado de lectura")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Registra una nueva notificación.
     * Se mantiene la ruta /enviar para compatibilidad con ms-reportes.
     *
     * @param notificacion datos de la alerta.
     * @return notificación creada.
     */
    @Operation(summary = "Enviar alerta", description = "Registra una nueva notificación en el sistema")
    @PostMapping("/enviar")
    public Notificacion enviarAlerta(@Valid @RequestBody Notificacion notificacion) {
        return notificacionService.enviarAlerta(notificacion);
    }

    /**
     * Alias REST para crear una notificación.
     *
     * @param notificacion datos de la alerta.
     * @return notificación creada.
     */
    @Operation(summary = "Crear notificación", description = "Crea una nueva notificación")
    @PostMapping
    public Notificacion crear(@Valid @RequestBody Notificacion notificacion) {
        return notificacionService.enviarAlerta(notificacion);
    }

    /**
     * Lista todo el historial de notificaciones.
     *
     * @return listado completo de notificaciones.
     */
    @Operation(summary = "Obtener historial", description = "Recupera todas las alertas enviadas históricamente")
    @GetMapping
    public List<Notificacion> listarHistorial() {
        return notificacionService.listarHistorial();
    }

    /**
     * Obtiene una notificación por ID.
     *
     * @param id identificador de la notificación.
     * @return notificación encontrada.
     */
    @Operation(summary = "Obtener por ID", description = "Busca una notificación específica")
    @GetMapping("/{id}")
    public Notificacion obtenerPorId(@PathVariable Long id) {
        return notificacionService.obtenerPorId(id);
    }

    /**
     * Lista notificaciones por destinatario.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return notificaciones asociadas.
     */
    @Operation(summary = "Listar por destinatario", description = "Obtiene las notificaciones de un destinatario")
    @GetMapping("/destinatario/{destinatario}")
    public List<Notificacion> listarPorDestinatario(@PathVariable String destinatario) {
        return notificacionService.listarPorDestinatario(destinatario);
    }

    /**
     * Lista notificaciones no leídas por destinatario.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return notificaciones no leídas.
     */
    @Operation(summary = "Listar no leídas", description = "Obtiene las notificaciones no leídas de un destinatario")
    @GetMapping("/destinatario/{destinatario}/no-leidas")
    public List<Notificacion> listarNoLeidas(@PathVariable String destinatario) {
        return notificacionService.listarNoLeidas(destinatario);
    }

    /**
     * Cuenta notificaciones no leídas por destinatario.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return cantidad de no leídas.
     */
    @Operation(summary = "Contador de no leídas", description = "Cuenta las notificaciones no leídas de un destinatario")
    @GetMapping("/destinatario/{destinatario}/contador")
    public Map<String, Long> contarNoLeidas(@PathVariable String destinatario) {
        return Map.of("noLeidas", notificacionService.contarNoLeidas(destinatario));
    }

    /**
     * Marca una notificación como leída.
     *
     * @param id identificador de la notificación.
     * @return notificación actualizada.
     */
    @Operation(summary = "Marcar como leída", description = "Actualiza una notificación individual como leída")
    @PatchMapping("/{id}/leer")
    public Notificacion marcarComoLeida(@PathVariable Long id) {
        return notificacionService.marcarComoLeida(id);
    }

    /**
     * Marca todas las notificaciones no leídas de un destinatario como leídas.
     *
     * @param destinatario rol, grupo o usuario destinatario.
     * @return notificaciones actualizadas.
     */
    @Operation(summary = "Marcar todas como leídas", description = "Marca todas las notificaciones de un destinatario como leídas")
    @PatchMapping("/destinatario/{destinatario}/leer-todas")
    public List<Notificacion> marcarTodasComoLeidas(@PathVariable String destinatario) {
        return notificacionService.marcarTodasComoLeidas(destinatario);
    }

    /**
     * Elimina una notificación por ID.
     *
     * @param id identificador de la notificación.
     */
    @Operation(summary = "Eliminar notificación", description = "Elimina una notificación del historial")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
    }
}