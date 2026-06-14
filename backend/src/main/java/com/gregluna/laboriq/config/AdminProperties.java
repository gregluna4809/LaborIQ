package com.gregluna.laboriq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laboriq.admin")
public record AdminProperties(String apiKey) {
}
