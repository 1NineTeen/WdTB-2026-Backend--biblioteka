package pl.wsb.rules.lab02;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.JpaModel;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Autotest reguł LAB 02 (nie dotyczy projektu zespołu): dowód, że reguły przechodzą dla poprawnego modelu
 * (przykład kawiarni w {@code pl.wsb.rules.examples.cafe}) i wskazują błędy w celowo złym modelu
 * ({@code pl.wsb.rules.examples.broken}). Działa niezależnie od numeru laboratorium.
 */
@DisplayName("Autotest reguł LAB 02 · przykład kawiarni")
class Lab02RulesSelfTest {

    private static final String CAFE = "pl.wsb.rules.examples.cafe";
    private static final String BROKEN = "pl.wsb.rules.examples.broken";
    private static final String NOTHING = "pl.wsb.rules.examples.nothing";

    private static JpaModel model(String rootPackage) {
        return JpaModel.of(new ClassFileImporter().importPackages(rootPackage), rootPackage);
    }

    @Test
    @DisplayName("Przykład kawiarni spełnia R2.01–R2.04")
    void cafeSatisfiesStaticRules() {
        JpaModel cafe = model(CAFE);
        assertThatCode(() -> {
            Lab02Checks.atLeastThreeOwnEntities(cafe);
            Lab02Checks.entitiesLiveInModuleDomainPackages(cafe);
            Lab02Checks.enumsStoredAsString(cafe);
            Lab02Checks.noFloatingPointColumns(cafe);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Celowo błędny model jest wskazywany z czytelnym komunikatem")
    void brokenModelIsRejected() {
        JpaModel broken = model(BROKEN);
        assertThatThrownBy(() -> Lab02Checks.atLeastThreeOwnEntities(model(NOTHING)))
                .hasMessageContaining("co najmniej 3 własnych encji");
        assertThatThrownBy(() -> Lab02Checks.entitiesLiveInModuleDomainPackages(broken))
                .hasMessageContaining("<moduł>.domain").hasMessageContaining("broken.shop");
        assertThatThrownBy(() -> Lab02Checks.enumsStoredAsString(broken))
                .hasMessageContaining("EnumType.STRING").hasMessageContaining("Order.status");
        assertThatThrownBy(() -> Lab02Checks.noFloatingPointColumns(broken))
                .hasMessageContaining("BigDecimal").hasMessageContaining("Order.total").hasMessageContaining("Customer.discount");
    }
}
