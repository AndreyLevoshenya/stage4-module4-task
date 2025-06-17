package com.mjc.school.dto;

import com.mjc.school.annotation.IdField;
import com.mjc.school.annotation.NotNull;
import com.mjc.school.annotation.StringField;

public final class CommentDtoRequest {
    @StringField(min = 5, max = 255)
    @NotNull
    private String content;

    @IdField
    private Long newsId;

    public CommentDtoRequest() {
    }

    public CommentDtoRequest(String content, Long newsId) {
        this.content = content;
        this.newsId = newsId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getNewsId() {
        return newsId;
    }

    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }
}
