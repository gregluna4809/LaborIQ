package com.gregluna.laboriq.occupation;

import com.gregluna.laboriq.occupation.dto.OccupationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEducationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEmploymentDto;
import com.gregluna.laboriq.occupation.dto.OccupationSearchResultDto;
import com.gregluna.laboriq.occupation.dto.OccupationSkillDto;
import com.gregluna.laboriq.occupation.dto.OccupationWageDto;
import com.gregluna.laboriq.occupation.dto.RelatedOccupationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public List<OccupationSearchResultDto> searchOccupations(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        List<Occupation> occupations = occupationRepository
                .findByTitleContainingIgnoreCaseOrSocCodeContainingIgnoreCaseOrderByTitleAsc(
                        normalizedQuery,
                        normalizedQuery
                );
        List<String> socCodes = occupations.stream()
                .map(Occupation::getSocCode)
                .toList();
        Map<String, OccupationWage> latestWages = latestWagesBySocCode(socCodes);
        Map<String, OccupationEmployment> latestEmployment = latestEmploymentBySocCode(socCodes);

        return occupations.stream()
                .map(occupation -> toSearchResultDto(
                        occupation,
                        latestWages.get(occupation.getSocCode()),
                        latestEmployment.get(occupation.getSocCode())
                ))
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

    private Map<String, OccupationWage> latestWagesBySocCode(List<String> socCodes) {
        if (socCodes.isEmpty()) {
            return Map.of();
        }
        return occupationWageRepository.findByOccupationSocCodeIn(socCodes)
                .stream()
                .collect(Collectors.toMap(
                        wage -> wage.getOccupation().getSocCode(),
                        Function.identity(),
                        (left, right) -> latestByYear(left, right, OccupationWage::getYear)
                ));
    }

    private Map<String, OccupationEmployment> latestEmploymentBySocCode(List<String> socCodes) {
        if (socCodes.isEmpty()) {
            return Map.of();
        }
        return occupationEmploymentRepository.findByOccupationSocCodeIn(socCodes)
                .stream()
                .collect(Collectors.toMap(
                        employment -> employment.getOccupation().getSocCode(),
                        Function.identity(),
                        (left, right) -> latestByYear(left, right, OccupationEmployment::getYear)
                ));
    }

    private <T> T latestByYear(T left, T right, Function<T, Short> yearExtractor) {
        return Comparator.comparing(yearExtractor).compare(left, right) >= 0 ? left : right;
    }

    private OccupationSearchResultDto toSearchResultDto(
            Occupation occupation,
            OccupationWage latestWage,
            OccupationEmployment latestEmployment
    ) {
        return new OccupationSearchResultDto(
                occupation.getSocCode(),
                occupation.getTitle(),
                occupationGroup(occupation),
                latestWage == null ? null : latestWage.getMedianWage(),
                latestWage == null ? null : latestWage.getYear(),
                latestEmployment == null ? null : latestEmployment.getEmploymentCount(),
                latestEmployment == null ? null : latestEmployment.getYear()
        );
    }

    private String occupationGroup(Occupation occupation) {
        Object occGroup = occupation.getSourceMetadata().get("occGroup");
        return occGroup == null ? null : occGroup.toString();
    }

    private OccupationWageDto toWageDto(OccupationWage wage) {
        return new OccupationWageDto(
                wage.getId(),
                wage.getOccupation().getSocCode(),
                wage.getYear(),
                wage.getMedianWage(),
                wage.getMeanWage(),
                wage.getP10Wage(),
                wage.getP25Wage(),
                wage.getP75Wage(),
                wage.getP90Wage()
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
