package com.gregluna.laboriq.economic.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EconomicIndicatorDto(
        Long id,
        String indicatorName,
        LocalDate indicatorDate,
        BigDecimal value,
        String source
) {
}
