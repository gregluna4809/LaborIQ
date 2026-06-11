package com.gregluna.laboriq.occupation;

import com.gregluna.laboriq.occupation.dto.OccupationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEducationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEmploymentDto;
import com.gregluna.laboriq.occupation.dto.OccupationSkillDto;
import com.gregluna.laboriq.occupation.dto.OccupationWageDto;
import com.gregluna.laboriq.occupation.dto.RelatedOccupationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccupationReadServiceTest {

    @Mock
    private OccupationRepository occupationRepository;

    @Mock
    private OccupationWageRepository occupationWageRepository;

    @Mock
    private OccupationEmploymentRepository occupationEmploymentRepository;

    @Mock
    private OccupationSkillRepository occupationSkillRepository;

    @Mock
    private OccupationEducationRepository occupationEducationRepository;

    @Mock
    private RelatedOccupationRepository relatedOccupationRepository;

    private OccupationReadService occupationReadService;

    @BeforeEach
    void setUp() {
        occupationReadService = new OccupationReadService(
                occupationRepository,
                occupationWageRepository,
                occupationEmploymentRepository,
                occupationSkillRepository,
                occupationEducationRepository,
                relatedOccupationRepository
        );
    }

    @Test
    void searchesOccupationsByTitleOrSocCode() {
        Occupation occupation = occupation("15-1252", "Software Developers");
        occupation.setSourceMetadata(Map.of("source", "test"));
        when(occupationRepository.findByTitleContainingIgnoreCaseOrSocCodeContainingIgnoreCaseOrderByTitleAsc(
                "software",
                "software"
        )).thenReturn(List.of(occupation));

        List<OccupationDto> results = occupationReadService.searchOccupations(" software ");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().socCode()).isEqualTo("15-1252");
        assertThat(results.getFirst().title()).isEqualTo("Software Developers");
    }

    @Test
    void getsOccupationBySocCode() {
        when(occupationRepository.findBySocCode("15-1252"))
                .thenReturn(Optional.of(occupation("15-1252", "Software Developers")));

        OccupationDto result = occupationReadService.getOccupationBySocCode("15-1252");

        assertThat(result.socCode()).isEqualTo("15-1252");
        assertThat(result.title()).isEqualTo("Software Developers");
    }

    @Test
    void throwsWhenOccupationIsMissing() {
        when(occupationRepository.findBySocCode("00-0000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> occupationReadService.getOccupationBySocCode("00-0000"))
                .isInstanceOf(OccupationNotFoundException.class)
                .hasMessage("Occupation not found for SOC code: 00-0000");
    }

    @Test
    void getsWageHistoryForOccupation() {
        Occupation occupation = occupation("15-1252", "Software Developers");
        when(occupationRepository.existsBySocCode("15-1252")).thenReturn(true);
        when(occupationWageRepository.findByOccupationSocCodeOrderByYearDesc("15-1252"))
                .thenReturn(List.of(wage(occupation, (short) 2024, "132270.00", "138110.00")));

        List<OccupationWageDto> results = occupationReadService.getWageHistory("15-1252");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().year()).isEqualTo((short) 2024);
        assertThat(results.getFirst().medianWage()).isEqualByComparingTo("132270.00");
        assertThat(results.getFirst().meanWage()).isEqualByComparingTo("138110.00");
    }

    @Test
    void getsLatestWageRecordForOccupation() {
        Occupation occupation = occupation("15-1252", "Software Developers");
        when(occupationRepository.existsBySocCode("15-1252")).thenReturn(true);
        when(occupationWageRepository.findFirstByOccupationSocCodeOrderByYearDesc("15-1252"))
                .thenReturn(Optional.of(wage(occupation, (short) 2024, "132270.00", "138110.00")));

        OccupationWageDto result = occupationReadService.getLatestWageRecord("15-1252");

        assertThat(result.socCode()).isEqualTo("15-1252");
        assertThat(result.year()).isEqualTo((short) 2024);
    }

    @Test
    void throwsWhenLatestWageRecordIsMissing() {
        when(occupationRepository.existsBySocCode("15-1252")).thenReturn(true);
        when(occupationWageRepository.findFirstByOccupationSocCodeOrderByYearDesc("15-1252"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> occupationReadService.getLatestWageRecord("15-1252"))
                .isInstanceOf(OccupationDataNotFoundException.class)
                .hasMessage("No wage records found for SOC code: 15-1252");
    }

    @Test
    void getsEmploymentHistoryForOccupation() {
        Occupation occupation = occupation("15-1252", "Software Developers");
        when(occupationRepository.existsBySocCode("15-1252")).thenReturn(true);
        when(occupationEmploymentRepository.findByOccupationSocCodeOrderByYearDesc("15-1252"))
                .thenReturn(List.of(employment(occupation, (short) 2024, 1900000L)));

        List<OccupationEmploymentDto> results = occupationReadService.getEmploymentHistory("15-1252");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().employmentCount()).isEqualTo(1900000L);
    }

    @Test
    void getsSkillsForOccupation() {
        Occupation occupation = occupation("15-1252", "Software Developers");
        when(occupationRepository.existsBySocCode("15-1252")).thenReturn(true);
        when(occupationSkillRepository.findByOccupationSocCodeOrderByImportanceScoreDesc("15-1252"))
                .thenReturn(List.of(skill(occupation, "Programming", "92.50")));

        List<OccupationSkillDto> results = occupationReadService.getSkills("15-1252");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().skillName()).isEqualTo("Programming");
        assertThat(results.getFirst().importanceScore()).isEqualByComparingTo("92.50");
    }

    @Test
    void getsEducationDataForOccupation() {
        Occupation occupation = occupation("15-1252", "Software Developers");
        when(occupationRepository.existsBySocCode("15-1252")).thenReturn(true);
        when(occupationEducationRepository.findByOccupationSocCodeOrderByPercentageDesc("15-1252"))
                .thenReturn(List.of(education(occupation, "Bachelor's degree", "73.20")));

        List<OccupationEducationDto> results = occupationReadService.getEducationData("15-1252");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().educationLevel()).isEqualTo("Bachelor's degree");
        assertThat(results.getFirst().percentage()).isEqualByComparingTo("73.20");
    }

    @Test
    void getsRelatedOccupations() {
        Occupation source = occupation("15-1252", "Software Developers");
        Occupation target = occupation("15-1251", "Computer Programmers");
        when(occupationRepository.existsBySocCode("15-1252")).thenReturn(true);
        when(relatedOccupationRepository.findBySourceOccupationSocCode("15-1252"))
                .thenReturn(List.of(relatedOccupation(source, target, "similar")));

        List<RelatedOccupationDto> results = occupationReadService.getRelatedOccupations("15-1252");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().sourceSocCode()).isEqualTo("15-1252");
        assertThat(results.getFirst().targetSocCode()).isEqualTo("15-1251");
        assertThat(results.getFirst().relationshipType()).isEqualTo("similar");
    }

    @Test
    void listMethodsRequireExistingOccupation() {
        when(occupationRepository.existsBySocCode("00-0000")).thenReturn(false);

        assertThatThrownBy(() -> occupationReadService.getSkills("00-0000"))
                .isInstanceOf(OccupationNotFoundException.class);
        verify(occupationRepository).existsBySocCode("00-0000");
    }

    private static Occupation occupation(String socCode, String title) {
        return new Occupation(socCode, title);
    }

    private static OccupationWage wage(Occupation occupation, Short year, String medianWage, String meanWage) {
        OccupationWage wage = new OccupationWage();
        wage.setOccupation(occupation);
        wage.setYear(year);
        wage.setMedianWage(new BigDecimal(medianWage));
        wage.setMeanWage(new BigDecimal(meanWage));
        return wage;
    }

    private static OccupationEmployment employment(Occupation occupation, Short year, Long employmentCount) {
        OccupationEmployment employment = new OccupationEmployment();
        employment.setOccupation(occupation);
        employment.setYear(year);
        employment.setEmploymentCount(employmentCount);
        return employment;
    }

    private static OccupationSkill skill(Occupation occupation, String skillName, String importanceScore) {
        OccupationSkill skill = new OccupationSkill();
        skill.setOccupation(occupation);
        skill.setSkillName(skillName);
        skill.setImportanceScore(new BigDecimal(importanceScore));
        return skill;
    }

    private static OccupationEducation education(Occupation occupation, String educationLevel, String percentage) {
        OccupationEducation education = new OccupationEducation();
        education.setOccupation(occupation);
        education.setEducationLevel(educationLevel);
        education.setPercentage(new BigDecimal(percentage));
        return education;
    }

    private static RelatedOccupation relatedOccupation(
            Occupation source,
            Occupation target,
            String relationshipType
    ) {
        RelatedOccupation relatedOccupation = new RelatedOccupation();
        relatedOccupation.setSourceOccupation(source);
        relatedOccupation.setTargetOccupation(target);
        relatedOccupation.setRelationshipType(relationshipType);
        return relatedOccupation;
    }
}
