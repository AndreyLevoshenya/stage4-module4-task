package com.mjc.school.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface BaseController<T, R, K> {

    ResponseEntity<Page<R>> readAll(String searchBy, String searchValue, Pageable pageable);

    ResponseEntity<R> readById(K id);

    ResponseEntity<R> create(T createRequest);

    ResponseEntity<R> update(Long id, T updateRequest);

    ResponseEntity<R> patch(Long id, T patchRequest);

    void deleteById(K id);
}
