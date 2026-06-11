package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDataDto;
import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "laboriq.bls", name = "sample-runner-enabled", havingValue = "true")
public class BlsSampleDataRunner implements CommandLineRunner {

    private final BlsProperties properties;
    private final BlsOccupationDataService occupationDataService;

    @Override
    public void run(String... args) {
        List<String> sampleSeriesIds = properties.sampleSeriesIds();
        if (sampleSeriesIds == null || sampleSeriesIds.isEmpty()) {
            log.warn("BLS sample runner is enabled, but no sample series IDs are configured");
            return;
        }

        log.info("Starting BLS sample data retrieval for series IDs: {}", sampleSeriesIds);

        try {
            List<BlsSeriesDto> series = occupationDataService.retrieveLatestOccupationSeries(sampleSeriesIds);
            log.info("BLS sample retrieval succeeded with {} series", series.size());

            series.forEach(this::logSeriesSample);
        } catch (BlsApiException ex) {
            log.error("BLS sample retrieval failed: {}", ex.getMessage(), ex);
        }
    }

    private void logSeriesSample(BlsSeriesDto series) {
        List<BlsSeriesDataDto> data = series.data();
        if (data == null || data.isEmpty()) {
            log.warn("BLS series {} returned no data points", series.seriesId());
            return;
        }

        BlsSeriesDataDto latest = data.getFirst();
        log.info(
                "BLS series {} latest data: year={}, period={}, periodName={}, value={}",
                series.seriesId(),
                latest.year(),
                latest.period(),
                latest.periodName(),
                latest.value()
        );
    }
}
