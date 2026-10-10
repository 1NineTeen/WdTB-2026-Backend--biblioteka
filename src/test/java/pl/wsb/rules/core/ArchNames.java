package pl.wsb.rules.core;

import java.util.List;

/** Nazwy adnotacji podawane jako tekst, żeby reguły kolejnych laboratoriów kompilowały się bez ich zależności. */
public final class ArchNames {

    public static final String SERVICE = "org.springframework.stereotype.Service";
    public static final String COMPONENT = "org.springframework.stereotype.Component";
    public static final String AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
    public static final String TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";
    public static final String JAKARTA_TRANSACTIONAL = "jakarta.transaction.Transactional";
    public static final String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    public static final String CONTROLLER_ADVICE = "org.springframework.web.bind.annotation.RestControllerAdvice";
    public static final String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    public static final String VALID = "jakarta.validation.Valid";
    public static final String VALIDATED = "org.springframework.validation.annotation.Validated";
    public static final String CONFIGURATION_PROPERTIES = "org.springframework.boot.context.properties.ConfigurationProperties";
    public static final String EVENT_LISTENER = "org.springframework.context.event.EventListener";
    public static final String TX_EVENT_LISTENER = "org.springframework.transaction.event.TransactionalEventListener";
    public static final String APPLICATION_MODULE_LISTENER = "org.springframework.modulith.events.ApplicationModuleListener";
    public static final String SCHEDULED = "org.springframework.scheduling.annotation.Scheduled";
    /** Spring Boot 3 i 4 trzymają adnotacje testowe w różnych pakietach; reguły akceptują oba. */
    public static final List<String> WEB_MVC_TEST = List.of(
            "org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest",
            "org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest");
    public static final List<String> DATA_JPA_TEST = List.of(
            "org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest",
            "org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest");
    public static final String SPRING_BOOT_TEST = "org.springframework.boot.test.context.SpringBootTest";
    public static final String REST_CLIENT = "org.springframework.web.client.RestClient";
    public static final String REST_TEMPLATE = "org.springframework.web.client.RestTemplate";

    private ArchNames() {
    }

    public static boolean isAnnotatedWithAny(com.tngtech.archunit.core.domain.JavaClass javaClass, List<String> annotations) {
        return annotations.stream().anyMatch(javaClass::isAnnotatedWith);
    }
}
