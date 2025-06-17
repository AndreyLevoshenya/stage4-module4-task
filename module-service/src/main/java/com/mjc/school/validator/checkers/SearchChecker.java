package com.mjc.school.validator.checkers;

import com.mjc.school.annotation.Search;
import org.springframework.stereotype.Component;

@Component
public class SearchChecker implements ConstraintChecker<Search> {
    @Override
    public boolean check(Object value, Search constraint) {
        if (value instanceof CharSequence sequence) {
            return (!sequence.isEmpty() && sequence.toString().matches("^[\\w_]+:.*$"));
        }
        return true;
    }

    @Override
    public Class<Search> getType() {
        return Search.class;
    }
}
