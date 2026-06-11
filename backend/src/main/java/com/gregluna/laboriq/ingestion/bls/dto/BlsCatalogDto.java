package com.gregluna.laboriq.ingestion.bls.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BlsCatalogDto(
        @JsonProperty("series_title")
        String seriesTitle,

        @JsonProperty("series_id")
        String seriesId,

        String seasonality,

        @JsonProperty("survey_name")
        String surveyName,

        @JsonProperty("survey_abbreviation")
        String surveyAbbreviation,

        @JsonProperty("measure_data_type")
        String measureDataType,

        @JsonProperty("commerce_industry")
        String commerceIndustry,

        String occupation,

        @JsonProperty("occupation_work_class")
        String occupationWorkClass,

        String area
) {
}
