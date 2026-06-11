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
 * Define un TTL de 10 minutos para evitar datos obsoletos en consultas frecuentes
 * del dashboard y del historial de reportes.
 */
@Configuration
public class RedisConfig {

    /**
     * Configura el gestor de caché usando Redis como proveedor.
     * El TTL definido es de 10 minutos para todos los cachés del microservicio.
     *
     * @param redisConnectionFactory fábrica de conexión administrada por Spring Data Redis.
     * @return CacheManager configurado con TTL global.
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