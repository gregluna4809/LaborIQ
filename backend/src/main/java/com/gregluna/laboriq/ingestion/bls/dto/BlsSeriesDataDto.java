package com.gregluna.laboriq.ingestion.bls.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record BlsSeriesDataDto(
        String year,
        String period,
        String periodName,
        String latest,
        String value,
        JsonNode footnotes
) {
}
