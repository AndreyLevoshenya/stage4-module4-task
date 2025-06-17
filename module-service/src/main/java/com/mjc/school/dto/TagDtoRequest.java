package com.mjc.school.dto;

import com.mjc.school.annotation.NotNull;
import com.mjc.school.annotation.StringField;

public class TagDtoRequest {
    @StringField(min = 3, max = 15)
    @NotNull
    private String name;

    public TagDtoRequest() {
    }

    public TagDtoRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


