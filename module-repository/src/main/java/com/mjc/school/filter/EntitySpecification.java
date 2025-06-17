package com.mjc.school.filter;

import com.mjc.school.model.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

public class EntitySpecification<T extends BaseEntity<Long>> {
    private static final String PERCENTAGE_SYMBOL = "%";

    public static <T> Specification<T> searchByField(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isEmpty() || field == null || field.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), PERCENTAGE_SYMBOL + value.toLowerCase() + PERCENTAGE_SYMBOL);
        };
    }
}
