package com.duoc.ms_reportes.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReporteDTO {

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;

    @NotNull(message = "La latitud es requerida")
    private Double latitud;

    @NotNull(message = "La longitud es requerida")
    private Double longitud;

    private String urlMedia;

    @NotBlank(message = "El tipo de usuario es requerido")
    private String tipoUsuario;

    @NotNull(message = "El ID de usuario es requerido")
    private Long usuarioId;

    @NotBlank(message = "La prioridad es requerida")
    private String prioridad;
}