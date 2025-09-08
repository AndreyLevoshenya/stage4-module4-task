package com.mjc.school.service;

import com.mjc.school.dto.AuthorDtoRequest;
import com.mjc.school.dto.AuthorDtoResponse;
import com.mjc.school.dto.AuthorDtoResponseWithNews;

public interface AuthorService extends BaseService<AuthorDtoRequest, AuthorDtoResponseWithNews, Long> {
    AuthorDtoResponseWithNews readByNewsId(Long newsId);
    AuthorDtoResponseWithNews readByUserUsername(String username);
}
