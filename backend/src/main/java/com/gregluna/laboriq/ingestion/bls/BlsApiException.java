package com.gregluna.laboriq.ingestion.bls;

public class BlsApiException extends RuntimeException {

    public BlsApiException(String message) {
        super(message);
    }

    public BlsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
