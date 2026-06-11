package com.gregluna.laboriq.occupation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatedOccupationRepository extends JpaRepository<RelatedOccupation, Long> {

    List<RelatedOccupation> findBySourceOccupationSocCode(String sourceSocCode);

    List<RelatedOccupation> findByTargetOccupationSocCode(String targetSocCode);
}
