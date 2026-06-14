package com.gregluna.laboriq.etl.dto;

import com.gregluna.laboriq.etl.EtlRunStatus;

import java.time.OffsetDateTime;

public record OewsJobAcceptedDto(
        Long etlRunId,
        String sourceSystem,
        EtlRunStatus status,
        OffsetDateTime startedAt,
        String message
) {
}
