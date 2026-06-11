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

@Getter
@Setter
@Entity
@Table(
        name = "occupation_employment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_occupation_employment_occupation_year",
                columnNames = {"occupation_id", "year"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationEmployment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupation occupation;

    @Column(name = "year", nullable = false)
    private Short year;

    @Column(name = "employment_count")
    private Long employmentCount;
}
