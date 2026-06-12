ALTER TABLE occupation_wages
    ADD COLUMN p10_wage NUMERIC(12, 2),
    ADD COLUMN p25_wage NUMERIC(12, 2),
    ADD COLUMN p75_wage NUMERIC(12, 2),
    ADD COLUMN p90_wage NUMERIC(12, 2);

ALTER TABLE occupation_wages
    ADD CONSTRAINT chk_occupation_wages_p10_non_negative CHECK (p10_wage IS NULL OR p10_wage >= 0),
    ADD CONSTRAINT chk_occupation_wages_p25_non_negative CHECK (p25_wage IS NULL OR p25_wage >= 0),
    ADD CONSTRAINT chk_occupation_wages_p75_non_negative CHECK (p75_wage IS NULL OR p75_wage >= 0),
    ADD CONSTRAINT chk_occupation_wages_p90_non_negative CHECK (p90_wage IS NULL OR p90_wage >= 0);
