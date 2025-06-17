package com.mjc.school.repository;

import com.mjc.school.model.News;
import com.mjc.school.model.SearchParameters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"news", "comments"})
    List<News> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"author", "comments"})
    Page<News> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"author", "comments"})
    Page<News> findAll(Specification<News> spec, @NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"author", "comments"})
    Optional<News> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"author", "tags"})
    @Query("""
                SELECT n FROM News n
                LEFT JOIN n.author a
                LEFT JOIN n.tags t
                WHERE (:#{#params.newsTitle} IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :#{#params.newsTitle}, '%')))
                AND (:#{#params.newsContent} IS NULL OR LOWER(n.content) LIKE LOWER(CONCAT('%', :#{#params.newsContent}, '%')))
                AND (:#{#params.authorName} IS NULL OR LOWER(a.name) = LOWER(:#{#params.authorName}))
                AND (:#{#params.tagIds} IS NULL OR t.id IN (:#{#params.tagIds}))
                AND (:#{#params.tagNames} IS NULL OR LOWER(t.name) IN (:#{#params.tagNames}))
            """)
    Page<News> readByParams(@Param("params") SearchParameters params, Pageable pageable);
}
