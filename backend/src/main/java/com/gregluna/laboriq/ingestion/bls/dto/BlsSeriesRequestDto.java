package com.gregluna.laboriq.ingestion.bls.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BlsSeriesRequestDto(
        List<String> seriesid,
        String startyear,
        String endyear,
        Boolean catalog,
        Boolean calculations,
        Boolean annualaverage,
        Boolean aspects,
        String registrationkey
) {
}
