package com.mjc.school.repository;

import com.mjc.school.model.Comment;
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

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    List<Comment> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Page<Comment> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Page<Comment> findAll(Specification<Comment> spec, @NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "news")
    Optional<Comment> findById(@NonNull Long id);

    @Query("SELECT c FROM Comment c WHERE c.news.id = :newsId")
    Page<Comment> readByNewsId(@Param("newsId") Long newsId, Pageable pageable);
}
