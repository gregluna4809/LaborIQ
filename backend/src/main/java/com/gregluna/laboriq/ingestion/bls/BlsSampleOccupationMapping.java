package com.gregluna.laboriq.ingestion.bls;

import com.gregluna.laboriq.occupation.Occupation;
import com.gregluna.laboriq.occupation.OccupationWage;

public record BlsSampleOccupationMapping(
        Occupation occupation,
        OccupationWage wage,
        String seriesId
) {
}
