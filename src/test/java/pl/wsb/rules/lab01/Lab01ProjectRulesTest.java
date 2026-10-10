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

import static org.assertj.core.api.Assertions.assertThat;

@SinceLab(1)
@DisplayName("LAB 01 · Projekt, aplikacja i opis pomysłu")
class Lab01ProjectRulesTest {

    @Test
    @DisplayName("R1.01 Projekt ma własną nazwę: pakiet pl.wsb.<nazwaprojektu> i artifactId wdtb-2026-backend-<nazwaprojektu>")
    void projectHasOwnName() {
        String name = Project.rootPackage().substring((Project.BASE_PACKAGE + ".").length());

        assertThat(name)
                .as("Pakiet główny musi mieć postać pl.wsb.<nazwaprojektu> (jedno słowo, małe litery, bez polskich znaków). "
                        + "Zmień pl.wsb.projekt na nazwę Waszego projektu.")
                .matches("[a-z][a-z0-9]*")
                .isNotEqualTo("projekt");

        Matcher artifact = Pattern.compile("<artifactId>([^<]+)</artifactId>")
                .matcher(Project.read("pom.xml").replaceAll("(?s)<parent>.*?</parent>", ""));
        assertThat(artifact.find()).isTrue();
        assertThat(artifact.group(1))
                .as("W pom.xml ustaw <artifactId>wdtb-2026-backend-" + name + "</artifactId> (repozytorium: WdTB-2026-Backend-" + name + ")")
                .isEqualTo("wdtb-2026-backend-" + name);
    }

    @Test
    @DisplayName("R1.02 PROJEKT.md: szkic pomysłu – zespół, problem, 5 historyjek, wstępny diagram ER")
    void projectCardIsFilled() {
        String card = Project.read("PROJEKT.md");

        assertThat(card)
                .as("PROJEKT.md: zastąp wszystkie znaczniki <uzupełnij>")
                .doesNotContain("<uzupełnij>");
        assertThat(card)
                .as("PROJEKT.md: dodaj diagram ER w bloku ```mermaid erDiagram")
                .contains("erDiagram");
        assertThat(Pattern.compile("(?m)^\\|\\s*@\\S+").matcher(card).results().count())
                .as("PROJEKT.md: tabela zespołu ma mieć wszystkie osoby (wiersze zaczynające się od | @login-github)")
                .isGreaterThanOrEqualTo(2);
        assertThat(Pattern.compile("(?mi)^\\s*\\d+\\.\\s*jako\\b").matcher(card).results().count())
                .as("PROJEKT.md: co najmniej 5 historyjek w formacie '1. Jako <kto> chcę <co>, aby <po co>'")
                .isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("R1.03 Aplikacja startuje (Spring Boot + baza H2)")
    void applicationStarts() {
        try (ConfigurableApplicationContext context = ProjectApp.startWithHibernateSchema()) {
            assertThat(context.isActive()).isTrue();
        }
    }

    @Test
    @DisplayName("R1.04 Każda osoba z zespołu ma swoje konto w data.sql (min. 3 konta poza demo)")
    void teamMembersHaveAccounts() {
        Set<String> demo = Set.of("ola@student.merito.pl", "kuba@student.merito.pl", "firma@example.com", "admin@example.com");
        try (ConfigurableApplicationContext context = ProjectApp.startWithHibernateSchema()) {
            List<String> emails = context.getBean(JdbcTemplate.class)
                    .queryForList("select lower(email) from user_account", String.class);
            assertThat(emails.stream().filter(e -> !demo.contains(e)).count())
                    .as("Dopiszcie do src/main/resources/data.sql konta wszystkich osób z zespołu. Teraz w bazie: " + emails)
                    .isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @DisplayName("R1.05 Strona startowa (static/index.html) opisuje Wasz projekt")
    void startPageDescribesProject() {
        assertThat(Project.read("src/main/resources/static/index.html").replaceAll("(?s)<!--.*?-->", ""))
                .as("Uzupełnijcie wizytówkę projektu w src/main/resources/static/index.html (zastąpcie wszystkie [uzupełnij])")
                .doesNotContain("[uzupełnij");
    }
}
