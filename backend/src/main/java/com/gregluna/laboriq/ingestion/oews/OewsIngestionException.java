package com.gregluna.laboriq.ingestion.oews;

public class OewsIngestionException extends RuntimeException {

    public OewsIngestionException(String message) {
        super(message);
    }

    public OewsIngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
