package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OccupationRepository extends JpaRepository<Occupation, Long> {

    Optional<Occupation> findBySocCode(String socCode);

    boolean existsBySocCode(String socCode);
}
