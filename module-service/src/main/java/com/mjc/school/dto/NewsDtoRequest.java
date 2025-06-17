package com.mjc.school.dto;

import com.mjc.school.annotation.IdField;
import com.mjc.school.annotation.NotNull;
import com.mjc.school.annotation.StringField;

import java.util.List;

public final class NewsDtoRequest {
    @StringField(min = 5, max = 30)
    @NotNull
    private String title;

    @StringField(min = 5, max = 225)
    @NotNull
    private String content;

    @IdField
    @NotNull
    private Long authorId;

    private List<Long> tagIds;

    public NewsDtoRequest() {
    }

    public NewsDtoRequest(String title, String content, Long authorId, List<Long> tagIds) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.tagIds = tagIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
