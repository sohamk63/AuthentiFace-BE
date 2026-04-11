package com.alethia.AuthentiFace.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ASYNC / MULTITHREADING configuration.
 * 
 * Enables @Async across the application and provides named thread pools:
 * 
 * 1. "eventExecutor" — Used by domain event listeners (Kafka forwarding).
 *    These are fire-and-forget side effects that should NOT block the main request thread.
 * 
 * 2. "faceExecutor" — Used by face enrollment to parallelize embedding generation
 *    and image storage, which are independent I/O operations.
 * 
 * WHY custom pools instead of Spring's default SimpleAsyncTaskExecutor?
 * - SimpleAsyncTaskExecutor creates a NEW thread per task (no pooling = resource leak under load)
 * - ThreadPoolTaskExecutor reuses threads, has bounded queue, and configurable rejection policy
 * - Named pools make debugging easier (thread names show up in logs)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool for domain event listeners (forwarding events to Kafka).
     * Core=2, Max=5: events are lightweight, don't need many threads.
     * Queue=100: buffer bursts without rejecting.
     */
    @Bean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool for face-related I/O operations (embedding HTTP call, disk storage).
     * Core=3, Max=6: face operations involve network + disk I/O, benefit from parallelism.
     * Queue=50: face operations are less frequent but heavier.
     */
    @Bean(name = "faceExecutor")
    public Executor faceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("face-io-");
        executor.initialize();
        return executor;
    }
}
