package com.duoc.ms_notificaciones.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue notificacionesQueue() {
        // Crea una cola duradera llamada "notificaciones.queue"
        return new Queue("notificaciones.queue", true);
    }
}