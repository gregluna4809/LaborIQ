package com.gregluna.laboriq.occupation.dto;

public record OccupationEmploymentDto(
        Long id,
        String socCode,
        Short year,
        Long employmentCount
) {
}
