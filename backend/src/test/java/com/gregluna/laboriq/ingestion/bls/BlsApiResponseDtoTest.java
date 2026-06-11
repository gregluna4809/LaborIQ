package com.gregluna.laboriq.ingestion.bls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gregluna.laboriq.ingestion.bls.dto.BlsApiResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlsApiResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesLatestSeriesResponseWithEmptyStringFootnotes() throws Exception {
        String json = """
                {
                  "status": "REQUEST_SUCCEEDED",
                  "responseTime": 123,
                  "message": [],
                  "Results": {
                    "series": [
                      {
                        "seriesID": "WMU00140201020000001300002500",
                        "data": [
                          {
                            "year": "2023",
                            "period": "A01",
                            "periodName": "Annual",
                            "latest": "true",
                            "value": "36.41",
                            "footnotes": ""
                          }
                        ]
                      }
                    ]
                  }
                }
                """;

        BlsApiResponseDto response = objectMapper.readValue(json, BlsApiResponseDto.class);

        assertThat(response.status()).isEqualTo("REQUEST_SUCCEEDED");
        assertThat(response.results().series()).hasSize(1);
        assertThat(response.results().series().getFirst().seriesId())
                .isEqualTo("WMU00140201020000001300002500");
        assertThat(response.results().series().getFirst().data().getFirst().value())
                .isEqualTo("36.41");
    }
}
