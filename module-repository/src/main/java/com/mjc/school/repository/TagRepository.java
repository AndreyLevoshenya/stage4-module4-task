package com.mjc.school.repository;

import com.mjc.school.model.Tag;
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

public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    List<Tag> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Page<Tag> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Page<Tag> findAll(Specification<Tag> spec, @NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Optional<Tag> findById(@NonNull Long id);

    @Query("SELECT t FROM Tag t INNER JOIN t.news n WHERE n.id = :newsId")
    Page<Tag> readByNewsId(@Param("newsId") Long newsId, Pageable pageable);
}
