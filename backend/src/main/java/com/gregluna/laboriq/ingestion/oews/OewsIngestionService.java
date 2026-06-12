package com.gregluna.laboriq.ingestion.oews;

import com.gregluna.laboriq.etl.EtlRun;
import com.gregluna.laboriq.etl.EtlRunFactory;
import com.gregluna.laboriq.etl.EtlRunRepository;
import com.gregluna.laboriq.etl.EtlRunStatus;
import com.gregluna.laboriq.etl.dto.EtlRunDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OewsIngestionService {

    private static final String SOURCE_SYSTEM = "BLS_OEWS";

    private final OewsProperties properties;
    private final OewsDownloadService downloadService;
    private final OewsExcelParserService parserService;
    private final OewsPersistenceService persistenceService;
    private final EtlRunRepository etlRunRepository;

    public EtlRunDto ingest() {
        EtlRun run = EtlRunFactory.running(SOURCE_SYSTEM, OffsetDateTime.now());
        etlRunRepository.save(run);

        String source = properties.hasLocalFile() ? "local:" + properties.localFilePath() : properties.datasetUrl();
        log.info("Starting OEWS ingestion [source={}] for year {}", source, properties.year());

        try {
            byte[] data = properties.hasLocalFile()
                    ? readLocal(properties.localFilePath())
                    : downloadService.download(properties.datasetUrl());
            List<OewsRow> rows = parserService.parse(data);
            int count = persistenceService.upsertAll(rows, properties.year());
            markCompleted(run, count);
            log.info("OEWS ingestion complete: {} records processed", count);
        } catch (Exception ex) {
            markFailed(run, ex);
        }

        return toDto(run);
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
        log.error("OEWS ingestion failed: {}", ex.getMessage(), ex);
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
