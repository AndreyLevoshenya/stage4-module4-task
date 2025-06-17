package com.mjc.school.validator;

import com.mjc.school.annotation.Constraint;
import com.mjc.school.validator.checkers.ConstraintChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class ValidatorImpl implements Validator {
    private final Logger LOGGER = LoggerFactory.getLogger(ValidatorImpl.class);
    private final Map<Class<? extends Annotation>, ConstraintChecker> checkerMap;

    @Autowired
    public ValidatorImpl(List<ConstraintChecker> checkers) {
        this.checkerMap = checkers.stream().collect(toMap(ConstraintChecker::getType, Function.identity()));
    }

    @Override
    public Set<ConstraintViolation> validate(Object o) {
        if (o == null) {
            return Collections.emptySet();
        }

        HashSet<ConstraintViolation> constraintViolations = new HashSet<>();

        for (Field field : o.getClass().getDeclaredFields()) {
            validateField(constraintViolations, field, o);
        }
        return constraintViolations;
    }

    private void validateField(Set<ConstraintViolation> constraintViolations, Field field, Object o) {
        for (var annotation : field.getDeclaredAnnotations()) {
            var annotationType = annotation.annotationType();
            if (annotationType.isAnnotationPresent(Constraint.class)) {
                try {
                    if (field.trySetAccessible() && field.canAccess(o)) {
                        var value = field.get(o);
                        var checker = checkerMap.get(annotationType);
                        if (checker != null && !checker.check(value, annotation)) {
                            constraintViolations.add(new ConstraintViolation("Constraint %s violated for value %s".formatted(annotationType.getSimpleName(), value)));
                        }
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("Cannot access field {}.{}()", field.getName(), o.getClass().getSimpleName(), e);
                }

            }
        }

    }
}
