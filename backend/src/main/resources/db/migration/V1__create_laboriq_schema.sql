CREATE TABLE occupations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    soc_code VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    source_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_occupations_soc_code UNIQUE (soc_code),
    CONSTRAINT chk_occupations_soc_code_not_blank CHECK (btrim(soc_code) <> ''),
    CONSTRAINT chk_occupations_title_not_blank CHECK (btrim(title) <> '')
);

CREATE TABLE occupation_wages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occupation_id BIGINT NOT NULL,
    year SMALLINT NOT NULL,
    median_wage NUMERIC(12, 2),
    mean_wage NUMERIC(12, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_occupation_wages_occupation
        FOREIGN KEY (occupation_id) REFERENCES occupations (id) ON DELETE CASCADE,
    CONSTRAINT uk_occupation_wages_occupation_year UNIQUE (occupation_id, year),
    CONSTRAINT chk_occupation_wages_year CHECK (year BETWEEN 1900 AND 2200),
    CONSTRAINT chk_occupation_wages_median_non_negative CHECK (median_wage IS NULL OR median_wage >= 0),
    CONSTRAINT chk_occupation_wages_mean_non_negative CHECK (mean_wage IS NULL OR mean_wage >= 0)
);

CREATE TABLE occupation_employment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occupation_id BIGINT NOT NULL,
    year SMALLINT NOT NULL,
    employment_count BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_occupation_employment_occupation
        FOREIGN KEY (occupation_id) REFERENCES occupations (id) ON DELETE CASCADE,
    CONSTRAINT uk_occupation_employment_occupation_year UNIQUE (occupation_id, year),
    CONSTRAINT chk_occupation_employment_year CHECK (year BETWEEN 1900 AND 2200),
    CONSTRAINT chk_occupation_employment_count_non_negative
        CHECK (employment_count IS NULL OR employment_count >= 0)
);

CREATE TABLE occupation_skills (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occupation_id BIGINT NOT NULL,
    skill_name VARCHAR(255) NOT NULL,
    importance_score NUMERIC(5, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_occupation_skills_occupation
        FOREIGN KEY (occupation_id) REFERENCES occupations (id) ON DELETE CASCADE,
    CONSTRAINT uk_occupation_skills_occupation_skill UNIQUE (occupation_id, skill_name),
    CONSTRAINT chk_occupation_skills_skill_name_not_blank CHECK (btrim(skill_name) <> ''),
    CONSTRAINT chk_occupation_skills_importance_score_range
        CHECK (importance_score IS NULL OR importance_score BETWEEN 0 AND 100)
);

CREATE TABLE occupation_education (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occupation_id BIGINT NOT NULL,
    education_level VARCHAR(255) NOT NULL,
    percentage NUMERIC(5, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_occupation_education_occupation
        FOREIGN KEY (occupation_id) REFERENCES occupations (id) ON DELETE CASCADE,
    CONSTRAINT uk_occupation_education_occupation_level UNIQUE (occupation_id, education_level),
    CONSTRAINT chk_occupation_education_level_not_blank CHECK (btrim(education_level) <> ''),
    CONSTRAINT chk_occupation_education_percentage_range
        CHECK (percentage IS NULL OR percentage BETWEEN 0 AND 100)
);

CREATE TABLE related_occupations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_occupation_id BIGINT NOT NULL,
    target_occupation_id BIGINT NOT NULL,
    relationship_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_related_occupations_source
        FOREIGN KEY (source_occupation_id) REFERENCES occupations (id) ON DELETE CASCADE,
    CONSTRAINT fk_related_occupations_target
        FOREIGN KEY (target_occupation_id) REFERENCES occupations (id) ON DELETE CASCADE,
    CONSTRAINT uk_related_occupations_relationship
        UNIQUE (source_occupation_id, target_occupation_id, relationship_type),
    CONSTRAINT chk_related_occupations_relationship_type_not_blank CHECK (btrim(relationship_type) <> ''),
    CONSTRAINT chk_related_occupations_not_self_related CHECK (source_occupation_id <> target_occupation_id)
);

CREATE TABLE economic_indicators (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    indicator_name VARCHAR(255) NOT NULL,
    indicator_date DATE NOT NULL,
    value NUMERIC(18, 4) NOT NULL,
    source VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_economic_indicators_name_date_source UNIQUE (indicator_name, indicator_date, source),
    CONSTRAINT chk_economic_indicators_name_not_blank CHECK (btrim(indicator_name) <> ''),
    CONSTRAINT chk_economic_indicators_source_not_blank CHECK (btrim(source) <> '')
);

CREATE TABLE etl_runs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_system VARCHAR(255) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL,
    records_processed BIGINT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_etl_runs_source_system_not_blank CHECK (btrim(source_system) <> ''),
    CONSTRAINT chk_etl_runs_status_not_blank CHECK (btrim(status) <> ''),
    CONSTRAINT chk_etl_runs_records_processed_non_negative CHECK (records_processed >= 0),
    CONSTRAINT chk_etl_runs_end_time_after_start_time CHECK (end_time IS NULL OR end_time >= start_time)
);

CREATE INDEX idx_occupations_title ON occupations (title);
CREATE INDEX idx_occupations_source_metadata ON occupations USING GIN (source_metadata);

CREATE INDEX idx_occupation_wages_occupation_id ON occupation_wages (occupation_id);
CREATE INDEX idx_occupation_wages_year ON occupation_wages (year);

CREATE INDEX idx_occupation_employment_occupation_id ON occupation_employment (occupation_id);
CREATE INDEX idx_occupation_employment_year ON occupation_employment (year);

CREATE INDEX idx_occupation_skills_occupation_id ON occupation_skills (occupation_id);
CREATE INDEX idx_occupation_skills_skill_name ON occupation_skills (skill_name);

CREATE INDEX idx_occupation_education_occupation_id ON occupation_education (occupation_id);
CREATE INDEX idx_occupation_education_level ON occupation_education (education_level);

CREATE INDEX idx_related_occupations_source_id ON related_occupations (source_occupation_id);
CREATE INDEX idx_related_occupations_target_id ON related_occupations (target_occupation_id);
CREATE INDEX idx_related_occupations_relationship_type ON related_occupations (relationship_type);

CREATE INDEX idx_economic_indicators_name ON economic_indicators (indicator_name);
CREATE INDEX idx_economic_indicators_date ON economic_indicators (indicator_date);
CREATE INDEX idx_economic_indicators_source ON economic_indicators (source);

CREATE INDEX idx_etl_runs_source_system ON etl_runs (source_system);
CREATE INDEX idx_etl_runs_status ON etl_runs (status);
CREATE INDEX idx_etl_runs_start_time ON etl_runs (start_time);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_occupations_set_updated_at
BEFORE UPDATE ON occupations
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_occupation_wages_set_updated_at
BEFORE UPDATE ON occupation_wages
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_occupation_employment_set_updated_at
BEFORE UPDATE ON occupation_employment
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_occupation_skills_set_updated_at
BEFORE UPDATE ON occupation_skills
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_occupation_education_set_updated_at
BEFORE UPDATE ON occupation_education
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_related_occupations_set_updated_at
BEFORE UPDATE ON related_occupations
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_economic_indicators_set_updated_at
BEFORE UPDATE ON economic_indicators
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_etl_runs_set_updated_at
BEFORE UPDATE ON etl_runs
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
