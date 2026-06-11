package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OccupationSkillRepository extends JpaRepository<OccupationSkill, Long> {

    List<OccupationSkill> findByOccupationSocCodeOrderByImportanceScoreDesc(String socCode);

    Optional<OccupationSkill> findByOccupationSocCodeAndSkillName(String socCode, String skillName);
}
