package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.etl.EtlRun;
import com.gregluna.laboriq.etl.EtlRunRepository;
import com.gregluna.laboriq.etl.EtlRunStatus;
import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDataDto;
import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesDto;
import com.gregluna.laboriq.occupation.Occupation;
import com.gregluna.laboriq.occupation.OccupationRepository;
import com.gregluna.laboriq.occupation.OccupationWage;
import com.gregluna.laboriq.occupation.OccupationWageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlsSamplePersistenceServiceTest {

    @Mock
    private BlsOccupationDataService occupationDataService;

    @Mock
    private OccupationRepository occupationRepository;

    @Mock
    private OccupationWageRepository occupationWageRepository;

    @Mock
    private EtlRunRepository etlRunRepository;

    private BlsSamplePersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        BlsProperties properties = new BlsProperties(
                "https://api.bls.gov/publicAPI/v2",
                "",
                true,
                false,
                Duration.ofSeconds(10),
                List.of("WMU00140201020000001300002500"),
                "13-0000",
                "Business and Financial Operations Occupations"
        );
        persistenceService = new BlsSamplePersistenceService(
                properties,
                occupationDataService,
                new BlsSampleOccupationMapper(),
                occupationRepository,
                occupationWageRepository,
                etlRunRepository
        );
    }

    @Test
    void persistsOccupationWageAndCompletedEtlRun() {
        when(occupationDataService.retrieveLatestOccupationSeries(List.of("WMU00140201020000001300002500")))
                .thenReturn(List.of(sampleSeries()));
        when(occupationRepository.findBySocCode("13-0000")).thenReturn(Optional.empty());
        when(occupationRepository.save(any(Occupation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(occupationWageRepository.findByOccupationSocCodeAndYear("13-0000", (short) 2023))
                .thenReturn(Optional.empty());
        when(occupationWageRepository.save(any(OccupationWage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        persistenceService.persistLatestSample();

        ArgumentCaptor<Occupation> occupationCaptor = ArgumentCaptor.forClass(Occupation.class);
        verify(occupationRepository).save(occupationCaptor.capture());
        assertThat(occupationCaptor.getValue().getSocCode()).isEqualTo("13-0000");
        assertThat(occupationCaptor.getValue().getSourceMetadata())
                .containsEntry("seriesId", "WMU00140201020000001300002500");

        ArgumentCaptor<OccupationWage> wageCaptor = ArgumentCaptor.forClass(OccupationWage.class);
        verify(occupationWageRepository).save(wageCaptor.capture());
        assertThat(wageCaptor.getValue().getYear()).isEqualTo((short) 2023);
        assertThat(wageCaptor.getValue().getMeanWage()).isEqualByComparingTo("36.41");

        ArgumentCaptor<EtlRun> etlRunCaptor = ArgumentCaptor.forClass(EtlRun.class);
        verify(etlRunRepository, org.mockito.Mockito.times(2)).save(etlRunCaptor.capture());
        EtlRun completedRun = etlRunCaptor.getAllValues().getLast();
        assertThat(completedRun.getStatus()).isEqualTo(EtlRunStatus.COMPLETED);
        assertThat(completedRun.getRecordsProcessed()).isEqualTo(2L);
        assertThat(completedRun.getEndTime()).isNotNull();
    }

    @Test
    void recordsFailedEtlRunWhenRetrievalFails() {
        when(occupationDataService.retrieveLatestOccupationSeries(List.of("WMU00140201020000001300002500")))
                .thenThrow(new BlsApiException("BLS unavailable"));

        assertThatThrownBy(() -> persistenceService.persistLatestSample())
                .isInstanceOf(BlsApiException.class)
                .hasMessage("BLS unavailable");

        ArgumentCaptor<EtlRun> etlRunCaptor = ArgumentCaptor.forClass(EtlRun.class);
        verify(etlRunRepository, org.mockito.Mockito.times(2)).save(etlRunCaptor.capture());
        EtlRun failedRun = etlRunCaptor.getAllValues().getLast();
        assertThat(failedRun.getStatus()).isEqualTo(EtlRunStatus.FAILED);
        assertThat(failedRun.getRecordsProcessed()).isZero();
        assertThat(failedRun.getErrorMessage()).isEqualTo("BLS unavailable");
        assertThat(failedRun.getEndTime()).isNotNull();
    }

    private static BlsSeriesDto sampleSeries() {
        return new BlsSeriesDto(
                "WMU00140201020000001300002500",
                null,
                List.of(new BlsSeriesDataDto("2023", "A01", "Annual", "true", "36.41", null))
        );
    }
}
