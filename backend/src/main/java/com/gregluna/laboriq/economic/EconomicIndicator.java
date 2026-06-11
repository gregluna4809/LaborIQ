package com.gregluna.laboriq.economic;

import com.gregluna.laboriq.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "economic_indicators",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_economic_indicators_name_date_source",
                columnNames = {"indicator_name", "indicator_date", "source"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EconomicIndicator extends BaseEntity {

    @Column(name = "indicator_name", nullable = false)
    private String indicatorName;

    @Column(name = "indicator_date", nullable = false)
    private LocalDate indicatorDate;

    @Column(name = "value", nullable = false, precision = 18, scale = 4)
    private BigDecimal value;

    @Column(name = "source", nullable = false)
    private String source;
}
