package pl.wsb.rules.lab03;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.Project;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.SinceLab;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static org.assertj.core.api.Assertions.assertThat;

@SinceLab(3)
@DisplayName("LAB 03 · Reguły encji JPA (ArchUnit)")
class Lab03EntityRulesTest {

    @Test
    @DisplayName("R3.01 Encje, @Embeddable i enumy domeny leżą w pakiecie <moduł>.domain")
    void entitiesLiveInDomainPackage() {
        String domainPackage = Project.rootPackage() + ".*.domain..";
        classes().that().areAnnotatedWith(JpaModel.ENTITY)
                .or().areAnnotatedWith(JpaModel.EMBEDDABLE)
                .or().areAnnotatedWith(JpaModel.MAPPED_SUPERCLASS)
                .should().resideInAPackage(domainPackage)
                .because("model domeny każdego modułu trzymamy w jednym miejscu: pl.wsb.<projekt>.<moduł>.domain"
                        + " (moduł wspólny: pl.wsb.<projekt>.shared.domain)")
                .allowEmptyShould(true)
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R3.02 Encje mają bezargumentowy konstruktor public/protected")
    void entitiesHaveNoArgConstructor() {
        classes().that().areAnnotatedWith(JpaModel.ENTITY)
                .should(haveNoArgConstructorVisibleToHibernate())
                .because("Hibernate tworzy obiekty refleksją; konstruktor protected chroni przed użyciem go w kodzie biznesowym")
                .allowEmptyShould(true)
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R3.03 Pola encji nie są publiczne")
    void entityFieldsAreNotPublic() {
        fields().that().areDeclaredInClassesThat().areAnnotatedWith(JpaModel.ENTITY)
                .and().areNotStatic()
                .should().notBePublic()
                .because("stan encji zmieniamy przez metody, które pilnują reguł biznesowych")
                .allowEmptyShould(true)
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R3.04 Każda encja ma @Table z jawną nazwą w snake_case")
    void entitiesHaveExplicitSnakeCaseTable() {
        classes().that().areAnnotatedWith(JpaModel.ENTITY)
                .should(haveSnakeCaseTableName())
                .because("nazwy tabel to kontrakt z bazą i z data.sql, a nie przypadkowy efekt nazwy klasy, nie przypadkowy efekt nazwy klasy;"
                        + " unikamy też słów zastrzeżonych SQL, np. user, order")
                .allowEmptyShould(true)
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R3.05 Relacje @ManyToOne i @OneToOne mają fetch = LAZY")
    void toOneRelationsAreLazy() {
        List<String> eager = JpaModel.persistentFields()
                .filter(f -> isEager(f))
                .map(JpaModel::describe)
                .toList();

        assertThat(eager)
                .as("Relacje *ToOne są domyślnie EAGER, co prowadzi do problemu N+1. Dodaj fetch = FetchType.LAZY")
                .isEmpty();
    }

    @Test
    @DisplayName("R3.06 Pola typu enum mają @Enumerated(EnumType.STRING)")
    void enumsAreStoredAsStrings() {
        List<String> wrong = JpaModel.persistentFields()
                .filter(f -> f.getRawType().isEnum())
                .filter(f -> f.tryGetAnnotationOfType(Enumerated.class)
                        .map(e -> e.value() != EnumType.STRING)
                        .orElse(true))
                .map(JpaModel::describe)
                .toList();

        assertThat(wrong)
                .as("ORDINAL zapisuje numer pozycji: dodanie nowej wartości w środku enuma psuje dane w bazie")
                .isEmpty();
    }

    @Test
    @DisplayName("R3.07 Kwoty i ilości bez float/double: używamy BigDecimal")
    void noFloatingPointInModel() {
        Set<String> forbidden = Set.of("float", "double", Float.class.getName(), Double.class.getName());
        List<String> wrong = JpaModel.persistentFields()
                .filter(f -> forbidden.contains(f.getRawType().getName()))
                .map(JpaModel::describe)
                .toList();

        assertThat(wrong)
                .as("0.1 + 0.2 != 0.3. W projekcie liczymy pieniądze, więc używamy " + BigDecimal.class.getSimpleName())
                .isEmpty();
    }

    @Test
    @DisplayName("R3.08 Identyfikatory są typu Long albo UUID")
    void idsAreLongOrUuid() {
        Set<String> allowed = Set.of(Long.class.getName(), UUID.class.getName());
        List<String> wrong = JpaModel.fieldsAnnotatedWith(JpaModel.ID)
                .filter(f -> !allowed.contains(f.getRawType().getName()))
                .map(f -> JpaModel.describe(f) + " : " + f.getRawType().getSimpleName())
                .toList();

        assertThat(wrong)
                .as("Typ prosty long/int ma wartość 0, nie null, więc Hibernate nie odróżni nowej encji od zapisanej")
                .isEmpty();
    }

    @Test
    @DisplayName("R3.09 Jest value object: klasa @Embeddable użyta jako pole encji")
    void hasEmbeddedValueObject() {
        assertThat(JpaModel.embeddables()).as("Dodaj @Embeddable, np. Money, Address albo TimeSlot").isNotEmpty();
        assertThat(JpaModel.persistentFields()
                .filter(f -> f.getRawType().isAnnotatedWith(JpaModel.EMBEDDABLE))
                .map(JpaModel::describe)
                .toList())
                .as("Użyj klasy @Embeddable jako pola encji (@Embedded)")
                .isNotEmpty();
    }

    private static boolean isEager(JavaField field) {
        if (field.isAnnotatedWith(ManyToOne.class)) {
            return field.getAnnotationOfType(ManyToOne.class).fetch() != FetchType.LAZY;
        }
        if (field.isAnnotatedWith(OneToOne.class)) {
            return field.getAnnotationOfType(OneToOne.class).fetch() != FetchType.LAZY;
        }
        return false;
    }

    private static ArchCondition<JavaClass> haveNoArgConstructorVisibleToHibernate() {
        return new ArchCondition<>("have a public or protected no-arg constructor") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean ok = javaClass.getConstructors().stream()
                        .filter(c -> c.getRawParameterTypes().isEmpty())
                        .anyMatch(c -> c.getModifiers().contains(JavaModifier.PUBLIC)
                                || c.getModifiers().contains(JavaModifier.PROTECTED));
                events.add(new SimpleConditionEvent(javaClass, ok,
                        javaClass.getName() + (ok ? " ma" : " nie ma") + " konstruktora bezargumentowego public/protected"));
            }
        };
    }

    private static ArchCondition<JavaClass> haveSnakeCaseTableName() {
        return new ArchCondition<>("have @Table(name = snake_case)") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String name = javaClass.tryGetAnnotationOfType(Table.class).map(Table::name).orElse("");
                boolean ok = name.matches("[a-z][a-z0-9]*(_[a-z0-9]+)*")
                        && !Set.of("user", "order", "group", "table", "select", "role").contains(name);
                events.add(new SimpleConditionEvent(javaClass, ok,
                        javaClass.getName() + ": @Table(name = \"" + name + "\")"
                                + (ok ? "" : " – wymagana jawna nazwa w snake_case, która nie jest słowem zastrzeżonym SQL")));
            }
        };
    }
}
