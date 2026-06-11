package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OccupationWageRepository extends JpaRepository<OccupationWage, Long> {

    List<OccupationWage> findByOccupationSocCodeOrderByYearDesc(String socCode);

    Optional<OccupationWage> findByOccupationSocCodeAndYear(String socCode, Short year);
}
