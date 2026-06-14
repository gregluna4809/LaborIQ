-- Enforces at the database level that only one RUNNING job per source system can exist.
-- This partial unique index is the cross-JVM complement to the in-process synchronized
-- guard in OewsIngestionService. If two application instances somehow both pass the
-- existsBySourceSystemAndStatus check before either commits a RUNNING row, the second
-- INSERT will fail with a unique constraint violation rather than silently creating a
-- duplicate concurrent job.
CREATE UNIQUE INDEX uk_etl_runs_one_running_per_source
    ON etl_runs (source_system)
    WHERE status = 'RUNNING';
