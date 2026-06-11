package com.gregluna.laboriq.occupation;

import com.gregluna.laboriq.occupation.dto.OccupationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEducationDto;
import com.gregluna.laboriq.occupation.dto.OccupationEmploymentDto;
import com.gregluna.laboriq.occupation.dto.OccupationSkillDto;
import com.gregluna.laboriq.occupation.dto.OccupationWageDto;
import com.gregluna.laboriq.occupation.dto.RelatedOccupationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/occupations")
@RequiredArgsConstructor
public class OccupationController {

    private final OccupationReadService occupationReadService;

    @GetMapping("/search")
    public List<OccupationDto> searchOccupations(@RequestParam(name = "q", required = false) String query) {
        return occupationReadService.searchOccupations(query);
    }

    @GetMapping("/{socCode}")
    public OccupationDto getOccupation(@PathVariable String socCode) {
        return occupationReadService.getOccupationBySocCode(socCode);
    }

    @GetMapping("/{socCode}/wages")
    public List<OccupationWageDto> getWageHistory(@PathVariable String socCode) {
        return occupationReadService.getWageHistory(socCode);
    }

    @GetMapping("/{socCode}/wages/latest")
    public OccupationWageDto getLatestWageRecord(@PathVariable String socCode) {
        return occupationReadService.getLatestWageRecord(socCode);
    }

    @GetMapping("/{socCode}/employment")
    public List<OccupationEmploymentDto> getEmploymentHistory(@PathVariable String socCode) {
        return occupationReadService.getEmploymentHistory(socCode);
    }

    @GetMapping("/{socCode}/skills")
    public List<OccupationSkillDto> getSkills(@PathVariable String socCode) {
        return occupationReadService.getSkills(socCode);
    }

    @GetMapping("/{socCode}/education")
    public List<OccupationEducationDto> getEducationData(@PathVariable String socCode) {
        return occupationReadService.getEducationData(socCode);
    }

    @GetMapping("/{socCode}/related")
    public List<RelatedOccupationDto> getRelatedOccupations(@PathVariable String socCode) {
        return occupationReadService.getRelatedOccupations(socCode);
    }
}
