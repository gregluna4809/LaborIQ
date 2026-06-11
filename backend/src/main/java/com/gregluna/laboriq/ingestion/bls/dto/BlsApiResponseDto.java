package com.gregluna.laboriq.ingestion.bls.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BlsApiResponseDto(
        String status,
        Integer responseTime,
        List<String> message,

        @JsonProperty("Results")
        BlsResultsDto results
) {
}
