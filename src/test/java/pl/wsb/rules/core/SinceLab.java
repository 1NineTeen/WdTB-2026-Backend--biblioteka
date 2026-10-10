package pl.wsb.rules.core;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reguła projektu obowiązuje od wskazanego laboratorium.
 *
 * <p>Aktualny numer laboratorium pochodzi z właściwości {@code lab}
 * (pom.xml, a w GitHub Actions z tagu {@code lab-N}).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SinceLabCondition.class)
public @interface SinceLab {

    int value();
}
