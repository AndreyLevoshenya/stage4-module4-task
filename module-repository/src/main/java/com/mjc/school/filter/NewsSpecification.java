package com.mjc.school.filter;

import com.mjc.school.model.News;
import com.mjc.school.model.Tag;
import jakarta.persistence.criteria.*;
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
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<News> subRoot = subquery.from(News.class);
            Join<News, Tag> subTags = subRoot.join("tags", JoinType.INNER);

            subquery.select(subRoot.get("id"))
                    .where(subTags.get("name").in(tagNames))
                    .groupBy(subRoot.get("id"))
                    .having(cb.equal(cb.countDistinct(subTags.get("name")), tagNames.size()));

            return cb.in(root.get("id")).value(subquery);
        };
    }

}

