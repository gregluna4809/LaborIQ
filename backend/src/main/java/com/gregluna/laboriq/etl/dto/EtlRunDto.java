package com.gregluna.laboriq.etl.dto;

import com.gregluna.laboriq.etl.EtlRunStatus;

import java.time.OffsetDateTime;

public record EtlRunDto(
        Long id,
        String sourceSystem,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        EtlRunStatus status,
        Long recordsProcessed,
        String errorMessage
) {
}
