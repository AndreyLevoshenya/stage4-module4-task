package com.mjc.school.validator.checkers;

import com.mjc.school.annotation.NotNull;
import org.springframework.stereotype.Component;

@Component
public class NotNullChecker implements ConstraintChecker<NotNull> {
    @Override
    public boolean check(Object value, NotNull constraint) {
        return value != null;
    }

    @Override
    public Class<NotNull> getType() {
        return NotNull.class;
    }
}
