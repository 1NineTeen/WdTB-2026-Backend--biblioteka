package pl.wsb.rules.lab02;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Autotest reguł LAB 02 (nie dotyczy projektu zespołu): dowód, że reguły przechodzą dla poprawnego modelu
 * (przykład kawiarni z instrukcji LAB 02 w pakiecie {@code sample}) i wskazują błędy w celowo złym modelu
 * ({@code counterexample}). Działa niezależnie od numeru laboratorium.
 */
@DisplayName("Autotest reguł LAB 02 · przykład kawiarni")
class Lab02RulesSelfTest {

    private static final String SAMPLE = "pl.wsb.rules.lab02.sample";
    private static final String COUNTEREXAMPLE = "pl.wsb.rules.lab02.counterexample";

    private static JpaModel model(String rootPackage) {
        return JpaModel.of(new ClassFileImporter().importPackages(rootPackage), rootPackage);
    }

    @Test
    @DisplayName("Przykład kawiarni spełnia R2.01–R2.09")
    void sampleSatisfiesStaticRules() {
        JpaModel sample = model(SAMPLE);
        assertThatCode(() -> {
            Lab02Checks.atLeastFourOwnEntities(sample);
            Lab02Checks.entitiesLiveInModuleDomainPackages(sample);
            Lab02Checks.atLeastTwoModules(sample);
            Lab02Checks.manyToOneWithJoinColumn(sample);
            Lab02Checks.manyToManyRelation(sample);
            Lab02Checks.someEntityReferencesUserAccount(sample);
            Lab02Checks.enumsStoredAsString(sample);
            Lab02Checks.noFloatingPointColumns(sample);
            Lab02Checks.oneToManyIsInverseSide(sample);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Celowo błędny model jest wskazywany z czytelnym komunikatem")
    void counterexampleIsRejected() {
        JpaModel bad = model(COUNTEREXAMPLE);
        assertThatThrownBy(() -> Lab02Checks.atLeastFourOwnEntities(bad))
                .hasMessageContaining("co najmniej 4 własnych encji").hasMessageContaining("Order");
        assertThatThrownBy(() -> Lab02Checks.entitiesLiveInModuleDomainPackages(bad))
                .hasMessageContaining("<moduł>.domain").hasMessageContaining("counterexample.shop");
        assertThatThrownBy(() -> Lab02Checks.atLeastTwoModules(bad))
                .hasMessageContaining("[shop]");
        assertThatThrownBy(() -> Lab02Checks.manyToOneWithJoinColumn(bad))
                .hasMessageContaining("@JoinColumn").hasMessageContaining("Order.customer");
        assertThatThrownBy(() -> Lab02Checks.manyToManyRelation(bad))
                .hasMessageContaining("wiele-do-wielu");
        assertThatThrownBy(() -> Lab02Checks.someEntityReferencesUserAccount(bad))
                .hasMessageContaining("UserAccount");
        assertThatThrownBy(() -> Lab02Checks.enumsStoredAsString(bad))
                .hasMessageContaining("EnumType.STRING").hasMessageContaining("Order.status");
        assertThatThrownBy(() -> Lab02Checks.noFloatingPointColumns(bad))
                .hasMessageContaining("BigDecimal").hasMessageContaining("Order.total").hasMessageContaining("Customer.discount");
        assertThatThrownBy(() -> Lab02Checks.oneToManyIsInverseSide(bad))
                .hasMessageContaining("mappedBy").hasMessageContaining("Order.lines");
    }
}
