package pl.wsb.rules.lab02;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.wsb.rules.core.DatabaseSchema;
import pl.wsb.rules.core.DatabaseSchema.ForeignKey;
import pl.wsb.rules.core.ProjectApp;
import pl.wsb.rules.core.SinceLab;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * LAB 01 sprawdza wyłącznie bazę danych, którą Hibernate zbudował z Waszych encji:
 * dokładnie to, co widzicie w konsoli H2. Bez analizy kodu.
 */
@SinceLab(2)
@DisplayName("LAB 02 · Schemat bazy danych")
class Lab02DatabaseSchemaTest {

    private static ConfigurableApplicationContext context;
    private static DatabaseSchema schema;
    private static Throwable startupError;

    @BeforeAll
    static void startApplication() {
        try {
            context = ProjectApp.startWithHibernateSchema();
            schema = DatabaseSchema.read(context.getBean(DataSource.class));
            System.out.println("[reguły] Tabele: " + schema.tables());
        } catch (Throwable e) {
            startupError = e;
        }
    }

    @AfterAll
    static void stopApplication() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @DisplayName("R2.01 Aplikacja startuje, a Hibernate tworzy tabele z encji bez błędów")
    void applicationStarts() {
        if (startupError != null) {
            Throwable root = startupError;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            fail("Aplikacja nie wstała. Pierwotna przyczyna: " + root, startupError);
        }
    }

    @Test
    @DisplayName("R2.02 Co najmniej 4 własne tabele (poza gotową user_account)")
    void hasEnoughTables() {
        assertThat(schema().ownTables()).as("Wasze tabele (tabela łącząca @ManyToMany też się liczy)").hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("R2.03 Każda tabela ma klucz główny")
    void everyTableHasPrimaryKey() {
        assertThat(schema().tables().stream().filter(t -> !schema().primaryKeys().containsKey(t)).toList())
                .as("Tabele bez klucza głównego (@Id)")
                .isEmpty();
    }

    @Test
    @DisplayName("R2.04 Tabele są powiązane: co najmniej 4 klucze obce")
    void tablesAreRelated() {
        assertThat(schema().foreignKeys())
                .as("Klucze obce powstają z @ManyToOne, @OneToOne i tabel łączących @ManyToMany")
                .hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("R2.05 Jest relacja wiele-do-wielu: tabela z kluczami obcymi do dwóch różnych tabel")
    void hasManyToManyTable() {
        List<String> joinTables = schema().tables().stream()
                .filter(t -> schema().foreignKeysOf(t).stream().map(ForeignKey::referencedTable).distinct().count() >= 2)
                .toList();
        assertThat(joinTables)
                .as("Dodaj @ManyToMany (tabela łącząca) albo encję łączącą z dwoma @ManyToOne, np. rezerwacja = klient + oferta")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R2.06 Jest relacja jeden-do-jednego: klucz obcy z ograniczeniem UNIQUE")
    void hasOneToOne() {
        List<String> oneToOne = schema().foreignKeys().stream()
                .filter(fk -> schema().isUnique(fk.table(), fk.column()))
                .map(fk -> fk.table() + "." + fk.column())
                .toList();
        assertThat(oneToOne)
                .as("Dodaj @OneToOne z @JoinColumn: Hibernate nałoży UNIQUE na kolumnę klucza obcego")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R2.07 Co najmniej jedna Wasza tabela wskazuje na user_account")
    void ownTablesUseUserAccount() {
        assertThat(schema().foreignKeys().stream().filter(fk -> fk.referencedTable().equals(DatabaseSchema.DEFAULT_TABLE)))
                .as("Kto w Waszym projekcie jest użytkownikiem? Powiąż go z kontem: @ManyToOne/@OneToOne UserAccount")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R2.08 Statusy zapisane jako tekst: kolumna ENUM w Waszej tabeli (@Enumerated(EnumType.STRING))")
    void hasStatusStoredAsText() {
        assertThat(schema().columns().stream()
                .filter(c -> !c.table().equals(DatabaseSchema.DEFAULT_TABLE))
                .filter(c -> c.type().startsWith("ENUM")))
                .as("Dodaj enum (np. status) z @Enumerated(EnumType.STRING). Przy ORDINAL w bazie ląduje liczba")
                .isNotEmpty();
    }

    @Test
    @DisplayName("R2.09 Żadnych liczb zmiennoprzecinkowych: kwoty jako NUMERIC (BigDecimal)")
    void noFloatingPointColumns() {
        Set<String> floating = Set.of("DOUBLE", "DOUBLE PRECISION", "FLOAT", "REAL", "FLOAT4", "FLOAT8");
        assertThat(schema().columns().stream()
                .filter(c -> floating.contains(c.type()))
                .map(c -> c.table() + "." + c.name() + " : " + c.type())
                .toList())
                .as("0.1 + 0.2 != 0.3. Pieniądze i ilości trzymamy w BigDecimal (kolumna NUMERIC)")
                .isEmpty();
    }

    @Test
    @DisplayName("R2.10 Dane startowe: co najmniej 2 Wasze tabele mają po ≥3 wiersze z data.sql")
    void ownTablesHaveSeedData() {
        JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
        Map<String, Integer> counts = new TreeMap<>();
        for (String table : schema().ownTables()) {
            counts.put(table, jdbc.queryForObject("select count(*) from " + table, Integer.class));
        }
        assertThat(counts.values().stream().filter(c -> c >= 3).count())
                .as("Dopiszcie dane do swoich tabel w src/main/resources/data.sql. Teraz: " + counts)
                .isGreaterThanOrEqualTo(2);
    }

    private static DatabaseSchema schema() {
        if (schema == null) {
            fail("Schemat niedostępny, bo aplikacja nie wstała (zobacz R1.03)");
        }
        return schema;
    }
}
