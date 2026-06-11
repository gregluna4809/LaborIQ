package com.gregluna.laboriq.etl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EtlRunMappingTest {

    @Test
    void mapsToEtlRunsTable() throws NoSuchFieldException {
        assertThat(EtlRun.class).hasAnnotation(Entity.class);
        assertThat(EtlRun.class.getAnnotation(Table.class).name()).isEqualTo("etl_runs");

        assertColumn("sourceSystem", "source_system", false);
        assertColumn("startTime", "start_time", false);
        assertColumn("endTime", "end_time", true);
        assertColumn("recordsProcessed", "records_processed", false);

        Column status = EtlRun.class.getDeclaredField("status").getAnnotation(Column.class);
        assertThat(status.name()).isEqualTo("status");
        assertThat(status.nullable()).isFalse();
        assertThat(status.length()).isEqualTo(50);
        assertThat(EtlRun.class.getDeclaredField("status").getAnnotation(Enumerated.class).value())
                .isEqualTo(EnumType.STRING);

        Column errorMessage = EtlRun.class.getDeclaredField("errorMessage").getAnnotation(Column.class);
        assertThat(errorMessage.name()).isEqualTo("error_message");
        assertThat(errorMessage.columnDefinition()).isEqualTo("TEXT");
    }

    private static void assertColumn(String fieldName, String columnName, boolean nullable)
            throws NoSuchFieldException {
        Column column = EtlRun.class.getDeclaredField(fieldName).getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo(columnName);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
