package com.gregluna.laboriq.ingestion.oews;

import com.gregluna.laboriq.etl.EtlRun;
import com.gregluna.laboriq.etl.EtlRunFactory;
import com.gregluna.laboriq.etl.EtlRunNotFoundException;
import com.gregluna.laboriq.etl.EtlRunRepository;
import com.gregluna.laboriq.etl.EtlRunStatus;
import com.gregluna.laboriq.etl.IngestionConflictException;
import com.gregluna.laboriq.etl.dto.EtlRunDto;
import com.gregluna.laboriq.etl.dto.OewsJobAcceptedDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OewsIngestionService {

    private static final String SOURCE_SYSTEM = "BLS_OEWS";

    private final EtlRunRepository etlRunRepository;
    private final OewsIngestionWorker ingestionWorker;

    // synchronized: prevents a TOCTOU race between the exists-check and the INSERT within
    // a single JVM. The V3 partial unique index (uk_etl_runs_one_running_per_source) is the
    // DB-level hard stop for multi-instance deployments where this synchronized block is
    // ineffective across processes.
    public synchronized OewsJobAcceptedDto trigger() {
        if (etlRunRepository.existsBySourceSystemAndStatus(SOURCE_SYSTEM, EtlRunStatus.RUNNING)) {
            throw new IngestionConflictException(SOURCE_SYSTEM);
        }

        EtlRun run = EtlRunFactory.running(SOURCE_SYSTEM, OffsetDateTime.now());
        etlRunRepository.save(run); // auto-committed: no outer @Transactional

        log.info("OEWS ingestion job created [etlRunId={}], dispatching to background worker", run.getId());
        ingestionWorker.runIngestion(run.getId());

        return new OewsJobAcceptedDto(
                run.getId(),
                run.getSourceSystem(),
                run.getStatus(),
                run.getStartTime(),
                "Ingestion job started"
        );
    }

    public EtlRunDto getStatus(Long id) {
        EtlRun run = etlRunRepository.findById(id)
                .orElseThrow(() -> new EtlRunNotFoundException(id));
        return toDto(run);
    }

    private EtlRunDto toDto(EtlRun run) {
        return new EtlRunDto(
                run.getId(),
                run.getSourceSystem(),
                run.getStartTime(),
                run.getEndTime(),
                run.getStatus(),
                run.getRecordsProcessed(),
                run.getErrorMessage()
        );
    }
}
