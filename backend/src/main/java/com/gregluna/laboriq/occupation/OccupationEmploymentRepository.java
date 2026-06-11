package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OccupationEmploymentRepository extends JpaRepository<OccupationEmployment, Long> {

    List<OccupationEmployment> findByOccupationSocCodeOrderByYearDesc(String socCode);

    Optional<OccupationEmployment> findByOccupationSocCodeAndYear(String socCode, Short year);
}
