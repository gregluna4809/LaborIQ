package com.gregluna.laboriq.ingestion.bls;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "laboriq.bls")
public record BlsProperties(
        String baseUrl,
        String registrationKey,
        boolean sampleRunnerEnabled,
        boolean persistSampleEnabled,
        Duration requestTimeout,
        List<String> sampleSeriesIds,
        String sampleSocCode,
        String sampleOccupationTitle
) {
}
