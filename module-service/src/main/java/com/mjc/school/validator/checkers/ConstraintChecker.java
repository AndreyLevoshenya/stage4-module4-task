package com.mjc.school.validator.checkers;

import java.lang.annotation.Annotation;

public interface ConstraintChecker<T extends Annotation> {
    boolean check(Object value, T constraint);

    Class<T> getType();

}
