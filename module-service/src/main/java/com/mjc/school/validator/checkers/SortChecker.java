package com.mjc.school.validator.checkers;

import com.mjc.school.annotation.Sort;
import org.springframework.stereotype.Component;

@Component
public class SortChecker implements ConstraintChecker<Sort> {
    @Override
    public boolean check(Object value, Sort constraint) {
        if (value instanceof CharSequence sequence) {
            return (!sequence.isEmpty() && sequence.toString().matches("^[\\w_]+:(asc|desc)$"));
        }
        return true;
    }

    @Override
    public Class<Sort> getType() {
        return Sort.class;
    }
}
