package pl.wsb.rules.core;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

class SinceLabCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return context.getElement()
                .flatMap(element -> AnnotationSupport.findAnnotation(element, SinceLab.class))
                .map(since -> since.value() <= Project.currentLab()
                        ? ConditionEvaluationResult.enabled("reguła obowiązuje od LAB " + since.value())
                        : ConditionEvaluationResult.disabled(
                                "reguła obowiązuje od LAB " + since.value() + " (aktualnie LAB " + Project.currentLab() + ")"))
                .orElse(ConditionEvaluationResult.enabled("brak @SinceLab"));
    }
}
