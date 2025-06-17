package com.mjc.school.service;

import com.mjc.school.dto.CommentDtoRequest;
import com.mjc.school.dto.CommentDtoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService extends BaseService<CommentDtoRequest, CommentDtoResponse, Long> {
    Page<CommentDtoResponse> readByNewsId(Long newsId, Pageable pageable);
}
