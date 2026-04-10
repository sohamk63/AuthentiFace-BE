package com.alethia.AuthentiFace.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // Face embedding — rarely changes, long TTL
                .withCacheConfiguration(CacheNames.FACE_EMBEDDINGS,
                        defaultConfig.entryTtl(Duration.ofHours(6)))
                // Face enrollment status — rarely changes
                .withCacheConfiguration(CacheNames.FACE_ENROLLMENT_STATUS,
                        defaultConfig.entryTtl(Duration.ofHours(1)))
                // Unread mail count — changes often, short TTL
                .withCacheConfiguration(CacheNames.UNREAD_MAIL_COUNT,
                        defaultConfig.entryTtl(Duration.ofSeconds(30)))
                // User details — moderate TTL
                .withCacheConfiguration(CacheNames.USER_DETAILS,
                        defaultConfig.entryTtl(Duration.ofMinutes(15)))
                .build();
    }
}
