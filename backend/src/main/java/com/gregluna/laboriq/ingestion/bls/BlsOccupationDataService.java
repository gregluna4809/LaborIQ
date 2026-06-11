package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlsOccupationDataService {

    private final BlsApiClient blsApiClient;

    public List<BlsSeriesDto> retrieveLatestOccupationSeries(List<String> seriesIds) {
        log.info("Retrieving latest BLS occupation data for {} series", seriesIds.size());

        return seriesIds.stream()
                .map(seriesId -> blsApiClient.getLatestSeriesData(seriesId).results().series())
                .flatMap(List::stream)
                .toList();
    }
}
