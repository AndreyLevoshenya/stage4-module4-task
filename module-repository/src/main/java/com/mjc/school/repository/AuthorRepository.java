package com.mjc.school.repository;

import com.mjc.school.model.Author;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    List<Author> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Page<Author> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Page<Author> findAll(Specification<Author> spec, @NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Optional<Author> findById(@NonNull Long id);

    @Query("SELECT a FROM Author a INNER JOIN a.news n WHERE n.id = :newsId")
    Optional<Author> readByNewsId(@Param("newsId") Long newsId);
}
