package pl.wsb.rules.lab05;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.ArchNames;
import pl.wsb.rules.core.Project;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.SinceLab;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

@SinceLab(5)
@DisplayName("LAB 05 · REST API")
class Lab05WebLayerRulesTest {

    @Test
    @DisplayName("R5.01 @RestController mieszka w <moduł>.web i nazywa się *Controller")
    void controllersLiveInWebLayer() {
        assertThat(Project.productionClasses().stream().filter(c -> c.isAnnotatedWith(ArchNames.REST_CONTROLLER)))
                .as("Brak klas @RestController")
                .isNotEmpty();
        classes().that().areAnnotatedWith(ArchNames.REST_CONTROLLER)
                .should().resideInAPackage(Project.rootPackage() + ".*.web..")
                .andShould().haveSimpleNameEndingWith("Controller")
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R5.02 Kontrolery nie znają repozytoriów ani encji")
    void controllersDoNotTouchPersistence() {
        noClasses().that().resideInAPackage(Project.rootPackage() + ".*.web..")
                .should().dependOnClassesThat().areAnnotatedWith(JpaModel.ENTITY)
                .orShould().dependOnClassesThat().areAssignableTo("org.springframework.data.repository.Repository")
                .because("encja w JSON to wyciek modelu: każda zmiana tabeli łamie klientów API")
                .allowEmptyShould(true)
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R5.03 DTO (*Request, *Response, *Dto) to rekordy")
    void dtosAreRecords() {
        classes().that().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .or().haveSimpleNameEndingWith("Dto")
                .should().beRecords()
                .allowEmptyShould(true)
                .check(Project.productionClasses());
    }

    @Test
    @DisplayName("R5.04 Jest globalna obsługa błędów @RestControllerAdvice (ProblemDetail)")
    void hasGlobalErrorHandling() {
        assertThat(Project.productionClasses().stream().filter(c -> c.isAnnotatedWith(ArchNames.CONTROLLER_ADVICE)))
                .as("Dodaj @RestControllerAdvice, który zamienia wyjątki domenowe na ProblemDetail (RFC 9457)")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R5.05 Każdy @RequestBody jest walidowany przez @Valid")
    void requestBodiesAreValidated() {
        List<String> notValidated = Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith(ArchNames.REST_CONTROLLER))
                .flatMap(c -> c.getMethods().stream())
                .flatMap(m -> m.getParameters().stream())
                .filter(p -> p.isAnnotatedWith(ArchNames.REQUEST_BODY))
                .filter(p -> !p.isAnnotatedWith(ArchNames.VALID) && !p.isAnnotatedWith(ArchNames.VALIDATED))
                .map(Lab05WebLayerRulesTest::describe)
                .toList();
        assertThat(notValidated).isEmpty();
    }

    @Test
    @DisplayName("R5.06 Każdy kontroler ma test <Nazwa>Test (@WebMvcTest albo @SpringBootTest)")
    void everyControllerIsTested() {
        Set<String> tests = Project.teamTestClasses().stream()
                .filter(t -> ArchNames.isAnnotatedWithAny(t, ArchNames.WEB_MVC_TEST) || t.isAnnotatedWith(ArchNames.SPRING_BOOT_TEST))
                .map(JavaClass::getSimpleName)
                .collect(Collectors.toSet());
        assertThat(Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith(ArchNames.REST_CONTROLLER))
                .map(JavaClass::getSimpleName)
                .filter(n -> !tests.contains(n + "Test") && !tests.contains(n + "Tests"))
                .toList())
                .as("Kontrolery bez testu")
                .isEmpty();
    }

    @Test
    @DisplayName("R5.07 API ma co najmniej 8 endpointów, w tym POST, PUT/PATCH i DELETE")
    void apiIsComplete() {
        List<String> mappings = Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith(ArchNames.REST_CONTROLLER))
                .flatMap(c -> c.getMethods().stream())
                .flatMap(m -> m.getAnnotations().stream())
                .map(a -> a.getRawType().getSimpleName())
                .filter(n -> n.endsWith("Mapping") && !n.equals("RequestMapping"))
                .toList();
        assertThat(mappings).as("Endpointy (@GetMapping, @PostMapping, …)").hasSizeGreaterThanOrEqualTo(8);
        assertThat(mappings).as("Tworzenie zasobu").contains("PostMapping");
        assertThat(mappings).as("Aktualizacja zasobu").containsAnyOf("PutMapping", "PatchMapping");
        assertThat(mappings).as("Usuwanie zasobu").contains("DeleteMapping");
    }

    private static String describe(JavaParameter parameter) {
        JavaMethod method = (JavaMethod) parameter.getOwner();
        return method.getOwner().getSimpleName() + "." + method.getName();
    }
}
