package pl.wsb.rules.lab02;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import pl.wsb.rules.core.ProjectApp;
import pl.wsb.rules.core.SinceLab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reguły LAB 02: model danych projektu (encje JPA, relacje, typy, dane startowe).
 * Sprawdzenia R2.01–R2.09 czytają pliki .class (ArchUnit), R2.10 uruchamia aplikację na czystej bazie H2.
 */
@SinceLab(2)
@DisplayName("LAB 02 · Encje i relacje")
class Lab02ProjectRulesTest {

    @Test
    @DisplayName("R2.01 Co najmniej 4 własne encje (@Entity) poza UserAccount")
    void atLeastFourOwnEntities() {
        Lab02Checks.atLeastFourOwnEntities(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.02 Encje i @Embeddable leżą w pakietach pl.wsb.<projekt>.<moduł>.domain")
    void entitiesLiveInModuleDomainPackages() {
        Lab02Checks.entitiesLiveInModuleDomainPackages(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.03 Co najmniej 2 moduły z własnymi encjami")
    void atLeastTwoModules() {
        Lab02Checks.atLeastTwoModules(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.04 Relacja wiele-do-jednego: @ManyToOne, każda z @JoinColumn(name = \"…_id\")")
    void manyToOneWithJoinColumn() {
        Lab02Checks.manyToOneWithJoinColumn(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.05 Relacja wiele-do-wielu: encja łącząca z dwiema @ManyToOne albo @ManyToMany")
    void manyToManyRelation() {
        Lab02Checks.manyToManyRelation(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.06 Co najmniej jedna własna encja wskazuje na konto użytkownika (UserAccount)")
    void someEntityReferencesUserAccount() {
        Lab02Checks.someEntityReferencesUserAccount(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.07 Enumy w encjach zapisywane jako tekst: @Enumerated(EnumType.STRING)")
    void enumsStoredAsString() {
        Lab02Checks.enumsStoredAsString(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.08 Kwoty w BigDecimal: w encjach nie ma pól double ani float")
    void noFloatingPointColumns() {
        Lab02Checks.noFloatingPointColumns(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.09 @OneToMany jest stroną odwrotną relacji: ma mappedBy")
    void oneToManyIsInverseSide() {
        Lab02Checks.oneToManyIsInverseSide(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.10 Dane startowe: aplikacja startuje, a każda własna tabela ma wiersze z data.sql")
    void everyOwnTableHasSeedData() {
        List<String> empty = new ArrayList<>();
        List<String> seeded = new ArrayList<>();
        try (ConfigurableApplicationContext context = ProjectApp.startWithHibernateSchema();
             EntityManager em = ProjectApp.entityManagerFactory(context).createEntityManager()) {
            List<EntityType<?>> entities = em.getMetamodel().getEntities().stream()
                    .filter(e -> !JpaModel.USER_ACCOUNT.equals(e.getJavaType().getSimpleName()))
                    .sorted(Comparator.comparing(EntityType::getName))
                    .toList();
            for (EntityType<?> entity : entities) {
                long rows = count(em, entity);
                (rows > 0 ? seeded : empty).add(tableName(entity) + " (" + entity.getName() + ")");
            }
        }
        assertThat(empty)
                .withFailMessage("Tabele bez danych startowych: %s. Dopiszcie do src/main/resources/data.sql co najmniej jeden "
                                + "insert do każdej z nich (najpierw tabele, na które wskazują klucze obce). Tabele z danymi: %s",
                        empty, seeded)
                .isEmpty();
    }

    private static long count(EntityManager em, EntityType<?> entity) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        query.select(cb.count(query.from(entity)));
        return em.createQuery(query).getSingleResult();
    }

    /** Nazwa tabeli z @Table albo domyślna Spring Boota (snake_case nazwy klasy). */
    private static String tableName(EntityType<?> entity) {
        Table table = entity.getJavaType().getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }
        return entity.getJavaType().getSimpleName()
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase();
    }
}
