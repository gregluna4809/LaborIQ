package com.gregluna.laboriq.etl;

import java.time.OffsetDateTime;

public final class EtlRunFactory {

    private EtlRunFactory() {
    }

    public static EtlRun running(String sourceSystem, OffsetDateTime startTime) {
        EtlRun etlRun = new EtlRun();
        etlRun.setSourceSystem(sourceSystem);
        etlRun.setStartTime(startTime);
        etlRun.setStatus(EtlRunStatus.RUNNING);
        etlRun.setRecordsProcessed(0L);
        return etlRun;
    }
}
