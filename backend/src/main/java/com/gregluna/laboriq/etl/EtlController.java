package com.gregluna.laboriq.etl;

import com.gregluna.laboriq.etl.dto.EtlRunDto;
import com.gregluna.laboriq.ingestion.oews.OewsIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/etl")
@RequiredArgsConstructor
public class EtlController {

    private final OewsIngestionService ingestionService;

    @PostMapping("/oews")
    public ResponseEntity<EtlRunDto> triggerOewsIngestion() {
        return ResponseEntity.ok(ingestionService.ingest());
    }
}
