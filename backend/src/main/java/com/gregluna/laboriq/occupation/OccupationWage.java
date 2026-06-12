package com.gregluna.laboriq.occupation;

import com.gregluna.laboriq.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "occupation_wages",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_occupation_wages_occupation_year",
                columnNames = {"occupation_id", "year"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationWage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupation occupation;

    @Column(name = "year", nullable = false)
    private Short year;

    @Column(name = "median_wage", precision = 12, scale = 2)
    private BigDecimal medianWage;

    @Column(name = "mean_wage", precision = 12, scale = 2)
    private BigDecimal meanWage;

    @Column(name = "p10_wage", precision = 12, scale = 2)
    private BigDecimal p10Wage;

    @Column(name = "p25_wage", precision = 12, scale = 2)
    private BigDecimal p25Wage;

    @Column(name = "p75_wage", precision = 12, scale = 2)
    private BigDecimal p75Wage;

    @Column(name = "p90_wage", precision = 12, scale = 2)
    private BigDecimal p90Wage;
}
