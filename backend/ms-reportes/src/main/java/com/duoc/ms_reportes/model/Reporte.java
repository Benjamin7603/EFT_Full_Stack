package com.duoc.ms_reportes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reporte implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    private String urlMedia;

    @NotBlank(message = "El tipo de usuario es obligatorio")
    private String tipoUsuario;

    private String prioridad;

    private String estado = "NUEVO";

    @Column(name = "fecha_reporte", nullable = false, updatable = false)
    private LocalDateTime fechaReporte;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @PrePersist
    public void prePersist() {
        if (this.fechaReporte == null) {
            this.fechaReporte = LocalDateTime.now(ZoneOffset.UTC);
        }

        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "NUEVO";
        }
    }
}