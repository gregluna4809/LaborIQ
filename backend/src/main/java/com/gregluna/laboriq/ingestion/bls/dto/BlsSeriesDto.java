package com.gregluna.laboriq.ingestion.bls.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BlsSeriesDto(
        @JsonProperty("seriesID")
        String seriesId,

        BlsCatalogDto catalog,

        List<BlsSeriesDataDto> data
) {
}
