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
        name = "occupation_skills",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_occupation_skills_occupation_skill",
                columnNames = {"occupation_id", "skill_name"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupation occupation;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "importance_score", precision = 5, scale = 2)
    private BigDecimal importanceScore;
}
