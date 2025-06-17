package com.mjc.school.validator.checkers;

import com.mjc.school.annotation.StringField;
import org.springframework.stereotype.Component;

@Component
public class StringFieldChecker implements ConstraintChecker<StringField> {
    @Override
    public boolean check(Object value, StringField constraint) {
        if (value instanceof CharSequence sequence) {
            return (sequence.length() >= constraint.min() && sequence.length() <= constraint.max());
        }
        return true;
    }

    @Override
    public Class<StringField> getType() {
        return StringField.class;
    }
}
