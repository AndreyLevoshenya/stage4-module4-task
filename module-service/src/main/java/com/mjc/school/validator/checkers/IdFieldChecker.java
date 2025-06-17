package com.mjc.school.validator.checkers;

import com.mjc.school.annotation.IdField;
import org.springframework.stereotype.Component;

@Component
public class IdFieldChecker implements ConstraintChecker<IdField> {

    @Override
    public boolean check(Object value, IdField constraint) {
        if (value == null) {
            return true;
        }
        if (value instanceof Number number) {
            return number.longValue() < Long.MAX_VALUE && number.longValue() > 0;
        } else {
            return false;
        }
    }

    @Override
    public Class<IdField> getType() {
        return IdField.class;
    }
}
