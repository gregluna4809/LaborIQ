package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.ingestion.bls.dto.BlsApiResponseDto;
import com.gregluna.laboriq.ingestion.bls.dto.BlsSeriesRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlsApiClient {

    private static final String SUCCESS_STATUS = "REQUEST_SUCCEEDED";

    private final WebClient blsWebClient;
    private final BlsProperties properties;

    public BlsApiResponseDto getLatestSeriesData(String seriesId) {
        try {
            BlsApiResponseDto response = blsWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/timeseries/data/{seriesId}")
                            .queryParam("latest", true)
                            .build(seriesId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new BlsApiException(
                                    "BLS API returned HTTP " + clientResponse.statusCode() + ": " + body
                            ))))
                    .bodyToMono(BlsApiResponseDto.class)
                    .timeout(properties.requestTimeout())
                    .block();

            return validateResponse(response, seriesId);
        } catch (WebClientResponseException ex) {
            throw new BlsApiException("BLS API request failed with HTTP " + ex.getStatusCode(), ex);
        } catch (WebClientRequestException ex) {
            throw new BlsApiException("Unable to connect to BLS API", ex);
        } catch (BlsApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BlsApiException("Unexpected error while calling BLS API", ex);
        }
    }

    public BlsApiResponseDto getSeriesData(List<String> seriesIds, String startYear, String endYear) {
        BlsSeriesRequestDto request = new BlsSeriesRequestDto(
                seriesIds,
                startYear,
                endYear,
                hasRegistrationKey(),
                null,
                null,
                null,
                registrationKeyOrNull()
        );

        try {
            BlsApiResponseDto response = blsWebClient
                    .post()
                    .uri("/timeseries/data/")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new BlsApiException(
                                    "BLS API returned HTTP " + clientResponse.statusCode() + ": " + body
                            ))))
                    .bodyToMono(BlsApiResponseDto.class)
                    .timeout(properties.requestTimeout())
                    .block();

            return validateResponse(response, String.join(",", seriesIds));
        } catch (WebClientResponseException ex) {
            throw new BlsApiException("BLS API request failed with HTTP " + ex.getStatusCode(), ex);
        } catch (WebClientRequestException ex) {
            throw new BlsApiException("Unable to connect to BLS API", ex);
        } catch (BlsApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BlsApiException("Unexpected error while calling BLS API", ex);
        }
    }

    private BlsApiResponseDto validateResponse(BlsApiResponseDto response, String seriesContext) {
        if (response == null) {
            throw new BlsApiException("BLS API returned an empty response for series " + seriesContext);
        }

        if (!SUCCESS_STATUS.equals(response.status())) {
            log.warn("BLS API returned unsuccessful status for series {}: status={}, messages={}",
                    seriesContext,
                    response.status(),
                    response.message());
            throw new BlsApiException("BLS API request was not successful: " + response.message());
        }

        if (response.results() == null || response.results().series() == null) {
            throw new BlsApiException("BLS API response did not include series results for " + seriesContext);
        }

        return response;
    }

    private boolean hasRegistrationKey() {
        return properties.registrationKey() != null && !properties.registrationKey().isBlank();
    }

    private String registrationKeyOrNull() {
        return hasRegistrationKey() ? properties.registrationKey() : null;
    }
}
