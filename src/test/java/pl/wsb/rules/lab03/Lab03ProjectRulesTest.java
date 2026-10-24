package pl.wsb.rules.lab03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.SinceLab;

/**
 * Reguły LAB 03: relacje między encjami. Sprawdzenia czytają pliki .class (ArchUnit).
 */
@SinceLab(3)
@DisplayName("LAB 03 · Relacje")
class Lab03ProjectRulesTest {

    @Test
    @DisplayName("R3.01 Relacja wiele-do-jednego: @ManyToOne, każda z @JoinColumn(name = \"…_id\")")
    void manyToOneWithJoinColumn() {
        Lab03Checks.manyToOneWithJoinColumn(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.02 Relacja wiele-do-wielu: encja łącząca z dwiema @ManyToOne albo @ManyToMany")
    void manyToManyRelation() {
        Lab03Checks.manyToManyRelation(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.03 Co najmniej jedna własna encja wskazuje na konto użytkownika (UserAccount)")
    void someEntityReferencesUserAccount() {
        Lab03Checks.someEntityReferencesUserAccount(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.04 @OneToMany jest stroną odwrotną relacji: ma mappedBy")
    void oneToManyIsInverseSide() {
        Lab03Checks.oneToManyIsInverseSide(JpaModel.ofProject());
    }
}
