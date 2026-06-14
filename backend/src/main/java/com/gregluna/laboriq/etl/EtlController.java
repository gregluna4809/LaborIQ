package com.gregluna.laboriq.etl;

import com.gregluna.laboriq.etl.dto.EtlRunDto;
import com.gregluna.laboriq.etl.dto.OewsJobAcceptedDto;
import com.gregluna.laboriq.ingestion.oews.OewsIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/etl")
@RequiredArgsConstructor
public class EtlController {

    private final OewsIngestionService ingestionService;

    @PostMapping("/oews")
    public ResponseEntity<OewsJobAcceptedDto> triggerOewsIngestion() {
        return ResponseEntity.accepted().body(ingestionService.trigger());
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<EtlRunDto> getEtlRunStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ingestionService.getStatus(id));
    }
}
