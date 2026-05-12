package com.duoc.ms_reportes.dto;

public class UbicacionDTO {

    private Long idReporte;
    private Double latitud;
    private Double longitud;

    // Constructor vacío (necesario para Spring)
    public UbicacionDTO() {}

    // Constructor con parámetros
    public UbicacionDTO(Long idReporte, Double latitud, Double longitud) {
        this.idReporte = idReporte;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Getters y Setters
    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}