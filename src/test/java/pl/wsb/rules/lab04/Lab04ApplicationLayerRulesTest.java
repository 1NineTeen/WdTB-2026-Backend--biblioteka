package pl.wsb.rules.lab04;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.ArchNames;
import pl.wsb.rules.core.Project;
import pl.wsb.rules.core.SinceLab;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

@SinceLab(4)
@DisplayName("LAB 04 · Serwisy, komponenty, moduły")
class Lab04ApplicationLayerRulesTest {

    @Test
    @DisplayName("R4.01 @Service mieszka w <moduł>.application")
    void servicesLiveInApplicationLayer() {
        assertThat(Project.productionClasses().stream().filter(c -> c.isAnnotatedWith(ArchNames.SERVICE)))
                .as("Brak klas @Service. Każdy moduł ma co najmniej jeden serwis przypadków użycia")
                .isNotEmpty();
        classes().that().areAnnotatedWith(ArchNames.SERVICE)
                .should().resideInAPackage(Project.rootPackage() + ".*.application..")
                .because("logika przypadków użycia ma jedno, przewidywalne miejsce w każdym module")
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R4.02 Wstrzykiwanie zależności tylko przez konstruktor")
    void noFieldInjection() {
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION
                .because("konstruktor pokazuje zależności i pozwala testować klasę bez Springa")
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R4.03 Moduły rozmawiają ze sobą tylko przez <moduł>.api")
    void modulesTalkThroughApiOnly() {
        classes().that().resideInAPackage(Project.rootPackage() + ".*..")
                .should(onlyDependOnOtherModulesThroughTheirApi())
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R4.04 Brak cykli zależności między modułami")
    void noCyclesBetweenModules() {
        slices().matching(Project.rootPackage() + ".(*)..")
                .should().beFreeOfCycles()
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R4.05 @Transactional tylko w warstwie application")
    void transactionsBelongToApplicationLayer() {
        noClasses().that().resideOutsideOfPackage(Project.rootPackage() + ".*.application..")
                .should().beAnnotatedWith(ArchNames.TRANSACTIONAL)
                .orShould().beAnnotatedWith(ArchNames.JAKARTA_TRANSACTIONAL)
                .because("granica transakcji to granica przypadku użycia")
                .check(Project.productionClasses());
        methods().that().areDeclaredInClassesThat().resideOutsideOfPackage(Project.rootPackage() + ".*.application..")
                .should().notBeAnnotatedWith(ArchNames.TRANSACTIONAL)
                .allowEmptyShould(true)
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R4.06 Domena nie zależy od warstw application, web i infrastructure")
    void domainIsIndependent() {
        noClasses().that().resideInAPackage(Project.rootPackage() + ".*.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        Project.rootPackage() + ".*.application..",
                        Project.rootPackage() + ".*.web..",
                        Project.rootPackage() + ".*.infrastructure..",
                        "org.springframework.web..")
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R4.07 Bez System.out, java.util.logging i wyjątków ogólnych")
    void generalHygiene() {
        GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(Project.productionClasses());
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(Project.productionClasses());
        GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(Project.productionClasses());
    }

    @Test
    @DisplayName("R4.08 Każdy @Service ma test jednostkowy <Nazwa>Test")
    void everyServiceIsTested() {
        Set<String> tests = Project.teamTestClasses().stream().map(JavaClass::getSimpleName).collect(Collectors.toSet());
        List<String> untested = Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith(ArchNames.SERVICE))
                .map(JavaClass::getSimpleName)
                .filter(n -> !tests.contains(n + "Test") && !tests.contains(n + "Tests"))
                .toList();
        assertThat(untested).as("Serwisy bez testu").isEmpty();
    }

    @Test
    @DisplayName("R4.09 Każdy moduł z encjami ma co najmniej jeden @Service")
    void everyModuleHasService() {
        Set<String> withEntities = Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith("jakarta.persistence.Entity"))
                .map(Project::moduleOf).flatMap(Optional::stream)
                .filter(m -> !m.equals("account") && !Project.SHARED_MODULES.contains(m))
                .collect(Collectors.toSet());
        Set<String> withServices = Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith(ArchNames.SERVICE))
                .map(Project::moduleOf).flatMap(Optional::stream)
                .collect(Collectors.toSet());
        withEntities.removeAll(withServices);
        assertThat(withEntities).as("Moduły bez serwisu").isEmpty();
    }

    private static ArchCondition<JavaClass> onlyDependOnOtherModulesThroughTheirApi() {
        return new ArchCondition<>("depend on other modules only through their api package") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                Optional<String> originModule = Project.moduleOf(origin);
                if (originModule.isEmpty()) {
                    return;
                }
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    Optional<String> targetModule = Project.moduleOf(target);
                    if (targetModule.isEmpty()
                            || targetModule.equals(originModule)
                            || Project.SHARED_MODULES.contains(targetModule.get())) {
                        continue;
                    }
                    boolean throughApi = Project.layerOf(target).filter("api"::equals).isPresent();
                    if (!throughApi) {
                        events.add(SimpleConditionEvent.violated(dependency,
                                dependency.getDescription() + " – moduł '" + originModule.get()
                                        + "' sięga do wnętrza modułu '" + targetModule.get() + "'. Użyj jego pakietu api."));
                    }
                }
            }
        };
    }
}
