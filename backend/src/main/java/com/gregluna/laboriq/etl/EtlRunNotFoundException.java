package com.gregluna.laboriq.etl;

public class EtlRunNotFoundException extends RuntimeException {

    public EtlRunNotFoundException(Long id) {
        super("ETL run not found: " + id);
    }
}
