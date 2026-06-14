package com.gregluna.laboriq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// Enables Spring's @Async proxy infrastructure.
// Uses the default SimpleAsyncTaskExecutor (one thread per task).
// A production deployment should replace this with a bounded ThreadPoolTaskExecutor
// to prevent unbounded thread creation under load.
@Configuration
@EnableAsync
public class AsyncConfig {
}
