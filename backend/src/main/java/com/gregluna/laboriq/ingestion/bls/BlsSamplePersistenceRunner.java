package com.gregluna.laboriq.ingestion.bls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "laboriq.bls", name = "persist-sample-enabled", havingValue = "true")
public class BlsSamplePersistenceRunner implements CommandLineRunner {

    private final BlsSamplePersistenceService persistenceService;

    @Override
    public void run(String... args) {
        log.info("Starting opt-in BLS sample persistence");
        persistenceService.persistLatestSample();
    }
}
