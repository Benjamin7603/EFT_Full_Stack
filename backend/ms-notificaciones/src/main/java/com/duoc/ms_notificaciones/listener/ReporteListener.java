package com.duoc.ms_notificaciones.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ReporteListener {

    @RabbitListener(queues = "notificaciones.queue")
    public void recibirMensaje(String mensajeJson) {
        try {
            System.out.println("🐇 ¡Mensaje asíncrono recibido desde RabbitMQ!");

            // Leemos el JSON dinámicamente sin necesitar una clase DTO
            ObjectMapper mapper = new ObjectMapper();
            JsonNode alerta = mapper.readTree(mensajeJson);

            // Extraemos los valores usando las llaves del JSON
            String mensajeTexto = alerta.get("mensaje").asText();
            String destinatario = alerta.get("destinatario").asText();

            System.out.println("✅ Alerta lista para guardar: " + mensajeTexto);
            System.out.println("✅ Destinatario: " + destinatario);

            // Aquí puedes llamar a tu servicio normal para guardar la notificación
            // notificacionService.guardar(mensajeTexto, destinatario);

        } catch (Exception e) {
            System.err.println("❌ Error procesando el JSON de RabbitMQ: " + e.getMessage());
        }
    }
}