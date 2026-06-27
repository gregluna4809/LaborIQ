package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OccupationWageRepository extends JpaRepository<OccupationWage, Long> {

    List<OccupationWage> findByOccupationSocCodeOrderByYearDesc(String socCode);

    Optional<OccupationWage> findFirstByOccupationSocCodeOrderByYearDesc(String socCode);

    @Query("""
            select wage
            from OccupationWage wage
            join fetch wage.occupation occupation
            where occupation.socCode in :socCodes
            """)
    List<OccupationWage> findByOccupationSocCodeIn(@Param("socCodes") Collection<String> socCodes);

    Optional<OccupationWage> findByOccupationSocCodeAndYear(String socCode, Short year);
}
