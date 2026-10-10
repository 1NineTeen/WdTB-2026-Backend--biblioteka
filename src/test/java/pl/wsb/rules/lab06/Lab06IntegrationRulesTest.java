package pl.wsb.rules.lab06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.ArchNames;
import pl.wsb.rules.core.Project;
import pl.wsb.rules.core.SinceLab;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

@SinceLab(6)
@DisplayName("LAB 06 · Zdarzenia, konfiguracja, integracje")
class Lab06IntegrationRulesTest {

    @Test
    @DisplayName("R6.01 Konfiguracja biznesowa w rekordzie @ConfigurationProperties")
    void hasTypedConfiguration() {
        assertThat(Project.productionClasses().stream()
                .filter(c -> c.isAnnotatedWith(ArchNames.CONFIGURATION_PROPERTIES))
                .filter(c -> c.isRecord()))
                .as("np. record PricingProperties(BigDecimal commission, Duration reservationTtl)")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R6.02 Moduły reagują na zdarzenia: rekord zdarzenia w <moduł>.api i listener w innym module")
    void modulesCommunicateWithEvents() {
        boolean listener = Project.productionClasses().stream()
                .flatMap(c -> c.getMethods().stream())
                .anyMatch(m -> m.isAnnotatedWith(ArchNames.EVENT_LISTENER)
                        || m.isAnnotatedWith(ArchNames.TX_EVENT_LISTENER)
                        || m.isAnnotatedWith(ArchNames.APPLICATION_MODULE_LISTENER));
        boolean eventRecord = Project.productionClasses().stream()
                .anyMatch(c -> c.isRecord() && c.getSimpleName().endsWith("Event")
                        && Project.layerOf(c).filter("api"::equals).isPresent());
        assertThat(listener).as("Brak @EventListener / @TransactionalEventListener").isTrue();
        assertThat(eventRecord).as("Brak rekordu *Event w pakiecie api").isTrue();
    }

    @Test
    @DisplayName("R6.03 Zadanie cykliczne @Scheduled (np. wygaszanie rezerwacji)")
    void hasScheduledJob() {
        assertThat(Project.productionClasses().stream()
                .flatMap(c -> c.getMethods().stream())
                .filter(m -> m.isAnnotatedWith(ArchNames.SCHEDULED)))
                .as("Dodaj metodę @Scheduled i włącz @EnableScheduling")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R6.04 Klienci HTTP do usług zewnętrznych tylko w <moduł>.infrastructure")
    void httpClientsInInfrastructure() {
        noClasses().that().resideOutsideOfPackage(Project.rootPackage() + ".*.infrastructure..")
                .and().resideOutsideOfPackage(Project.rootPackage() + ".config..")
                .should().dependOnClassesThat().haveFullyQualifiedName(ArchNames.REST_CLIENT)
                .orShould().dependOnClassesThat().haveFullyQualifiedName(ArchNames.REST_TEMPLATE)
                .check(Project.productionClasses());
    }
}
