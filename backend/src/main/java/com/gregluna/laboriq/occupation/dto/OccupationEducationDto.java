package com.gregluna.laboriq.occupation.dto;

import java.math.BigDecimal;

public record OccupationEducationDto(
        Long id,
        String socCode,
        String educationLevel,
        BigDecimal percentage
) {
}
