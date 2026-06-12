package com.gregluna.laboriq.ingestion.oews;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OewsProperties.class)
public class OewsConfiguration {
}
