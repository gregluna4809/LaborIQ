package com.gregluna.laboriq.economic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {

    List<EconomicIndicator> findByIndicatorNameOrderByIndicatorDateDesc(String indicatorName);

    Optional<EconomicIndicator> findByIndicatorNameAndIndicatorDateAndSource(
            String indicatorName,
            LocalDate indicatorDate,
            String source
    );
}
