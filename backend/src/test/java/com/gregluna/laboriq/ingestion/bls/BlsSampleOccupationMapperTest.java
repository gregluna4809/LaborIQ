package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDataDto;
import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlsSampleOccupationMapperTest {

    private final BlsSampleOccupationMapper mapper = new BlsSampleOccupationMapper();

    @Test
    void mapsLatestAnnualSeriesToOccupationAndWage() {
        BlsProperties properties = new BlsProperties(
                "https://api.bls.gov/publicAPI/v2",
                "",
                true,
                false,
                java.time.Duration.ofSeconds(10),
                List.of("WMU00140201020000001300002500"),
                "13-0000",
                "Business and Financial Operations Occupations"
        );
        BlsSeriesDto series = new BlsSeriesDto(
                "WMU00140201020000001300002500",
                null,
                List.of(new BlsSeriesDataDto("2023", "A01", "Annual", "true", "36.41", null))
        );

        BlsSampleOccupationMapping mapping = mapper.mapLatestAnnualSeries(series, properties);

        assertThat(mapping.seriesId()).isEqualTo("WMU00140201020000001300002500");
        assertThat(mapping.occupation().getSocCode()).isEqualTo("13-0000");
        assertThat(mapping.occupation().getTitle()).isEqualTo("Business and Financial Operations Occupations");
        assertThat(mapping.occupation().getSourceMetadata())
                .containsEntry("sourceSystem", "BLS")
                .containsEntry("seriesId", "WMU00140201020000001300002500")
                .containsEntry("year", "2023");
        assertThat(mapping.wage().getOccupation()).isSameAs(mapping.occupation());
        assertThat(mapping.wage().getYear()).isEqualTo((short) 2023);
        assertThat(mapping.wage().getMeanWage()).isEqualByComparingTo("36.41");
        assertThat(mapping.wage().getMedianWage()).isNull();
    }
}
