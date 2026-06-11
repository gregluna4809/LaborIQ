package com.gregluna.laboriq.occupation;

public class OccupationNotFoundException extends RuntimeException {

    public OccupationNotFoundException(String socCode) {
        super("Occupation not found for SOC code: " + socCode);
    }
}
