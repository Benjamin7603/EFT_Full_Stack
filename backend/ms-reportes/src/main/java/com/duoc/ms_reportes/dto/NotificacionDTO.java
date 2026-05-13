package com.duoc.ms_reportes.dto;

public class NotificacionDTO {

    private String mensaje;
    private String destinatario;

    // Constructor vacío
    public NotificacionDTO() {}

    // Constructor con parámetros
    public NotificacionDTO(String mensaje, String destinatario) {
        this.mensaje = mensaje;
        this.destinatario = destinatario;
    }

    // Getters y Setters
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
}