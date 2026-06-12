package com.gregluna.laboriq.ingestion.oews;

import java.math.BigDecimal;

record OewsRow(
        String occCode,
        String occTitle,
        String occGroup,
        Long totEmp,
        BigDecimal aMean,
        BigDecimal aMedian,
        BigDecimal aPct10,
        BigDecimal aPct25,
        BigDecimal aPct75,
        BigDecimal aPct90
) {
}
