package com.gregluna.laboriq.occupation.dto;

public record RelatedOccupationDto(
        Long id,
        String sourceSocCode,
        String targetSocCode,
        String relationshipType
) {
}
