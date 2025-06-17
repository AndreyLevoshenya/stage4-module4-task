package com.mjc.school.model;

public interface BaseEntity<K> {

    K getId();

    void setId(K id);
}
