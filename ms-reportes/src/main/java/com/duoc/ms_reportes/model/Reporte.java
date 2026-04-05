package com.duoc.ms_reportes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    private String urlMedia; // foto o video en la nube

    @NotBlank(message = "El tipo de usuario es obligatorio")
    private String tipoUsuario; // "CIUDADANO" o "OFICIAL"

    private String prioridad;

    private String estado = "NUEVO"; // Estado inicial por defecto

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte = LocalDateTime.now();

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
}