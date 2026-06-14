package com.duoc.ms_reportes.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * Configuración centralizada de Redis Cache para el microservicio de reportes.
 *
 * Cachés utilizados por el servicio:
 * - reportesTodos: almacena el listado histórico completo de reportes.
 * - reportesActivos: almacena reportes en estado NUEVO y EN_PROGRESO.
 *
 * TTL definido:
 * - reportesTodos: 10 minutos.
 * - reportesActivos: 10 minutos.
 *
 * La caché se invalida mediante @CacheEvict cuando se crea un reporte
 * o cuando se modifica su estado/prioridad, evitando datos obsoletos.
 */
@Configuration
public class RedisConfig {

    /**
     * Configura el gestor de caché usando Redis como proveedor.
     *
     * Se aplica un TTL global de 10 minutos para todos los cachés
     * registrados en ms-reportes:
     * - reportesTodos
     * - reportesActivos
     *
     * @param redisConnectionFactory fábrica de conexión administrada por Spring Data Redis.
     * @return CacheManager configurado con Redis y TTL global.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}