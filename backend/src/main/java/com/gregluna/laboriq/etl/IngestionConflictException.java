package com.gregluna.laboriq.etl;

public class IngestionConflictException extends RuntimeException {

    public IngestionConflictException(String sourceSystem) {
        super("An ingestion job for " + sourceSystem + " is already running");
    }
}
