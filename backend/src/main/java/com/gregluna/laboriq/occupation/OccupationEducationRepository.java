package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OccupationEducationRepository extends JpaRepository<OccupationEducation, Long> {

    List<OccupationEducation> findByOccupationSocCodeOrderByPercentageDesc(String socCode);

    Optional<OccupationEducation> findByOccupationSocCodeAndEducationLevel(String socCode, String educationLevel);
}
