package pl.wsb.rules.lab03;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.JpaModel;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Autotest reguł LAB 03 (nie dotyczy projektu zespołu): reguły relacji przechodzą dla przykładu kawiarni
 * ({@code pl.wsb.rules.examples.cafe}) i wskazują błędy w celowo złym modelu ({@code pl.wsb.rules.examples.broken}).
 */
@DisplayName("Autotest reguł LAB 03 · przykład kawiarni")
class Lab03RulesSelfTest {

    private static JpaModel model(String rootPackage) {
        return JpaModel.of(new ClassFileImporter().importPackages(rootPackage), rootPackage);
    }

    @Test
    @DisplayName("Przykład kawiarni spełnia R3.01–R3.04")
    void cafeSatisfiesRelationRules() {
        JpaModel cafe = model("pl.wsb.rules.examples.cafe");
        assertThatCode(() -> {
            Lab03Checks.manyToOneWithJoinColumn(cafe);
            Lab03Checks.manyToManyRelation(cafe);
            Lab03Checks.someEntityReferencesUserAccount(cafe);
            Lab03Checks.oneToManyIsInverseSide(cafe);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Celowo błędny model jest wskazywany z czytelnym komunikatem")
    void brokenModelIsRejected() {
        JpaModel broken = model("pl.wsb.rules.examples.broken");
        assertThatThrownBy(() -> Lab03Checks.manyToOneWithJoinColumn(broken))
                .hasMessageContaining("@JoinColumn").hasMessageContaining("Order.customer");
        assertThatThrownBy(() -> Lab03Checks.manyToManyRelation(broken))
                .hasMessageContaining("wiele-do-wielu");
        assertThatThrownBy(() -> Lab03Checks.someEntityReferencesUserAccount(broken))
                .hasMessageContaining("UserAccount");
        assertThatThrownBy(() -> Lab03Checks.oneToManyIsInverseSide(broken))
                .hasMessageContaining("mappedBy").hasMessageContaining("Order.lines");
    }
}
