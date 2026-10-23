package pl.wsb.rules.lab02;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sprawdzenia reguł LAB 02 na modelu {@link JpaModel}. Oddzielone od klasy testowej,
 * żeby ten sam kod działał na projekcie zespołu i na przykładzie kawiarni w {@code sample}.
 */
final class Lab02Checks {

    static final int MIN_OWN_ENTITIES = 4;
    static final int MIN_MODULES = 2;

    private static final Set<String> FLOATING_POINT = Set.of("double", "float", "java.lang.Double", "java.lang.Float");

    private Lab02Checks() {
    }

    /** R2.01 */
    static void atLeastFourOwnEntities(JpaModel model) {
        List<JavaClass> own = model.ownEntities();
        assertThat(own)
                .withFailMessage("Potrzeba co najmniej %d własnych encji (@Entity) poza UserAccount, jest %d: %s. "
                                + "Każda tabela z Waszego modelu danych to klasa z @Entity w pakiecie %s.<moduł>.domain.",
                        MIN_OWN_ENTITIES, own.size(), JpaModel.names(own), model.rootPackage())
                .hasSizeGreaterThanOrEqualTo(MIN_OWN_ENTITIES);
    }

    /** R2.02 */
    static void entitiesLiveInModuleDomainPackages(JpaModel model) {
        List<String> misplaced = new ArrayList<>();
        for (JavaClass c : model.mappedClasses()) {
            boolean ok = model.moduleOf(c).isPresent() && model.layerOf(c).filter("domain"::equals).isPresent();
            if (!ok) {
                misplaced.add(c.getSimpleName() + " (" + c.getPackageName() + ")");
            }
        }
        assertThat(misplaced)
                .withFailMessage("Encje i klasy @Embeddable leżą w pakietach %s.<moduł>.domain, np. %s.booking.domain. "
                                + "Przenieście (IntelliJ: Refactor → Move): %s",
                        model.rootPackage(), model.rootPackage(), misplaced)
                .isEmpty();
    }

    /** R2.03 */
    static void atLeastTwoModules(JpaModel model) {
        Set<String> modules = new TreeSet<>();
        model.ownEntities().forEach(c -> model.moduleOf(c).ifPresent(modules::add));
        assertThat(modules)
                .withFailMessage("Projekt ma co najmniej %d moduły z własnymi encjami (patrz Koncepcja projektu), teraz: %s. "
                                + "Moduł to pakiet %s.<moduł>, np. seating (stoliki) i booking (rezerwacje); "
                                + "każdy zarządza własnymi tabelami.",
                        MIN_MODULES, modules, model.rootPackage())
                .hasSizeGreaterThanOrEqualTo(MIN_MODULES);
    }

    /** R2.04 */
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

    /** R2.05 */
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

    /** R2.06 */
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

    /** R2.07 */
    static void enumsStoredAsString(JpaModel model) {
        List<String> offenders = new ArrayList<>();
        for (JavaClass c : model.mappedClasses()) {
            if (JpaModel.isUserAccount(c)) {
                continue;
            }
            for (JavaField f : JpaModel.fields(c)) {
                if (!f.getRawType().isEnum()) {
                    continue;
                }
                boolean asString = f.tryGetAnnotationOfType(Enumerated.class)
                        .map(e -> e.value() == EnumType.STRING)
                        .orElse(false);
                if (!asString) {
                    offenders.add(JpaModel.where(f));
                }
            }
        }
        assertThat(offenders)
                .withFailMessage("Enumy w bazie zapisujemy jako tekst, nie numer pozycji (po zmianie kolejności stałych dane "
                        + "przestałyby się zgadzać). Dopiszcie @Enumerated(EnumType.STRING) nad polami: %s", offenders)
                .isEmpty();
    }

    /** R2.08 */
    static void noFloatingPointColumns(JpaModel model) {
        List<String> offenders = new ArrayList<>();
        for (JavaClass c : model.mappedClasses()) {
            if (JpaModel.isUserAccount(c)) {
                continue;
            }
            for (JavaField f : JpaModel.fields(c)) {
                if (FLOATING_POINT.contains(f.getRawType().getName())) {
                    offenders.add(JpaModel.where(f) + " (" + f.getRawType().getSimpleName() + ")");
                }
            }
        }
        assertThat(offenders)
                .withFailMessage("Kwoty i inne wielkości dziesiętne przechowujemy w BigDecimal (double i float zaokrąglają: "
                        + "0.1 + 0.2 ≠ 0.3), a ilości w Integer. Zmieńcie typ pól: %s", offenders)
                .isEmpty();
    }

    /** R2.09 */
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
