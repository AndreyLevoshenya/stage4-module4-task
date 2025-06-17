package com.mjc.school.dto;

import org.springframework.hateoas.RepresentationModel;

import java.util.Objects;

public class TagDtoResponse extends RepresentationModel<TagDtoResponse> {
    private Long id;
    private String name;

    public TagDtoResponse() {
    }

    public TagDtoResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TagDtoResponse{" +
                "id=" + id +
                ", name=" + name +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagDtoResponse that = (TagDtoResponse) o;
        return id.equals(that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
