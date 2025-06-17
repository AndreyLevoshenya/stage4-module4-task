package com.mjc.school.service.impl;

import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.dto.TagDtoRequest;
import com.mjc.school.dto.TagDtoResponse;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.filter.EntitySpecification;
import com.mjc.school.mapper.TagDtoMapper;
import com.mjc.school.model.Tag;
import com.mjc.school.repository.NewsRepository;
import com.mjc.school.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static com.mjc.school.exception.ExceptionErrorCodes.NEWS_DOES_NOT_EXIST;
import static com.mjc.school.exception.ExceptionErrorCodes.TAG_DOES_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {
    @Mock
    private TagRepository tagRepository;
    @Mock
    private NewsRepository newsRepository;
    @Mock
    private TagDtoMapper tagDtoMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void readAll_shouldReturnAllTags_whenSearchingRequestIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Tag> tags = List.of(new Tag(), new Tag());
        Page<Tag> tagPage = new PageImpl<>(tags, pageable, tags.size());

        TagDtoResponse dtoResponse = new TagDtoResponse();

        when(tagRepository.findAll(pageable)).thenReturn(tagPage);
        when(tagDtoMapper.modelToDto(any(Tag.class))).thenReturn(dtoResponse);

        Page<TagDtoResponse> result = tagService.readAll(null, pageable);

        assertEquals(tags.size(), result.getContent().size());
        verify(tagRepository).findAll(pageable);
        verify(tagDtoMapper, times(tags.size())).modelToDto(any(Tag.class));
    }

    @Test
    void readAll_shouldReturnFilteredTags_whenSearchingRequestIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchingRequest searchingRequest = new SearchingRequest("name:Name");
        String[] specs = searchingRequest.getFieldNameAndValue().split(":");
        Specification<Tag> specification = EntitySpecification.searchByField(specs[0], specs[1]);

        List<Tag> tags = List.of(new Tag(), new Tag());
        Page<Tag> tagPage = new PageImpl<>(tags, pageable, tags.size());

        when(tagRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tagPage);
        when(tagDtoMapper.modelToDto(any(Tag.class))).thenReturn(new TagDtoResponse());

        Page<TagDtoResponse> result = tagService.readAll(searchingRequest, pageable);

        assertEquals(tags.size(), result.getContent().size());
        verify(tagRepository).findAll(any(Specification.class), eq(pageable));
        verify(tagDtoMapper, times(tags.size())).modelToDto(any(Tag.class));
    }

    @Test
    void readById_shouldReturnTagDto_whenTagExists() {
        Long id = 1L;
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName("Name");
        TagDtoResponse expectedDto = new TagDtoResponse(id, "Name");

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));
        when(tagDtoMapper.modelToDto(tag)).thenReturn(expectedDto);

        TagDtoResponse actualDto = tagService.readById(id);

        assertEquals(expectedDto, actualDto);
        verify(tagRepository).findById(id);
        verify(tagDtoMapper).modelToDto(tag);
    }

    @Test
    void readById_shouldThrowNotFoundException_whenTagDoesNotExist() {
        Long id = 99L;

        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.readById(id));

        assertEquals(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());
        verify(tagRepository).findById(id);
        verifyNoInteractions(tagDtoMapper);
    }

    @Test
    void create_shouldSaveAndReturnTagDto() {
        Long id = 1L;
        TagDtoRequest createRequest = new TagDtoRequest("Name");
        Tag model = new Tag("Name");
        Tag savedTag = new Tag("Name");
        savedTag.setId(id);
        TagDtoResponse expectedDto = new TagDtoResponse(id, "Name");

        when(tagDtoMapper.dtoToModel(createRequest)).thenReturn(model);
        when(tagRepository.save(model)).thenReturn(savedTag);
        when(tagDtoMapper.modelToDto(savedTag)).thenReturn(expectedDto);

        TagDtoResponse actualDto = tagService.create(createRequest);

        assertEquals(expectedDto, actualDto);
        verify(tagDtoMapper).dtoToModel(createRequest);
        verify(tagRepository).save(model);
        verify(tagDtoMapper).modelToDto(savedTag);
    }

    @Test
    void update_shouldReturnUpdatedTagDto_whenTagExists() {
        Long id = 1L;
        TagDtoRequest updateRequest = new TagDtoRequest();
        updateRequest.setName("Updated Name");

        Tag tag = new Tag("Updated Name");
        tag.setId(id);
        Tag savedTag = new Tag("Updated Name");
        savedTag.setId(id);
        TagDtoResponse expectedDto = new TagDtoResponse(id, "Updated Name");

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(savedTag);
        when(tagDtoMapper.modelToDto(savedTag)).thenReturn(expectedDto);

        TagDtoResponse actualDto = tagService.update(id, updateRequest);

        assertEquals(expectedDto, actualDto);
        verify(tagRepository).findById(id);
        verify(tagRepository).save(tag);
        verify(tagDtoMapper).modelToDto(savedTag);
    }

    @Test
    void update_shouldThrowNotFoundException_whenTagDoesNotExist() {
        Long id = 1L;
        TagDtoRequest updateRequest = new TagDtoRequest();

        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.update(id, updateRequest));

        assertEquals(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());
        verify(tagRepository).findById(id);
        verifyNoMoreInteractions(tagRepository);
        verifyNoInteractions(tagDtoMapper);
    }

    @Test
    void patch_shouldUpdateNameAndReturnDto_whenTagExists() {
        Long id = 1L;
        String newName = "Updated Name";

        TagDtoRequest patchRequest = new TagDtoRequest();
        patchRequest.setName(newName);

        Tag prevTag = new Tag("Old Name");
        prevTag.setId(id);
        Tag savedTag = new Tag(newName);
        savedTag.setId(id);
        TagDtoResponse expectedDto = new TagDtoResponse(id, newName);

        when(tagRepository.findById(id)).thenReturn(Optional.of(prevTag));
        when(tagRepository.save(prevTag)).thenReturn(savedTag);
        when(tagDtoMapper.modelToDto(savedTag)).thenReturn(expectedDto);

        TagDtoResponse actualDto = tagService.patch(id, patchRequest);

        assertEquals(expectedDto, actualDto);
        assertEquals(newName, prevTag.getName());

        verify(tagRepository).findById(id);
        verify(tagRepository).save(prevTag);
        verify(tagDtoMapper).modelToDto(savedTag);
    }

    @Test
    void patch_shouldNotUpdateName_whenNameIsNull() {
        Long id = 1L;
        String originalName = "Original Name";

        Tag existingTag = new Tag(originalName);
        existingTag.setId(id);

        TagDtoRequest patchRequest = new TagDtoRequest();

        TagDtoResponse expectedResponse = new TagDtoResponse(id, originalName);

        when(tagRepository.findById(id)).thenReturn(Optional.of(existingTag));
        when(tagRepository.save(existingTag)).thenReturn(existingTag);
        when(tagDtoMapper.modelToDto(existingTag)).thenReturn(expectedResponse);

        TagDtoResponse actualResponse = tagService.patch(id, patchRequest);

        assertEquals(expectedResponse, actualResponse);
        assertEquals(originalName, existingTag.getName());
        verify(tagRepository).findById(id);
        verify(tagRepository).save(existingTag);
        verify(tagDtoMapper).modelToDto(existingTag);
    }

    @Test
    void patch_shouldThrowNotFoundException_whenTagDoesNotExist() {
        Long id = 99L;
        TagDtoRequest patchRequest = new TagDtoRequest();

        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.patch(id, patchRequest));

        assertEquals(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());

        verify(tagRepository).findById(id);
        verifyNoMoreInteractions(tagRepository);
        verifyNoInteractions(tagDtoMapper);
    }

    @Test
    void deleteById_shouldDelete_whenTagExists() {
        Long id = 1L;
        Tag tag = new Tag("Tag1");
        tag.setId(id);

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));

        tagService.deleteById(id);

        verify(tagRepository).findById(id);
        verify(tagRepository).deleteById(id);
    }

    @Test
    void deleteById_shouldThrowNotFoundException_whenTagDoesNotExist() {
        Long id = 99L;

        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.deleteById(id));

        assertEquals(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());

        verify(tagRepository).findById(id);
        verify(tagRepository, never()).deleteById(any());
    }

    @Test
    void readByNewsId_shouldReturnPageOfTags_whenNewsExists() {
        Long newsId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Tag tag = new Tag("Name");
        tag.setId(1L);
        TagDtoResponse dtoResponse = new TagDtoResponse(1L, "Name");
        Page<Tag> tagPage = new PageImpl<>(List.of(tag));

        when(newsRepository.existsById(newsId)).thenReturn(true);
        when(tagRepository.readByNewsId(newsId, pageable)).thenReturn(tagPage);
        when(tagDtoMapper.modelToDto(tag)).thenReturn(dtoResponse);

        Page<TagDtoResponse> result = tagService.readByNewsId(newsId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dtoResponse, result.getContent().get(0));
        verify(newsRepository).existsById(newsId);
        verify(tagRepository).readByNewsId(newsId, pageable);
        verify(tagDtoMapper).modelToDto(tag);
    }

    @Test
    void readByNewsId_shouldThrowNotFoundException_whenNewsNotFound() {
        Long newsId = 99L;
        Pageable pageable = PageRequest.of(0, 10);

        when(newsRepository.existsById(newsId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.readByNewsId(newsId, pageable));

        assertEquals(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), newsId), exception.getMessage());

        verify(newsRepository).existsById(newsId);
        verifyNoInteractions(tagRepository);
        verifyNoInteractions(tagDtoMapper);
    }

}