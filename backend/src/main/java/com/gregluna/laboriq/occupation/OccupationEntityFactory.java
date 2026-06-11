package com.gregluna.laboriq.occupation;

import java.math.BigDecimal;

public final class OccupationEntityFactory {

    private OccupationEntityFactory() {
    }

    public static OccupationWage occupationWage(Occupation occupation, Short year, BigDecimal meanWage) {
        OccupationWage wage = new OccupationWage();
        wage.setOccupation(occupation);
        wage.setYear(year);
        wage.setMeanWage(meanWage);
        return wage;
    }
}
