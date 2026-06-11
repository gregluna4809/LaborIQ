package com.gregluna.laboriq.occupation.dto;

import java.math.BigDecimal;

public record OccupationSkillDto(
        Long id,
        String socCode,
        String skillName,
        BigDecimal importanceScore
) {
}
