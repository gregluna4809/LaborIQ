package com.gregluna.laboriq.etl;

import com.gregluna.laboriq.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "etl_runs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EtlRun extends BaseEntity {

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EtlRunStatus status;

    @Column(name = "records_processed", nullable = false)
    private Long recordsProcessed = 0L;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
