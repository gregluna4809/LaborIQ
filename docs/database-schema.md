# LaborIQ Database Schema

This document describes the initial PostgreSQL schema created by Flyway migration `V1__create_laboriq_schema.sql`.

## ERD

```mermaid
erDiagram
    occupations {
        bigint id PK
        varchar soc_code UK
        varchar title
        text description
        jsonb source_metadata
        timestamptz created_at
        timestamptz updated_at
    }

    occupation_wages {
        bigint id PK
        bigint occupation_id FK
        smallint year
        numeric median_wage
        numeric mean_wage
        timestamptz created_at
        timestamptz updated_at
    }

    occupation_employment {
        bigint id PK
        bigint occupation_id FK
        smallint year
        bigint employment_count
        timestamptz created_at
        timestamptz updated_at
    }

    occupation_skills {
        bigint id PK
        bigint occupation_id FK
        varchar skill_name
        numeric importance_score
        timestamptz created_at
        timestamptz updated_at
    }

    occupation_education {
        bigint id PK
        bigint occupation_id FK
        varchar education_level
        numeric percentage
        timestamptz created_at
        timestamptz updated_at
    }

    related_occupations {
        bigint id PK
        bigint source_occupation_id FK
        bigint target_occupation_id FK
        varchar relationship_type
        timestamptz created_at
        timestamptz updated_at
    }

    economic_indicators {
        bigint id PK
        varchar indicator_name
        date indicator_date
        numeric value
        varchar source
        timestamptz created_at
        timestamptz updated_at
    }

    etl_runs {
        bigint id PK
        varchar source_system
        timestamptz start_time
        timestamptz end_time
        varchar status
        bigint records_processed
        text error_message
        timestamptz created_at
        timestamptz updated_at
    }

    occupations ||--o{ occupation_wages : has
    occupations ||--o{ occupation_employment : has
    occupations ||--o{ occupation_skills : has
    occupations ||--o{ occupation_education : has
    occupations ||--o{ related_occupations : source
    occupations ||--o{ related_occupations : target
```

## Table Notes

`occupations` stores the canonical occupation record keyed by unique SOC code. `source_metadata` is JSONB so ETL jobs can retain source dataset identifiers, release versions, URLs, and other provenance fields without changing the core schema.

`occupation_wages`, `occupation_employment`, `occupation_skills`, and `occupation_education` are occupation detail tables with cascade deletes from `occupations`. Wage and employment rows are unique by occupation and year. Skill and education rows are unique by occupation and name or level.

`related_occupations` models directed occupation relationships. The schema prevents self-relationships and duplicate source-target-type combinations.

`economic_indicators` stores date-based labor and macroeconomic measurements. Rows are unique by indicator name, date, and source.

`etl_runs` records ingestion activity by source system, timing, status, record count, and error details.

## Index Summary

The migration adds indexes for common lookup and join paths:

- SOC code uniqueness on `occupations`.
- Foreign-key indexes on all occupation detail tables.
- Year indexes on wage and employment fact tables.
- Lookup indexes for skill name, education level, relationship type, economic indicator name/date/source, and ETL status/start time.
- A GIN index on `occupations.source_metadata` for JSONB metadata queries.
