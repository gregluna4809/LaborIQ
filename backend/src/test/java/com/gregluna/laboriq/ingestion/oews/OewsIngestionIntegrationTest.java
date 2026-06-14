package com.gregluna.laboriq.ingestion.oews;

import com.gregluna.laboriq.etl.EtlRunRepository;
import com.gregluna.laboriq.etl.EtlRunStatus;
import com.gregluna.laboriq.etl.dto.EtlRunDto;
import com.gregluna.laboriq.etl.dto.OewsJobAcceptedDto;
import com.gregluna.laboriq.occupation.OccupationEmploymentRepository;
import com.gregluna.laboriq.occupation.OccupationRepository;
import com.gregluna.laboriq.occupation.OccupationWageRepository;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// TODO: Testcontainers cannot currently discover Docker on this machine.
// docker-java 3.3.6 (bundled with Testcontainers 1.19.8) returns HTTP 400 from the Docker Desktop
// WSL2 named-pipe proxy (docker_engine, dockerDesktopLinuxEngine). Root cause: Docker Desktop's
// proxy requires versioned API paths (/v1.x/...) but docker-java sends unversioned requests.
// Fix options: (1) enable "Expose daemon on tcp://localhost:2375" in Docker Desktop settings,
// or (2) upgrade Testcontainers to a version with docker-java >= 3.4.x that handles the proxy.
// Track separately — this does NOT block OEWS ingestion logic, which compiles and runs correctly.
@Disabled("Testcontainers Docker discovery fails on Windows Docker Desktop WSL2 — see TODO above")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "laboriq.bls.sample-runner-enabled=false"
)
@Testcontainers
class OewsIngestionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @MockBean
    OewsDownloadService downloadService;

    @Autowired
    OewsIngestionService ingestionService;

    @Autowired
    OccupationRepository occupationRepository;

    @Autowired
    OccupationWageRepository wageRepository;

    @Autowired
    OccupationEmploymentRepository employmentRepository;

    @Autowired
    EtlRunRepository etlRunRepository;

    @BeforeEach
    void cleanDatabase() {
        employmentRepository.deleteAll();
        wageRepository.deleteAll();
        occupationRepository.deleteAll();
        etlRunRepository.deleteAll();
    }

    @Test
    void ingestsOccupationsWagesAndEmployment() throws Exception {
        when(downloadService.download(anyString())).thenReturn(buildTestZip());

        OewsJobAcceptedDto accepted = ingestionService.trigger();
        assertThat(accepted.status()).isEqualTo(EtlRunStatus.RUNNING);

        EtlRunDto result = awaitCompletion(accepted.etlRunId());
        assertThat(result.status()).isEqualTo(EtlRunStatus.COMPLETED);
        assertThat(result.recordsProcessed()).isEqualTo(3L);
        assertThat(result.endTime()).isNotNull();
        assertThat(occupationRepository.count()).isEqualTo(3L);
        assertThat(wageRepository.count()).isEqualTo(3L);
        assertThat(employmentRepository.count()).isEqualTo(3L);
    }

    @Test
    void upsertIsIdempotent() throws Exception {
        when(downloadService.download(anyString())).thenReturn(buildTestZip());

        awaitCompletion(ingestionService.trigger().etlRunId());
        awaitCompletion(ingestionService.trigger().etlRunId());

        assertThat(occupationRepository.count()).isEqualTo(3L);
        assertThat(wageRepository.count()).isEqualTo(3L);
        assertThat(employmentRepository.count()).isEqualTo(3L);
    }

    @Test
    void wagesContainPercentileData() throws Exception {
        when(downloadService.download(anyString())).thenReturn(buildTestZip());

        awaitCompletion(ingestionService.trigger().etlRunId());

        var wages = wageRepository.findByOccupationSocCodeOrderByYearDesc("15-1252");
        assertThat(wages).hasSize(1);
        var wage = wages.getFirst();
        assertThat(wage.getMeanWage()).isNotNull();
        assertThat(wage.getMedianWage()).isNotNull();
        assertThat(wage.getP10Wage()).isNotNull();
        assertThat(wage.getP25Wage()).isNotNull();
        assertThat(wage.getP75Wage()).isNotNull();
        assertThat(wage.getP90Wage()).isNotNull();
    }

    @Test
    void recordsFailedEtlRunOnDownloadError() throws Exception {
        when(downloadService.download(anyString())).thenThrow(new RuntimeException("network error"));

        OewsJobAcceptedDto accepted = ingestionService.trigger();
        EtlRunDto result = awaitCompletion(accepted.etlRunId());

        assertThat(result.status()).isEqualTo(EtlRunStatus.FAILED);
        assertThat(result.errorMessage()).isEqualTo("network error");
        assertThat(result.endTime()).isNotNull();
        assertThat(occupationRepository.count()).isZero();
    }

    // Polls until the ETL run leaves RUNNING state (COMPLETED or FAILED).
    // Timeout: 5 seconds — sufficient for in-memory test data; increase if needed for CI.
    private EtlRunDto awaitCompletion(Long etlRunId) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            EtlRunDto status = ingestionService.getStatus(etlRunId);
            if (status.status() != EtlRunStatus.RUNNING) {
                return status;
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("EtlRun " + etlRunId + " did not complete within 5 seconds");
    }

    private static byte[] buildTestZip() throws IOException {
        byte[] xlsxBytes = buildTestXlsx();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("national_M2024_dl.xlsx"));
            zos.write(xlsxBytes);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private static byte[] buildTestXlsx() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            String[] headers = {
                "OCC_CODE", "OCC_TITLE", "O_GROUP", "TOT_EMP", "EMP_PRSE",
                "H_MEAN", "A_MEAN", "MEAN_PRSE", "H_PCT10", "H_PCT25", "H_MEDIAN",
                "H_PCT75", "H_PCT90", "A_PCT10", "A_PCT25", "A_MEDIAN", "A_PCT75", "A_PCT90",
                "ANNUAL", "HOURLY"
            };
            var headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            addRow(sheet, 1, "15-1252", "Software Developers", "DETAILED",
                    1620000, 148310.0, 136620.0, 93590.0, 110120.0, 153580.0, 168580.0);
            addRow(sheet, 2, "11-1021", "General and Operations Managers", "DETAILED",
                    3320000, 123370.0, 113310.0, 72180.0, 87350.0, 128000.0, 163600.0);
            addRow(sheet, 3, "29-1141", "Registered Nurses", "DETAILED",
                    3213000, 91990.0, 82750.0, 62790.0, 73140.0, 100790.0, 122600.0);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    private static void addRow(XSSFSheet sheet, int rowNum,
            String occCode, String occTitle, String occGroup, long totEmp,
            double aMean, double aMedian, double aPct10, double aPct25, double aPct75, double aPct90) {
        var row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(occCode);
        row.createCell(1).setCellValue(occTitle);
        row.createCell(2).setCellValue(occGroup);
        row.createCell(3).setCellValue(totEmp);
        row.createCell(5).setCellValue("#");
        row.createCell(6).setCellValue(aMean);
        row.createCell(8).setCellValue("#");
        row.createCell(9).setCellValue("#");
        row.createCell(10).setCellValue("#");
        row.createCell(11).setCellValue("#");
        row.createCell(12).setCellValue("#");
        row.createCell(13).setCellValue(aPct10);
        row.createCell(14).setCellValue(aPct25);
        row.createCell(15).setCellValue(aMedian);
        row.createCell(16).setCellValue(aPct75);
        row.createCell(17).setCellValue(aPct90);
    }
}
