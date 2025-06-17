package com.mjc.school.validator;

import java.util.Set;

public interface Validator {
    Set<ConstraintViolation> validate(Object o);
}
