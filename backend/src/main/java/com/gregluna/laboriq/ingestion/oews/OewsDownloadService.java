package com.gregluna.laboriq.ingestion.oews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class OewsDownloadService {

    private final RestClient restClient;

    public OewsDownloadService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public byte[] download(String url) {
        log.info("Downloading OEWS dataset from {}", url);
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(byte[].class);
    }
}
