package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OccupationEmploymentRepository extends JpaRepository<OccupationEmployment, Long> {

    List<OccupationEmployment> findByOccupationSocCodeOrderByYearDesc(String socCode);

    @Query("""
            select employment
            from OccupationEmployment employment
            join fetch employment.occupation occupation
            where occupation.socCode in :socCodes
            """)
    List<OccupationEmployment> findByOccupationSocCodeIn(@Param("socCodes") Collection<String> socCodes);

    Optional<OccupationEmployment> findByOccupationSocCodeAndYear(String socCode, Short year);
}
