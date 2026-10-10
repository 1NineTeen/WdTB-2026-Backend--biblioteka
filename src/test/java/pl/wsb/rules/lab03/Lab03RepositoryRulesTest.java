package pl.wsb.rules.lab03;

import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import pl.wsb.rules.core.ArchNames;
import pl.wsb.rules.core.Project;
import pl.wsb.rules.core.SinceLab;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

@SinceLab(3)
@DisplayName("LAB 03 · Repozytoria i zapytania")
class Lab03RepositoryRulesTest {

    private static final String SPRING_DATA_REPOSITORY = "org.springframework.data.repository.Repository";
    private static final String SPRING_BOOT_TEST = "org.springframework.boot.test.context.SpringBootTest";

    @Test
    @DisplayName("R3.10 Repozytoria to interfejsy *Repository w pakiecie <moduł>.domain")
    void repositoriesFollowConvention() {
        classes().that().areAssignableTo(SPRING_DATA_REPOSITORY)
                .and().doNotHaveFullyQualifiedName(SPRING_DATA_REPOSITORY)
                .should().beInterfaces()
                .andShould().haveSimpleNameEndingWith("Repository")
                .andShould().resideInAPackage(Project.rootPackage() + ".*.domain..")
                .because("repozytorium to kolekcja obiektów domeny, więc mieszka obok encji, którymi zarządza")
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R3.11 Każde repozytorium ma własny test <Nazwa>Test z @DataJpaTest")
    void everyRepositoryIsTested() {
        Set<String> testedNames = Project.teamTestClasses().stream()
                .filter(t -> ArchNames.isAnnotatedWithAny(t, ArchNames.DATA_JPA_TEST) || t.isAnnotatedWith(SPRING_BOOT_TEST))
                .map(JavaClass::getSimpleName)
                .collect(Collectors.toSet());
        List<String> untested = repositories().stream()
                .map(JavaClass::getSimpleName)
                .filter(name -> !testedNames.contains(name + "Test") && !testedNames.contains(name + "Tests"))
                .toList();
        assertThat(untested).as("Repozytoria bez testu @DataJpaTest").isEmpty();
    }

    @Test
    @DisplayName("R3.12 Zapytania pochodne: co najmniej 3 metody findBy/countBy/existsBy…")
    void hasDerivedQueries() {
        List<String> derived = repositoryMethods()
                .filter(m -> !m.isAnnotationPresent(Query.class))
                .filter(m -> m.getName().matches("(find|read|get|query|search|stream|count|exists|delete|remove)\\w*By\\w+"))
                .map(Lab03RepositoryRulesTest::describe)
                .toList();
        assertThat(derived).as("Zapytania pochodne z nazwy metody").hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("R3.13 JPQL: co najmniej 2 metody @Query, w tym jedna z agregacją (GROUP BY)")
    void hasJpqlQueriesWithAggregation() {
        List<String> queries = repositoryMethods()
                .filter(m -> m.isAnnotationPresent(Query.class))
                .map(m -> m.getAnnotation(Query.class).value().toLowerCase(Locale.ROOT).replaceAll("\\s+", " "))
                .toList();
        assertThat(queries).as("Metody @Query").hasSizeGreaterThanOrEqualTo(2);
        assertThat(queries).as("Pytanie inwestora wymaga agregacji: użyj count/sum/avg … group by")
                .anyMatch(q -> q.contains("group by"));
    }

    @Test
    @DisplayName("R3.14 Stronicowanie: metoda przyjmuje Pageable i zwraca Page albo Slice")
    void hasPagination() {
        assertThat(repositoryMethods()
                .filter(m -> Arrays.asList(m.getParameterTypes()).contains(Pageable.class))
                .filter(m -> Page.class.isAssignableFrom(m.getReturnType()) || Slice.class.isAssignableFrom(m.getReturnType()))
                .map(Lab03RepositoryRulesTest::describe)
                .toList())
                .as("Lista, która rośnie bez końca, musi być stronicowana")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R3.15 Projekcja: zapytanie zwraca rekord albo interfejs zamiast encji")
    void hasProjection() {
        assertThat(repositoryMethods()
                .filter(m -> returnedTypes(m.getGenericReturnType()).anyMatch(Lab03RepositoryRulesTest::isProjection))
                .map(Lab03RepositoryRulesTest::describe)
                .toList())
                .as("Pobierz tylko potrzebne kolumny, np. List<OfferSummary> gdzie OfferSummary to record")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R3.16 N+1 rozwiązany jawnie: @EntityGraph albo JOIN FETCH")
    void hasFetchPlan() {
        assertThat(repositoryMethods()
                .filter(m -> m.isAnnotationPresent(EntityGraph.class)
                        || (m.isAnnotationPresent(Query.class)
                        && m.getAnnotation(Query.class).value().toLowerCase(Locale.ROOT).matches("(?s).*join\\s+fetch.*")))
                .map(Lab03RepositoryRulesTest::describe)
                .toList())
                .as("Co najmniej jedno zapytanie ładuje relację razem z encją")
                .isNotEmpty();
    }

    private static List<JavaClass> repositories() {
        return Project.productionClasses().stream()
                .filter(c -> c.isInterface() && c.isAssignableTo(SPRING_DATA_REPOSITORY))
                .filter(c -> c.getPackageName().startsWith(Project.rootPackage()))
                .toList();
    }

    private static Stream<Method> repositoryMethods() {
        return repositories().stream()
                .map(JavaClass::reflect)
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                .filter(m -> !m.isSynthetic());
    }

    private static Stream<Class<?>> returnedTypes(Type type) {
        if (type instanceof Class<?> c) {
            return Stream.of(c);
        }
        if (type instanceof ParameterizedType p) {
            return Stream.concat(Stream.of((Class<?>) p.getRawType()),
                    Arrays.stream(p.getActualTypeArguments()).flatMap(Lab03RepositoryRulesTest::returnedTypes));
        }
        return Stream.empty();
    }

    private static boolean isProjection(Class<?> type) {
        boolean own = type.getPackageName().startsWith(Project.rootPackage());
        boolean entity = type.isAnnotationPresent(jakarta.persistence.Entity.class);
        return own && !entity && (type.isRecord() || type.isInterface());
    }

    private static String describe(Method m) {
        return m.getDeclaringClass().getSimpleName() + "." + m.getName();
    }
}
