package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.etl.EtlRun;
import com.gregluna.laboriq.etl.EtlRunFactory;
import com.gregluna.laboriq.etl.EtlRunRepository;
import com.gregluna.laboriq.etl.EtlRunStatus;
import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDto;
import com.gregluna.laboriq.occupation.Occupation;
import com.gregluna.laboriq.occupation.OccupationRepository;
import com.gregluna.laboriq.occupation.OccupationWage;
import com.gregluna.laboriq.occupation.OccupationWageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlsSamplePersistenceService {

    private static final String SOURCE_SYSTEM = "BLS_SAMPLE";

    private final BlsProperties properties;
    private final BlsOccupationDataService occupationDataService;
    private final BlsSampleOccupationMapper mapper;
    private final OccupationRepository occupationRepository;
    private final OccupationWageRepository occupationWageRepository;
    private final EtlRunRepository etlRunRepository;

    public void persistLatestSample() {
        EtlRun etlRun = EtlRunFactory.running(SOURCE_SYSTEM, OffsetDateTime.now());
        etlRunRepository.save(etlRun);

        try {
            String seriesId = sampleSeriesId();
            List<BlsSeriesDto> series = occupationDataService.retrieveLatestOccupationSeries(List.of(seriesId));
            if (series.isEmpty()) {
                throw new BlsApiException("BLS sample retrieval returned no series for " + seriesId);
            }

            BlsSampleOccupationMapping mapping = mapper.mapLatestAnnualSeries(series.getFirst(), properties);
            Occupation occupation = saveOccupation(mapping.occupation());
            saveWage(occupation, mapping.wage());

            markCompleted(etlRun, 2L);
            log.info("Persisted BLS sample series {} for occupation {}", mapping.seriesId(), occupation.getSocCode());
        } catch (RuntimeException ex) {
            markFailed(etlRun, ex);
            throw ex;
        }
    }

    private String sampleSeriesId() {
        if (properties.sampleSeriesIds() == null || properties.sampleSeriesIds().isEmpty()) {
            throw new BlsApiException("No BLS sample series IDs are configured");
        }
        return properties.sampleSeriesIds().getFirst();
    }

    private Occupation saveOccupation(Occupation mappedOccupation) {
        Occupation occupation = occupationRepository.findBySocCode(mappedOccupation.getSocCode())
                .orElse(mappedOccupation);

        occupation.setTitle(mappedOccupation.getTitle());
        occupation.setDescription(mappedOccupation.getDescription());
        occupation.setSourceMetadata(mappedOccupation.getSourceMetadata());

        return occupationRepository.save(occupation);
    }

    private void saveWage(Occupation occupation, OccupationWage mappedWage) {
        OccupationWage wage = occupationWageRepository
                .findByOccupationSocCodeAndYear(occupation.getSocCode(), mappedWage.getYear())
                .orElse(mappedWage);

        wage.setOccupation(occupation);
        wage.setMedianWage(mappedWage.getMedianWage());
        wage.setMeanWage(mappedWage.getMeanWage());
        occupationWageRepository.save(wage);
    }

    private void markCompleted(EtlRun etlRun, long recordsProcessed) {
        etlRun.setEndTime(OffsetDateTime.now());
        etlRun.setStatus(EtlRunStatus.COMPLETED);
        etlRun.setRecordsProcessed(recordsProcessed);
        etlRunRepository.save(etlRun);
    }

    private void markFailed(EtlRun etlRun, RuntimeException ex) {
        etlRun.setEndTime(OffsetDateTime.now());
        etlRun.setStatus(EtlRunStatus.FAILED);
        etlRun.setRecordsProcessed(0L);
        etlRun.setErrorMessage(ex.getMessage());
        etlRunRepository.save(etlRun);
        log.error("BLS sample persistence failed: {}", ex.getMessage(), ex);
    }
}
