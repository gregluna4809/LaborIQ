package com.gregluna.laboriq.ingestion.oews;

import com.gregluna.laboriq.etl.EtlRun;
import com.gregluna.laboriq.etl.EtlRunRepository;
import com.gregluna.laboriq.etl.EtlRunStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OewsIngestionWorker {

    private final OewsProperties properties;
    private final OewsDownloadService downloadService;
    private final OewsExcelParserService parserService;
    private final OewsPersistenceService persistenceService;
    private final EtlRunRepository etlRunRepository;

    // Not @Transactional: persistenceService.upsertAll() manages its own transaction boundary.
    // markCompleted/markFailed each auto-commit via individual repository.save() calls.
    // Making this @Transactional would put the whole operation in one tx, causing a
    // rollback-only state if upsertAll() throws, which would prevent markFailed() from persisting.
    @Async
    public void runIngestion(Long etlRunId) {
        EtlRun run = etlRunRepository.findById(etlRunId)
                .orElseThrow(() -> new IllegalStateException("EtlRun " + etlRunId + " not found"));

        String source = properties.hasLocalFile()
                ? "local:" + properties.localFilePath()
                : properties.datasetUrl();
        log.info("Starting async OEWS ingestion [etlRunId={}, source={}] for year {}",
                etlRunId, source, properties.year());

        try {
            byte[] data = properties.hasLocalFile()
                    ? readLocal(properties.localFilePath())
                    : downloadService.download(properties.datasetUrl());
            List<OewsRow> rows = parserService.parse(data);
            int count = persistenceService.upsertAll(rows, properties.year());
            markCompleted(run, count);
            log.info("OEWS ingestion complete [etlRunId={}]: {} records processed", etlRunId, count);
        } catch (Exception ex) {
            markFailed(run, ex);
        }
    }

    private byte[] readLocal(String path) {
        try {
            log.info("Reading OEWS dataset from local file: {}", path);
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            throw new OewsIngestionException("Cannot read local OEWS file: " + path, e);
        }
    }

    private void markCompleted(EtlRun run, long recordsProcessed) {
        run.setEndTime(OffsetDateTime.now());
        run.setStatus(EtlRunStatus.COMPLETED);
        run.setRecordsProcessed(recordsProcessed);
        etlRunRepository.save(run);
    }

    private void markFailed(EtlRun run, Exception ex) {
        run.setEndTime(OffsetDateTime.now());
        run.setStatus(EtlRunStatus.FAILED);
        run.setRecordsProcessed(0L);
        run.setErrorMessage(ex.getMessage());
        etlRunRepository.save(run);
        log.error("OEWS ingestion failed [etlRunId={}]: {}", run.getId(), ex.getMessage(), ex);
    }
}
