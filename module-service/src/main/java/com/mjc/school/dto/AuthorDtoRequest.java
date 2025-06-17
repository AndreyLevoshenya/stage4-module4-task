package com.mjc.school.dto;

import com.mjc.school.annotation.NotNull;
import com.mjc.school.annotation.StringField;

public final class AuthorDtoRequest {
    @NotNull
    @StringField(min = 3, max = 15)
    private String name;

    public AuthorDtoRequest() {
    }

    public AuthorDtoRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
