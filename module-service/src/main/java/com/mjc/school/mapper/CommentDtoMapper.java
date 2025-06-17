package com.mjc.school.mapper;

import com.mjc.school.dto.CommentDtoRequest;
import com.mjc.school.dto.CommentDtoResponse;
import com.mjc.school.model.Comment;
import com.mjc.school.repository.NewsRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CommentDtoMapper {

    @Mappings({
            @Mapping(target = "newsDtoResponse", expression = "java(newsDtoMapper.modelToDto(model.getNews()))")
    })
    CommentDtoResponse modelToDto(Comment model, @Context NewsDtoMapper newsDtoMapper);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "news", expression = "java(newsRepository.getReferenceById(dtoRequest.getNewsId()))"),
            @Mapping(target = "createDate", ignore = true),
            @Mapping(target = "lastUpdateDate", ignore = true),
    })
    Comment dtoToModel(CommentDtoRequest dtoRequest, @Context NewsRepository newsRepository);
}
