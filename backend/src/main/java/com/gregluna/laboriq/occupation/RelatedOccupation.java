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
        name = "related_occupations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_related_occupations_relationship",
                columnNames = {"source_occupation_id", "target_occupation_id", "relationship_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RelatedOccupation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_occupation_id", nullable = false)
    private Occupation sourceOccupation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_occupation_id", nullable = false)
    private Occupation targetOccupation;

    @Column(name = "relationship_type", nullable = false, length = 100)
    private String relationshipType;
}
