package com.mjc.school.filter;

import com.mjc.school.model.News;
import com.mjc.school.model.Tag;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class NewsSpecification {

    public static Specification<News> searchByText(String value) {
        return EntitySpecification.searchByFields(List.of("title", "content"), value);
    }

    public static Specification<News> hasTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty())
            return (root, query, cb) -> cb.conjunction();

        return (root, query, cb) -> {
            query.distinct(true);

            Join<News, Tag> tags = root.join("tags", JoinType.INNER);
            CriteriaBuilder.In<String> inClause = cb.in(tags.get("name"));
            tagNames.forEach(inClause::value);

            return inClause;
        };
    }
}

