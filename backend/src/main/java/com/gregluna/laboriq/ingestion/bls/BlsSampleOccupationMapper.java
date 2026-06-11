package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDataDto;
import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDto;
import com.gregluna.laboriq.occupation.Occupation;
import com.gregluna.laboriq.occupation.OccupationEntityFactory;
import com.gregluna.laboriq.occupation.OccupationWage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class BlsSampleOccupationMapper {

    private static final String SOURCE_SYSTEM = "BLS";

    public BlsSampleOccupationMapping mapLatestAnnualSeries(BlsSeriesDto series, BlsProperties properties) {
        if (series == null || series.seriesId() == null || series.seriesId().isBlank()) {
            throw new BlsApiException("BLS sample series is missing a series ID");
        }

        BlsSeriesDataDto latestAnnualData = latestAnnualData(series);
        Short year = parseYear(latestAnnualData.year(), series.seriesId());
        BigDecimal wageValue = parseWageValue(latestAnnualData.value(), series.seriesId());

        Occupation occupation = new Occupation(properties.sampleSocCode(), properties.sampleOccupationTitle());
        occupation.setDescription("BLS sample occupation loaded from public API series " + series.seriesId());
        occupation.setSourceMetadata(sourceMetadata(series.seriesId(), latestAnnualData));

        OccupationWage wage = OccupationEntityFactory.occupationWage(occupation, year, wageValue);

        return new BlsSampleOccupationMapping(occupation, wage, series.seriesId());
    }

    private BlsSeriesDataDto latestAnnualData(BlsSeriesDto series) {
        if (series.data() == null || series.data().isEmpty()) {
            throw new BlsApiException("BLS series " + series.seriesId() + " returned no data points");
        }

        return series.data().stream()
                .filter(data -> data.period() != null && data.period().startsWith("A"))
                .findFirst()
                .orElseThrow(() -> new BlsApiException(
                        "BLS series " + series.seriesId() + " returned no annual data points"
                ));
    }

    private Short parseYear(String year, String seriesId) {
        try {
            return Short.valueOf(year);
        } catch (NumberFormatException ex) {
            throw new BlsApiException("BLS series " + seriesId + " returned an invalid year: " + year, ex);
        }
    }

    private BigDecimal parseWageValue(String value, String seriesId) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new BlsApiException("BLS series " + seriesId + " returned an invalid wage value: " + value, ex);
        }
    }

    private Map<String, Object> sourceMetadata(String seriesId, BlsSeriesDataDto data) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceSystem", SOURCE_SYSTEM);
        metadata.put("seriesId", seriesId);
        metadata.put("year", data.year());
        metadata.put("period", data.period());
        metadata.put("periodName", data.periodName());
        metadata.put("latest", data.latest());
        metadata.put("value", data.value());
        return metadata;
    }
}
