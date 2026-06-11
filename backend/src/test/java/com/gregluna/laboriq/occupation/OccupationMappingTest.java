package com.gregluna.laboriq.occupation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class OccupationMappingTest {

    @Test
    void occupationMapsToOccupationsTable() throws NoSuchFieldException {
        assertThat(Occupation.class).hasAnnotation(Entity.class);
        assertThat(Occupation.class.getAnnotation(Table.class).name()).isEqualTo("occupations");

        Column socCode = Occupation.class.getDeclaredField("socCode").getAnnotation(Column.class);
        assertThat(socCode.name()).isEqualTo("soc_code");
        assertThat(socCode.nullable()).isFalse();
        assertThat(socCode.unique()).isTrue();
        assertThat(socCode.length()).isEqualTo(20);

        Field sourceMetadata = Occupation.class.getDeclaredField("sourceMetadata");
        assertThat(sourceMetadata.getAnnotation(Column.class).columnDefinition()).isEqualTo("jsonb");
        assertThat(sourceMetadata.getAnnotation(JdbcTypeCode.class).value()).isEqualTo(SqlTypes.JSON);
    }

    @Test
    void occupationOwnsExpectedChildCollections() throws NoSuchFieldException {
        assertOneToMany("wages", "occupation", true);
        assertOneToMany("employmentRecords", "occupation", true);
        assertOneToMany("skills", "occupation", true);
        assertOneToMany("educationRecords", "occupation", true);
        assertOneToMany("relatedOccupations", "sourceOccupation", true);
        assertOneToMany("inverseRelatedOccupations", "targetOccupation", false);
    }

    @Test
    void occupationFactTablesUseLazyOccupationRelationships() throws NoSuchFieldException {
        assertLazyOccupationJoin(OccupationWage.class, "occupation", "occupation_id");
        assertLazyOccupationJoin(OccupationEmployment.class, "occupation", "occupation_id");
        assertLazyOccupationJoin(OccupationSkill.class, "occupation", "occupation_id");
        assertLazyOccupationJoin(OccupationEducation.class, "occupation", "occupation_id");
    }

    @Test
    void relatedOccupationMapsBothDirectedOccupationReferences() throws NoSuchFieldException {
        assertThat(RelatedOccupation.class.getAnnotation(Table.class).name()).isEqualTo("related_occupations");

        assertLazyOccupationJoin(RelatedOccupation.class, "sourceOccupation", "source_occupation_id");
        assertLazyOccupationJoin(RelatedOccupation.class, "targetOccupation", "target_occupation_id");

        Column relationshipType = RelatedOccupation.class
                .getDeclaredField("relationshipType")
                .getAnnotation(Column.class);
        assertThat(relationshipType.name()).isEqualTo("relationship_type");
        assertThat(relationshipType.nullable()).isFalse();
        assertThat(relationshipType.length()).isEqualTo(100);
    }

    @Test
    void occupationDetailTablesMatchSchemaNames() {
        assertThat(OccupationWage.class.getAnnotation(Table.class).name()).isEqualTo("occupation_wages");
        assertThat(OccupationEmployment.class.getAnnotation(Table.class).name()).isEqualTo("occupation_employment");
        assertThat(OccupationSkill.class.getAnnotation(Table.class).name()).isEqualTo("occupation_skills");
        assertThat(OccupationEducation.class.getAnnotation(Table.class).name()).isEqualTo("occupation_education");
    }

    private static void assertOneToMany(String fieldName, String mappedBy, boolean orphanRemoval)
            throws NoSuchFieldException {
        OneToMany oneToMany = Occupation.class.getDeclaredField(fieldName).getAnnotation(OneToMany.class);

        assertThat(oneToMany.mappedBy()).isEqualTo(mappedBy);
        assertThat(oneToMany.orphanRemoval()).isEqualTo(orphanRemoval);
        if (orphanRemoval) {
            assertThat(Arrays.asList(oneToMany.cascade())).contains(CascadeType.ALL);
        }
    }

    private static void assertLazyOccupationJoin(Class<?> entityType, String fieldName, String columnName)
            throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

        assertThat(manyToOne.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(manyToOne.optional()).isFalse();
        assertThat(joinColumn.name()).isEqualTo(columnName);
        assertThat(joinColumn.nullable()).isFalse();
    }
}
