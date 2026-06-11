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
        name = "occupation_education",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_occupation_education_occupation_level",
                columnNames = {"occupation_id", "education_level"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationEducation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupation occupation;

    @Column(name = "education_level", nullable = false)
    private String educationLevel;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;
}
