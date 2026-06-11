package com.duoc.ms_notificaciones.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;

    @NotBlank(message = "El destinatario no puede estar vacío")
    private String destinatario;
    private String tipo;
    private String prioridad;
    private Boolean leida = false;
    private Long reporteId;
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }

        if (leida == null) {
            leida = false;
        }

        if (titulo == null || titulo.isBlank()) {
            titulo = "Alerta GeoFire";
        }

        if (tipo == null || tipo.isBlank()) {
            tipo = "SISTEMA";
        }

        if (prioridad == null || prioridad.isBlank()) {
            prioridad = "MEDIA";
        }
    }
}