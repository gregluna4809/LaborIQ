package com.gregluna.laboriq.occupation.dto;

import java.util.Map;

public record OccupationDto(
        Long id,
        String socCode,
        String title,
        String description,
        Map<String, Object> sourceMetadata
) {
}
