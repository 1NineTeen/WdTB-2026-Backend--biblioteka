package pl.wsb.rules.lab02;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import pl.wsb.rules.core.JpaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sprawdzenia reguł LAB 02 (encje, pakiety, typy kolumn) na modelu {@link JpaModel}. Oddzielone od klasy testowej,
 * żeby ten sam kod działał na projekcie zespołu i na przykładach w {@code pl.wsb.rules.examples}.
 */
final class Lab02Checks {

    static final int MIN_OWN_ENTITIES = 3;

    private static final Set<String> FLOATING_POINT = Set.of("double", "float", "java.lang.Double", "java.lang.Float");

    private Lab02Checks() {
    }

    /** R2.01 */
    static void atLeastThreeOwnEntities(JpaModel model) {
        List<JavaClass> own = model.ownEntities();
        assertThat(own)
                .withFailMessage("Potrzeba co najmniej %d własnych encji (@Entity) poza UserAccount, jest %d: %s. "
                                + "Każda tabela to klasa z @Entity w pakiecie %s.<moduł>.domain.",
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

    /** R2.04 */
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
}
