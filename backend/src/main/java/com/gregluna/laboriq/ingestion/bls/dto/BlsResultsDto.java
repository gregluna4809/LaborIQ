package com.gregluna.laboriq.ingestion.bls.dto;

import java.util.List;

public record BlsResultsDto(
        List<BlsSeriesDto> series
) {
}
