package com.mjc.school.dto;

import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.Objects;

public class CommentDtoResponse extends RepresentationModel<CommentDtoResponse> {
    private Long id;
    private String content;
    private NewsDtoResponse newsDtoResponse;
    private LocalDateTime createDate;
    private LocalDateTime lastUpdateDate;

    public CommentDtoResponse() {
    }

    public CommentDtoResponse(Long id, String content, NewsDtoResponse newsDtoResponse, LocalDateTime createDate, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.content = content;
        this.newsDtoResponse = newsDtoResponse;
        this.createDate = createDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NewsDtoResponse getNewsDtoResponse() {
        return newsDtoResponse;
    }

    public void setNewsDtoResponse(NewsDtoResponse newsDtoResponse) {
        this.newsDtoResponse = newsDtoResponse;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String toString() {
        return "CommentDtoResponse{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", newsDtoResponse=" + newsDtoResponse +
                ", createDate=" + createDate +
                ", lastUpdateDate=" + lastUpdateDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentDtoResponse that = (CommentDtoResponse) o;
        return id.equals(that.id) && content.equals(that.content) && newsDtoResponse.equals(that.newsDtoResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, newsDtoResponse);
    }
}
