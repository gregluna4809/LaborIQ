package com.gregluna.laboriq.occupation.dto;

import java.math.BigDecimal;

public record OccupationSearchResultDto(
        String socCode,
        String title,
        String occGroup,
        BigDecimal latestMedianWage,
        Short latestWageYear,
        Long latestEmploymentCount,
        Short latestEmploymentYear
) {
}
