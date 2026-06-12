package com.gregluna.laboriq.ingestion.oews;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laboriq.bls.oews")
public record OewsProperties(
        String datasetUrl,
        int year,
        String localFilePath
) {
    public boolean hasLocalFile() {
        return localFilePath != null && !localFilePath.isBlank();
    }
}
