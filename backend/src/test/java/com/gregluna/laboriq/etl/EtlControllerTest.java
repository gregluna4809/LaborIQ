package com.gregluna.laboriq.etl;

import com.gregluna.laboriq.config.SecurityConfig;
import com.gregluna.laboriq.etl.dto.EtlRunDto;
import com.gregluna.laboriq.etl.dto.OewsJobAcceptedDto;
import com.gregluna.laboriq.ingestion.oews.OewsIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EtlController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "laboriq.admin.api-key=test-key-123")
class EtlControllerTest {

    private static final String VALID_KEY = "test-key-123";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OewsIngestionService ingestionService;

    // --- POST /api/admin/etl/oews ---

    @Test
    void triggerIngestionWithoutApiKeyReturns403() throws Exception {
        mockMvc.perform(post("/api/admin/etl/oews"))
                .andExpect(status().isForbidden());
    }

    @Test
    void triggerIngestionWithWrongApiKeyReturns403() throws Exception {
        mockMvc.perform(post("/api/admin/etl/oews")
                        .header("X-Api-Key", "wrong-key"))
                .andExpect(status().isForbidden());
    }

    @Test
    void triggerIngestionWithValidApiKeyReturns202() throws Exception {
        OewsJobAcceptedDto dto = new OewsJobAcceptedDto(
                42L, "BLS_OEWS", EtlRunStatus.RUNNING,
                OffsetDateTime.now(), "Ingestion job started");
        when(ingestionService.trigger()).thenReturn(dto);

        mockMvc.perform(post("/api/admin/etl/oews")
                        .header("X-Api-Key", VALID_KEY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.etlRunId").value(42))
                .andExpect(jsonPath("$.sourceSystem").value("BLS_OEWS"))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.message").value("Ingestion job started"));
    }

    @Test
    void triggerIngestionWhenAlreadyRunningReturns409() throws Exception {
        when(ingestionService.trigger()).thenThrow(new IngestionConflictException("BLS_OEWS"));

        mockMvc.perform(post("/api/admin/etl/oews")
                        .header("X-Api-Key", VALID_KEY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("An ingestion job for BLS_OEWS is already running"));
    }

    // --- GET /api/admin/etl/runs/{id} ---

    @Test
    void getEtlRunStatusWithoutApiKeyReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/etl/runs/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEtlRunStatusWithWrongApiKeyReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/etl/runs/1")
                        .header("X-Api-Key", "wrong-key"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEtlRunStatusWithValidApiKeyReturns200() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusMinutes(2);
        OffsetDateTime end = OffsetDateTime.now();
        EtlRunDto dto = new EtlRunDto(
                42L, "BLS_OEWS", start, end, EtlRunStatus.COMPLETED, 847L, null);
        when(ingestionService.getStatus(42L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/etl/runs/42")
                        .header("X-Api-Key", VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.sourceSystem").value("BLS_OEWS"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.recordsProcessed").value(847));
    }

    @Test
    void getEtlRunStatusForUnknownIdReturns404() throws Exception {
        when(ingestionService.getStatus(999L)).thenThrow(new EtlRunNotFoundException(999L));

        mockMvc.perform(get("/api/admin/etl/runs/999")
                        .header("X-Api-Key", VALID_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("ETL run not found: 999"));
    }
}
