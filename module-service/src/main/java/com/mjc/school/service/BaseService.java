package com.mjc.school.service;

import com.mjc.school.annotation.Valid;
import com.mjc.school.dto.SearchingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BaseService<T, R, K> {
    Page<R> readAll(@Valid SearchingRequest searchingRequest, Pageable pageable);

    R readById(K id);

    R create(T createRequest);

    R update(K id, T updateRequest);

    R patch(K id, T patchRequest);

    void deleteById(K id);
}
