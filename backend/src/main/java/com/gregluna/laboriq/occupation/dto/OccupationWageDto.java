package com.gregluna.laboriq.occupation.dto;

import java.math.BigDecimal;

public record OccupationWageDto(
        Long id,
        String socCode,
        Short year,
        BigDecimal medianWage,
        BigDecimal meanWage,
        BigDecimal p10Wage,
        BigDecimal p25Wage,
        BigDecimal p75Wage,
        BigDecimal p90Wage
) {
}
