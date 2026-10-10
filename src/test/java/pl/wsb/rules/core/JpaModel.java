package pl.wsb.rules.core;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;

import java.util.List;
import java.util.stream.Stream;

/** Pomocnicze zapytania o model JPA, oparte wyłącznie na adnotacjach (bez uruchamiania aplikacji). */
public final class JpaModel {

    public static final String ENTITY = "jakarta.persistence.Entity";
    public static final String EMBEDDABLE = "jakarta.persistence.Embeddable";
    public static final String MAPPED_SUPERCLASS = "jakarta.persistence.MappedSuperclass";
    public static final String ONE_TO_ONE = "jakarta.persistence.OneToOne";
    public static final String ONE_TO_MANY = "jakarta.persistence.OneToMany";
    public static final String MANY_TO_ONE = "jakarta.persistence.ManyToOne";
    public static final String MANY_TO_MANY = "jakarta.persistence.ManyToMany";
    public static final String EMBEDDED = "jakarta.persistence.Embedded";
    public static final String EMBEDDED_ID = "jakarta.persistence.EmbeddedId";
    public static final String ID = "jakarta.persistence.Id";
    public static final String ENUMERATED = "jakarta.persistence.Enumerated";

    private JpaModel() {
    }

    public static List<JavaClass> entities() {
        return Project.productionClasses().stream().filter(c -> c.isAnnotatedWith(ENTITY)).toList();
    }

    public static List<JavaClass> embeddables() {
        return Project.productionClasses().stream().filter(c -> c.isAnnotatedWith(EMBEDDABLE)).toList();
    }

    /** Pola trwałe: encje, klasy bazowe @MappedSuperclass i @Embeddable. */
    public static Stream<JavaField> persistentFields() {
        return Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith(ENTITY) || c.isAnnotatedWith(EMBEDDABLE) || c.isAnnotatedWith(MAPPED_SUPERCLASS))
                .flatMap(c -> c.getFields().stream())
                .filter(f -> !f.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.STATIC))
                .filter(f -> !f.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.TRANSIENT))
                .filter(f -> !f.isAnnotatedWith("jakarta.persistence.Transient"));
    }

    public static Stream<JavaField> fieldsAnnotatedWith(String annotation) {
        return persistentFields().filter(f -> f.isAnnotatedWith(annotation));
    }

    public static String describe(JavaField field) {
        return field.getOwner().getSimpleName() + "." + field.getName();
    }
}
