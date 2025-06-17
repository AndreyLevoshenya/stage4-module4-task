package com.mjc.school.mapper;

import com.mjc.school.dto.TagDtoRequest;
import com.mjc.school.dto.TagDtoResponse;
import com.mjc.school.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TagDtoMapper {


    TagDtoResponse modelToDto(Tag tag);

    @Mappings(value = {
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "news", ignore = true)
    })
    Tag dtoToModel(TagDtoRequest dtoRequest);
}
