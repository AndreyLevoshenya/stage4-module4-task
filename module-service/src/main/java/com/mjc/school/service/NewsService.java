package com.mjc.school.service;

import com.mjc.school.dto.NewsDtoRequest;
import com.mjc.school.dto.NewsDtoResponse;
import com.mjc.school.dto.ParametersDtoRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsService extends BaseService<NewsDtoRequest, NewsDtoResponse, Long> {
    Page<NewsDtoResponse> readByParams(ParametersDtoRequest parametersDtoRequest, Pageable pageable);
}
