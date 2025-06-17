package com.mjc.school.dto;

import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class NewsDtoResponse extends RepresentationModel<NewsDtoResponse> {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime lastUpdateDate;
    private AuthorDtoResponse authorDtoResponse;
    private List<TagDtoResponse> tagDtoResponseList;

    public NewsDtoResponse() {
    }

    public NewsDtoResponse(Long id, String title, String content, LocalDateTime createDate, LocalDateTime lastUpdateDate, AuthorDtoResponse authorDtoResponse, List<TagDtoResponse> tagDtoResponseList) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createDate = createDate;
        this.lastUpdateDate = lastUpdateDate;
        this.authorDtoResponse = authorDtoResponse;
        this.tagDtoResponseList = tagDtoResponseList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AuthorDtoResponse getAuthorDtoResponse() {
        return authorDtoResponse;
    }

    public void setAuthorDtoResponse(AuthorDtoResponse authorDtoResponse) {
        this.authorDtoResponse = authorDtoResponse;
    }

    public List<TagDtoResponse> getTagDtoResponseList() {
        return tagDtoResponseList;
    }

    public void setTagDtoResponseList(List<TagDtoResponse> tagDtoResponseSet) {
        this.tagDtoResponseList = tagDtoResponseSet;
    }

    @Override
    public String toString() {
        return "NewsDtoResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createDate=" + createDate +
                ", lastUpdateDate=" + lastUpdateDate +
                ", authorDtoResponse=" + authorDtoResponse +
                ", tagDtoResponseList=" + tagDtoResponseList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsDtoResponse that = (NewsDtoResponse) o;
        return id.equals(that.id) && title.equals(that.title) && content.equals(that.content) && createDate.equals(that.createDate) && lastUpdateDate.equals(that.lastUpdateDate) && authorDtoResponse.equals(that.authorDtoResponse) && tagDtoResponseList.equals(that.tagDtoResponseList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, createDate, lastUpdateDate, authorDtoResponse, tagDtoResponseList);
    }
}
