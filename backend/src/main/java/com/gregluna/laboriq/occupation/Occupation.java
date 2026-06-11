package com.gregluna.laboriq.occupation;

import com.gregluna.laboriq.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "occupations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Occupation extends BaseEntity {

    @Column(name = "soc_code", nullable = false, unique = true, length = 20)
    private String socCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> sourceMetadata = new HashMap<>();

    @OneToMany(mappedBy = "occupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OccupationWage> wages = new HashSet<>();

    @OneToMany(mappedBy = "occupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OccupationEmployment> employmentRecords = new HashSet<>();

    @OneToMany(mappedBy = "occupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OccupationSkill> skills = new HashSet<>();

    @OneToMany(mappedBy = "occupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OccupationEducation> educationRecords = new HashSet<>();

    @OneToMany(mappedBy = "sourceOccupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RelatedOccupation> relatedOccupations = new HashSet<>();

    @OneToMany(mappedBy = "targetOccupation")
    private Set<RelatedOccupation> inverseRelatedOccupations = new HashSet<>();

    public Occupation(String socCode, String title) {
        this.socCode = socCode;
        this.title = title;
    }
}
