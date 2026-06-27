package com.gregluna.laboriq.occupation;

import com.gregluna.laboriq.config.SecurityConfig;
import com.gregluna.laboriq.occupation.dto.OccupationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEducationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEmploymentDto;
import com.gregluna.laboriq.occupation.dto.OccupationSearchResultDto;
import com.gregluna.laboriq.occupation.dto.OccupationSkillDto;
import com.gregluna.laboriq.occupation.dto.OccupationWageDto;
import com.gregluna.laboriq.occupation.dto.RelatedOccupationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OccupationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "laboriq.admin.api-key=test-key")
class OccupationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OccupationReadService occupationReadService;

    @Test
    void searchOccupationsReturnsSearchResultDtos() throws Exception {
        when(occupationReadService.searchOccupations("software"))
                .thenReturn(List.of(new OccupationSearchResultDto(
                        "15-1252",
                        "Software Developers",
                        "DETAILED",
                        new BigDecimal("132270.00"),
                        (short) 2024,
                        1900000L,
                        (short) 2024
                )));

        mockMvc.perform(get("/api/occupations/search").param("q", "software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].socCode").value("15-1252"))
                .andExpect(jsonPath("$[0].title").value("Software Developers"))
                .andExpect(jsonPath("$[0].occGroup").value("DETAILED"))
                .andExpect(jsonPath("$[0].latestMedianWage").value(132270.00))
                .andExpect(jsonPath("$[0].latestWageYear").value(2024))
                .andExpect(jsonPath("$[0].latestEmploymentCount").value(1900000L))
                .andExpect(jsonPath("$[0].latestEmploymentYear").value(2024));
    }

    @Test
    void searchWithBlankQueryReturnsEmptyList() throws Exception {
        when(occupationReadService.searchOccupations("")).thenReturn(List.of());

        mockMvc.perform(get("/api/occupations/search").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getOccupationReturnsOccupationDto() throws Exception {
        when(occupationReadService.getOccupationBySocCode("15-1252"))
                .thenReturn(new OccupationDto(
                        1L,
                        "15-1252",
                        "Software Developers",
                        "Builds software",
                        Map.of()
                ));

        mockMvc.perform(get("/api/occupations/{socCode}", "15-1252"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.socCode").value("15-1252"))
                .andExpect(jsonPath("$.title").value("Software Developers"));
    }

    @Test
    void getWageHistoryReturnsWageDtos() throws Exception {
        when(occupationReadService.getWageHistory("15-1252"))
                .thenReturn(List.of(new OccupationWageDto(
                        10L,
                        "15-1252",
                        (short) 2024,
                        new BigDecimal("132270.00"),
                        new BigDecimal("138110.00"),
                        null, null, null, null
                )));

        mockMvc.perform(get("/api/occupations/{socCode}/wages", "15-1252"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].year").value(2024))
                .andExpect(jsonPath("$[0].medianWage").value(132270.00))
                .andExpect(jsonPath("$[0].meanWage").value(138110.00));
    }

    @Test
    void getLatestWageReturnsWageDto() throws Exception {
        when(occupationReadService.getLatestWageRecord("15-1252"))
                .thenReturn(new OccupationWageDto(
                        10L,
                        "15-1252",
                        (short) 2024,
                        new BigDecimal("132270.00"),
                        new BigDecimal("138110.00"),
                        null, null, null, null
                ));

        mockMvc.perform(get("/api/occupations/{socCode}/wages/latest", "15-1252"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.socCode").value("15-1252"))
                .andExpect(jsonPath("$.year").value(2024));
    }

    @Test
    void getEmploymentHistoryReturnsEmploymentDtos() throws Exception {
        when(occupationReadService.getEmploymentHistory("15-1252"))
                .thenReturn(List.of(new OccupationEmploymentDto(20L, "15-1252", (short) 2024, 1900000L)));

        mockMvc.perform(get("/api/occupations/{socCode}/employment", "15-1252"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employmentCount").value(1900000L));
    }

    @Test
    void getSkillsReturnsSkillDtos() throws Exception {
        when(occupationReadService.getSkills("15-1252"))
                .thenReturn(List.of(new OccupationSkillDto(
                        30L,
                        "15-1252",
                        "Programming",
                        new BigDecimal("92.50")
                )));

        mockMvc.perform(get("/api/occupations/{socCode}/skills", "15-1252"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].skillName").value("Programming"))
                .andExpect(jsonPath("$[0].importanceScore").value(92.50));
    }

    @Test
    void getEducationReturnsEducationDtos() throws Exception {
        when(occupationReadService.getEducationData("15-1252"))
                .thenReturn(List.of(new OccupationEducationDto(
                        40L,
                        "15-1252",
                        "Bachelor's degree",
                        new BigDecimal("73.20")
                )));

        mockMvc.perform(get("/api/occupations/{socCode}/education", "15-1252"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].educationLevel").value("Bachelor's degree"))
                .andExpect(jsonPath("$[0].percentage").value(73.20));
    }

    @Test
    void getRelatedOccupationsReturnsRelatedOccupationDtos() throws Exception {
        when(occupationReadService.getRelatedOccupations("15-1252"))
                .thenReturn(List.of(new RelatedOccupationDto(50L, "15-1252", "15-1251", "similar")));

        mockMvc.perform(get("/api/occupations/{socCode}/related", "15-1252"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sourceSocCode").value("15-1252"))
                .andExpect(jsonPath("$[0].targetSocCode").value("15-1251"))
                .andExpect(jsonPath("$[0].relationshipType").value("similar"));
    }

    @Test
    void returnsNotFoundForMissingOccupation() throws Exception {
        when(occupationReadService.getOccupationBySocCode("00-0000"))
                .thenThrow(new OccupationNotFoundException("00-0000"));

        mockMvc.perform(get("/api/occupations/{socCode}", "00-0000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Occupation not found for SOC code: 00-0000"))
                .andExpect(jsonPath("$.path").value("/api/occupations/00-0000"));
    }

    @Test
    void returnsNotFoundForMissingOccupationData() throws Exception {
        when(occupationReadService.getLatestWageRecord("15-1252"))
                .thenThrow(new OccupationDataNotFoundException("No wage records found for SOC code: 15-1252"));

        mockMvc.perform(get("/api/occupations/{socCode}/wages/latest", "15-1252"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No wage records found for SOC code: 15-1252"))
                .andExpect(jsonPath("$.path").value("/api/occupations/15-1252/wages/latest"));
    }
}
