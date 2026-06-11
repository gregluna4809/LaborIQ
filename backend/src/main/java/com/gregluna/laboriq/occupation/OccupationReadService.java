package com.gregluna.laboriq.occupation;

import com.gregluna.laboriq.occupation.dto.OccupationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEducationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEmploymentDto;
import com.gregluna.laboriq.occupation.dto.OccupationSkillDto;
import com.gregluna.laboriq.occupation.dto.OccupationWageDto;
import com.gregluna.laboriq.occupation.dto.RelatedOccupationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OccupationReadService {

    private final OccupationRepository occupationRepository;
    private final OccupationWageRepository occupationWageRepository;
    private final OccupationEmploymentRepository occupationEmploymentRepository;
    private final OccupationSkillRepository occupationSkillRepository;
    private final OccupationEducationRepository occupationEducationRepository;
    private final RelatedOccupationRepository relatedOccupationRepository;

    public List<OccupationDto> searchOccupations(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        return occupationRepository
                .findByTitleContainingIgnoreCaseOrSocCodeContainingIgnoreCaseOrderByTitleAsc(
                        normalizedQuery,
                        normalizedQuery
                )
                .stream()
                .map(this::toOccupationDto)
                .toList();
    }

    public OccupationDto getOccupationBySocCode(String socCode) {
        return occupationRepository.findBySocCode(socCode)
                .map(this::toOccupationDto)
                .orElseThrow(() -> new OccupationNotFoundException(socCode));
    }

    public List<OccupationWageDto> getWageHistory(String socCode) {
        requireOccupation(socCode);
        return occupationWageRepository.findByOccupationSocCodeOrderByYearDesc(socCode)
                .stream()
                .map(this::toWageDto)
                .toList();
    }

    public OccupationWageDto getLatestWageRecord(String socCode) {
        requireOccupation(socCode);
        return occupationWageRepository.findFirstByOccupationSocCodeOrderByYearDesc(socCode)
                .map(this::toWageDto)
                .orElseThrow(() -> new OccupationDataNotFoundException(
                        "No wage records found for SOC code: " + socCode
                ));
    }

    public List<OccupationEmploymentDto> getEmploymentHistory(String socCode) {
        requireOccupation(socCode);
        return occupationEmploymentRepository.findByOccupationSocCodeOrderByYearDesc(socCode)
                .stream()
                .map(this::toEmploymentDto)
                .toList();
    }

    public List<OccupationSkillDto> getSkills(String socCode) {
        requireOccupation(socCode);
        return occupationSkillRepository.findByOccupationSocCodeOrderByImportanceScoreDesc(socCode)
                .stream()
                .map(this::toSkillDto)
                .toList();
    }

    public List<OccupationEducationDto> getEducationData(String socCode) {
        requireOccupation(socCode);
        return occupationEducationRepository.findByOccupationSocCodeOrderByPercentageDesc(socCode)
                .stream()
                .map(this::toEducationDto)
                .toList();
    }

    public List<RelatedOccupationDto> getRelatedOccupations(String socCode) {
        requireOccupation(socCode);
        return relatedOccupationRepository.findBySourceOccupationSocCode(socCode)
                .stream()
                .map(this::toRelatedOccupationDto)
                .toList();
    }

    private void requireOccupation(String socCode) {
        if (!occupationRepository.existsBySocCode(socCode)) {
            throw new OccupationNotFoundException(socCode);
        }
    }

    private OccupationDto toOccupationDto(Occupation occupation) {
        return new OccupationDto(
                occupation.getId(),
                occupation.getSocCode(),
                occupation.getTitle(),
                occupation.getDescription(),
                occupation.getSourceMetadata()
        );
    }

    private OccupationWageDto toWageDto(OccupationWage wage) {
        return new OccupationWageDto(
                wage.getId(),
                wage.getOccupation().getSocCode(),
                wage.getYear(),
                wage.getMedianWage(),
                wage.getMeanWage()
        );
    }

    private OccupationEmploymentDto toEmploymentDto(OccupationEmployment employment) {
        return new OccupationEmploymentDto(
                employment.getId(),
                employment.getOccupation().getSocCode(),
                employment.getYear(),
                employment.getEmploymentCount()
        );
    }

    private OccupationSkillDto toSkillDto(OccupationSkill skill) {
        return new OccupationSkillDto(
                skill.getId(),
                skill.getOccupation().getSocCode(),
                skill.getSkillName(),
                skill.getImportanceScore()
        );
    }

    private OccupationEducationDto toEducationDto(OccupationEducation education) {
        return new OccupationEducationDto(
                education.getId(),
                education.getOccupation().getSocCode(),
                education.getEducationLevel(),
                education.getPercentage()
        );
    }

    private RelatedOccupationDto toRelatedOccupationDto(RelatedOccupation relatedOccupation) {
        return new RelatedOccupationDto(
                relatedOccupation.getId(),
                relatedOccupation.getSourceOccupation().getSocCode(),
                relatedOccupation.getTargetOccupation().getSocCode(),
                relatedOccupation.getRelationshipType()
        );
    }
}
