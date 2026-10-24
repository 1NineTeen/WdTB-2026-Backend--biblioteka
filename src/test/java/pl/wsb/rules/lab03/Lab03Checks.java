package pl.wsb.rules.lab03;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import pl.wsb.rules.core.JpaModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sprawdzenia reguł LAB 03 (relacje między encjami) na modelu {@link JpaModel}. Oddzielone od klasy testowej,
 * żeby ten sam kod działał na projekcie zespołu i na przykładach w {@code pl.wsb.rules.examples}.
 */
final class Lab03Checks {

    private Lab03Checks() {
    }

    /** R3.01 */
    static void manyToOneWithJoinColumn(JpaModel model) {
        List<JavaField> manyToOne = model.ownFieldsAnnotatedWith(ManyToOne.class);
        assertThat(manyToOne)
                .withFailMessage("Brak relacji @ManyToOne we własnych encjach. Podstawowa relacja to pole typu encji nadrzędnej "
                        + "z @ManyToOne i @JoinColumn(name = \"<tabela>_id\"), np. w rezerwacji pole stolika: "
                        + "@ManyToOne @JoinColumn(name = \"table_id\") private CafeTable table;")
                .isNotEmpty();

        List<String> withoutJoinColumn = manyToOne.stream()
                .filter(f -> f.tryGetAnnotationOfType(JoinColumn.class).map(j -> j.name().isBlank()).orElse(true))
                .map(JpaModel::where)
                .toList();
        assertThat(withoutJoinColumn)
                .withFailMessage("Każde pole @ManyToOne ma @JoinColumn(name = \"…_id\"), żeby kolumna klucza obcego miała "
                        + "czytelną nazwę w bazie. Dopiszcie @JoinColumn w: %s", withoutJoinColumn)
                .isEmpty();
    }

    /** R3.02 */
    static void manyToManyRelation(JpaModel model) {
        List<JavaField> manyToMany = model.ownFieldsAnnotatedWith(ManyToMany.class);
        List<String> joinEntities = model.ownEntities().stream()
                .filter(c -> JpaModel.fields(c).stream().filter(f -> f.isAnnotatedWith(ManyToOne.class)).count() >= 2)
                .map(JavaClass::getSimpleName)
                .toList();
        assertThat(manyToMany.isEmpty() && joinEntities.isEmpty())
                .withFailMessage("Brak relacji wiele-do-wielu. Najlepiej zamodelujcie ją encją łączącą z dwiema relacjami @ManyToOne "
                        + "i własnymi atrybutami (np. ReservationExtra: rezerwacja + dodatek + ilość). "
                        + "Gdy relacja nie ma atrybutów, można użyć @ManyToMany z @JoinTable.")
                .isFalse();
    }

    /** R3.03 */
    static void someEntityReferencesUserAccount(JpaModel model) {
        List<String> references = model.ownEntities().stream()
                .flatMap(c -> JpaModel.fields(c).stream())
                .filter(f -> f.isAnnotatedWith(ManyToOne.class) || f.isAnnotatedWith(OneToOne.class))
                .filter(f -> JpaModel.isUserAccount(f.getRawType()))
                .map(JpaModel::where)
                .toList();
        assertThat(references)
                .withFailMessage("Żadna własna encja nie wskazuje na konto użytkownika. Encja osoby korzystającej z aplikacji "
                        + "(klient, gość, członek, uczestnik…) ma pole @ManyToOne albo @OneToOne typu UserAccount "
                        + "z @JoinColumn(name = \"user_account_id\"). Konto to logowanie, Wasza encja to dane biznesowe.")
                .isNotEmpty();
    }

    /** R3.04 */
    static void oneToManyIsInverseSide(JpaModel model) {
        List<String> offenders = model.ownFieldsAnnotatedWith(OneToMany.class).stream()
                .filter(f -> f.getAnnotationOfType(OneToMany.class).mappedBy().isBlank())
                .map(JpaModel::where)
                .toList();
        assertThat(offenders)
                .withFailMessage("@OneToMany to tylko strona odwrotna relacji: klucz obcy jest po stronie @ManyToOne. "
                        + "Dopiszcie mappedBy = \"<nazwa pola @ManyToOne w encji podrzędnej>\" w: %s "
                        + "(bez mappedBy Hibernate tworzy dodatkową tabelę łączącą albo kolumnę, której nie planowaliście).",
                        offenders)
                .isEmpty();
    }
}
