package com.duoc.ms_reportes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reportes {
    @Id
    @GeneratedValue
    private Long id;
    private Double latitud;
    private Double longitud;
    private String descripcion;
    private String urlMedia;

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte = LocalDateTime.now();
    private Long usuarioId;


}
