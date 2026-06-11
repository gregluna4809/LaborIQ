package com.gregluna.laboriq.economic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EconomicIndicatorMappingTest {

    @Test
    void mapsToEconomicIndicatorsTableWithNaturalUniqueness() throws NoSuchFieldException {
        assertThat(EconomicIndicator.class).hasAnnotation(Entity.class);

        Table table = EconomicIndicator.class.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo("economic_indicators");
        assertThat(table.uniqueConstraints())
                .extracting(UniqueConstraint::name)
                .contains("uk_economic_indicators_name_date_source");

        assertColumn("indicatorName", "indicator_name", false);
        assertColumn("indicatorDate", "indicator_date", false);
        assertColumn("source", "source", false);

        Column value = EconomicIndicator.class.getDeclaredField("value").getAnnotation(Column.class);
        assertThat(value.name()).isEqualTo("value");
        assertThat(value.nullable()).isFalse();
        assertThat(value.precision()).isEqualTo(18);
        assertThat(value.scale()).isEqualTo(4);
    }

    private static void assertColumn(String fieldName, String columnName, boolean nullable)
            throws NoSuchFieldException {
        Column column = EconomicIndicator.class.getDeclaredField(fieldName).getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo(columnName);
        assertThat(column.nullable()).isEqualTo(nullable);
    }
}
