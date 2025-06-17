package com.mjc.school.dto;

import com.mjc.school.annotation.Search;

public class SearchingRequest {
    @Search
    private String fieldNameAndValue;

    public SearchingRequest() {
    }

    public SearchingRequest(String fieldNameAndValue) {
        this.fieldNameAndValue = fieldNameAndValue;
    }

    public String getFieldNameAndValue() {
        return fieldNameAndValue;
    }

    public void setFieldNameAndValue(String fieldNameAndValue) {
        this.fieldNameAndValue = fieldNameAndValue;
    }
}
