package com.gregluna.laboriq.etl;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtlRunRepository extends JpaRepository<EtlRun, Long> {

    List<EtlRun> findBySourceSystemOrderByStartTimeDesc(String sourceSystem);

    List<EtlRun> findByStatusOrderByStartTimeDesc(EtlRunStatus status);
}
