package pl.wsb.rules.core;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Uruchamia aplikację zespołu na czystej bazie H2 w pamięci, z wybranymi nadpisanymi ustawieniami.
 * Argumenty linii poleceń mają pierwszeństwo przed application.properties.
 */
public final class ProjectApp {

    private ProjectApp() {
    }

    /** Schemat generuje Hibernate z encji, potem wczytuje się data.sql. */
    public static ConfigurableApplicationContext startWithHibernateSchema() {
        return start("--spring.jpa.hibernate.ddl-auto=create-drop");
    }

    public static ConfigurableApplicationContext start(String... overrides) {
        List<String> args = new ArrayList<>(List.of(
                "--spring.datasource.url=jdbc:h2:mem:test-" + UUID.randomUUID()
                        + ";DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "--spring.datasource.username=sa",
                "--spring.datasource.password=",
                "--spring.h2.console.enabled=false",
                "--spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true",
                "--spring.jpa.show-sql=false",
                "--spring.main.banner-mode=off",
                "--logging.level.root=WARN"));
        args.addAll(List.of(overrides));
        return new SpringApplicationBuilder(Project.applicationClass())
                .web(WebApplicationType.NONE)
                .run(args.toArray(String[]::new));
    }

    public static EntityManagerFactory entityManagerFactory(ConfigurableApplicationContext context) {
        return context.getBean(EntityManagerFactory.class);
    }
}
