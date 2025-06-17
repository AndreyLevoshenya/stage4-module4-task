package com.mjc.school.mapper;

import com.mjc.school.dto.NewsDtoRequest;
import com.mjc.school.dto.NewsDtoResponse;
import com.mjc.school.model.News;
import com.mjc.school.repository.AuthorRepository;
import com.mjc.school.repository.TagRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface NewsDtoMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createDate", ignore = true),
            @Mapping(target = "lastUpdateDate", ignore = true),
            @Mapping(target = "author", expression = "java(authorRepository.getReferenceById(dtoRequest.getAuthorId()))"),
            @Mapping(target = "tags",
                    expression = "java(dtoRequest.getTagIds() != null ? dtoRequest.getTagIds().stream().map(tagId -> tagRepository.getReferenceById(tagId)).toList() : java.util.Collections.emptyList())"),
            @Mapping(target = "comments", ignore = true)})
    News dtoToModel(
            NewsDtoRequest dtoRequest,
            @Context AuthorRepository authorRepository,
            @Context TagRepository tagRepository);

    @Mappings({
            @Mapping(source = "author", target = "authorDtoResponse"),
            @Mapping(source = "tags", target = "tagDtoResponseList")})
    NewsDtoResponse modelToDto(News news);
}
