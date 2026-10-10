package pl.wsb.rules.lab01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.wsb.rules.core.Project;
import pl.wsb.rules.core.ProjectApp;
import pl.wsb.rules.core.SinceLab;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SinceLab(1)
@DisplayName("LAB 01 · Projekt, aplikacja i opis pomysłu")
class Lab01ProjectRulesTest {

    @Test
    @DisplayName("R1.01 Projekt ma własną nazwę: pakiet pl.wsb.<nazwaprojektu> i artifactId wdtb-2026-backend-<nazwaprojektu>")
    void projectHasOwnName() {
        String name = Project.rootPackage().substring((Project.BASE_PACKAGE + ".").length());

        assertThat(name)
                .withFailMessage("Pakiet główny musi mieć postać pl.wsb.<nazwaprojektu> (jedno słowo, małe litery, bez polskich znaków). "
                        + "Zmieńcie pl.wsb.projekt na nazwę Waszego projektu. Teraz: pl.wsb." + name)
                .matches("[a-z][a-z0-9]*")
                .isNotEqualTo("projekt");

        Matcher artifact = Pattern.compile("<artifactId>([^<]+)</artifactId>")
                .matcher(Project.read("pom.xml").replaceAll("(?s)<parent>.*?</parent>", ""));
        assertThat(artifact.find()).isTrue();
        assertThat(artifact.group(1))
                .withFailMessage("W pom.xml ustawcie <artifactId>wdtb-2026-backend-" + name + "</artifactId> (repozytorium: WdTB-2026-Backend-" + name + "). "
                        + "Teraz: " + artifact.group(1))
                .isEqualTo("wdtb-2026-backend-" + name);
    }

    @Test
    @DisplayName("R1.02 Aplikacja startuje (Spring Boot + baza H2)")
    void applicationStarts() {
        try (ConfigurableApplicationContext context = ProjectApp.startWithHibernateSchema()) {
            assertThat(context.isActive()).isTrue();
        }
    }

    @Test
    @DisplayName("R1.03 Każda osoba z zespołu ma swoje konto w data.sql (min. 3 konta poza demo)")
    void teamMembersHaveAccounts() {
        Set<String> demo = Set.of("ola@student.merito.pl", "kuba@student.merito.pl", "firma@example.com", "admin@example.com");
        try (ConfigurableApplicationContext context = ProjectApp.startWithHibernateSchema()) {
            List<String> emails = context.getBean(JdbcTemplate.class)
                    .queryForList("select lower(email) from user_account", String.class);
            long teamAccounts = emails.stream().filter(e -> !demo.contains(e)).count();
            assertThat(teamAccounts)
                    .withFailMessage("Dopiszcie do src/main/resources/data.sql konta wszystkich osób z zespołu "
                            + "(są " + teamAccounts + ", potrzeba co najmniej 3). Teraz w bazie: " + emails)
                    .isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @DisplayName("R1.04 Strona startowa (static/index.html) opisuje Wasz projekt")
    void startPageDescribesProject() {
        String page = Project.read("src/main/resources/static/index.html");
        // komentarze HTML zamieniamy na tyle samo pustych linii, żeby numery linii zgadzały się z plikiem
        String withoutComments = Pattern.compile("(?s)<!--.*?-->").matcher(page)
                .replaceAll(m -> "\n".repeat((int) m.group().lines().count() - 1));
        assertThat(linesContaining(withoutComments, "[uzupełnij"))
                .as("Uzupełnijcie wizytówkę projektu w src/main/resources/static/index.html "
                        + "(zastąpcie wszystkie [uzupełnij]; poniżej linie, w których jeszcze są)")
                .isEmpty();
    }

    @Test
    @DisplayName("R1.05 Wersja w pom.xml: 1.0.0-<nr indeksu>-<nr indeksu>[-<nr indeksu>]")
    void versionContainsStudentNumbers() {
        Matcher version = Pattern.compile("<version>([^<]+)</version>")
                .matcher(Project.read("pom.xml").replaceAll("(?s)<parent>.*?</parent>", ""));
        assertThat(version.find()).isTrue();
        String value = version.group(1).trim();
        assertThat(value)
                .withFailMessage("W pom.xml ustawcie <version>1.0.0-<numery indeksów osób z zespołu oddzielone myślnikiem></version>, "
                        + "np. 1.0.0-123456-123457-123458. Teraz: " + value)
                .matches("1\\.0\\.0(-\\d{5,7}){2,3}");
        String[] numbers = value.substring("1.0.0-".length()).split("-");
        assertThat(Set.of(numbers))
                .withFailMessage("Każdy numer indeksu w wersji wpisujecie raz. Teraz: " + value)
                .hasSize(numbers.length);
    }

    /** Linie z danym znacznikiem w formacie "linia N: …", żeby błąd wskazywał miejsce zamiast wypisywać cały plik. */
    private static List<String> linesContaining(String text, String marker) {
        List<String> lines = text.lines().toList();
        return IntStream.range(0, lines.size())
                .filter(i -> lines.get(i).contains(marker))
                .mapToObj(i -> "linia " + (i + 1) + ": " + lines.get(i).strip())
                .toList();
    }
}
