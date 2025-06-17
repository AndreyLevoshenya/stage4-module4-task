package com.mjc.school.service.impl;

import com.mjc.school.dto.AuthorDtoRequest;
import com.mjc.school.dto.AuthorDtoResponse;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.mapper.AuthorDtoMapper;
import com.mjc.school.model.Author;
import com.mjc.school.repository.AuthorRepository;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mjc.school.exception.ExceptionErrorCodes.AUTHOR_DOES_NOT_EXIST;
import static com.mjc.school.exception.ExceptionErrorCodes.NEWS_DOES_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private AuthorDtoMapper authorDtoMapper;

    @InjectMocks
    private AuthorServiceImpl authorService;

    @Test
    void readAll_shouldReturnAllAuthors_whenSearchingRequestIsNull() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author author = new Author("Name", date, date, new ArrayList<>());
        author.setId(1L);
        List<Author> authors = List.of(author);
        Page<Author> page = new PageImpl<>(authors);
        AuthorDtoResponse dtoResponse = new AuthorDtoResponse(
                author.getId(),
                author.getName(),
                author.getCreateDate(),
                author.getLastUpdateDate());

        when(authorRepository.findAll(pageable)).thenReturn(page);
        when(authorDtoMapper.modelToDto(authors.get(0))).thenReturn(dtoResponse);

        // When
        Page<AuthorDtoResponse> result = authorService.readAll(null, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Name", result.getContent().get(0).getName());

        verify(authorRepository).findAll(pageable);
        verify(authorDtoMapper).modelToDto(authors.get(0));
    }

    @Test
    void readAll_shouldApplySpecification_whenSearchingRequestIsProvided() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        SearchingRequest request = new SearchingRequest("name:Name");
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author author = new Author("Name", date, date, new ArrayList<>());
        author.setId(1L);
        List<Author> authors = List.of(author);
        Page<Author> page = new PageImpl<>(authors);
        AuthorDtoResponse dtoResponse = new AuthorDtoResponse(
                author.getId(),
                author.getName(),
                author.getCreateDate(),
                author.getLastUpdateDate());

        Specification<Author> spec = (root, query, cb) ->
                cb.equal(root.get("name"), "Name");

        when(authorRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(authorDtoMapper.modelToDto(authors.get(0))).thenReturn(dtoResponse);

        // When
        Page<AuthorDtoResponse> result = authorService.readAll(request, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Name", result.getContent().get(0).getName());

        verify(authorRepository).findAll(any(Specification.class), eq(pageable));
        verify(authorDtoMapper).modelToDto(authors.get(0));
    }

    @Test
    void shouldReturnAuthorDtoWhenAuthorExists() {
        // given
        Long id = 1L;
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author author = new Author("Name", date, date, new ArrayList<>());
        author.setId(1L);

        AuthorDtoResponse dtoResponse = new AuthorDtoResponse(
                author.getId(),
                author.getName(),
                author.getCreateDate(),
                author.getLastUpdateDate());

        when(authorRepository.findById(id)).thenReturn(Optional.of(author));
        when(authorDtoMapper.modelToDto(author)).thenReturn(dtoResponse);

        // when
        AuthorDtoResponse result = authorService.readById(id);

        // then
        assertNotNull(result);
        assertEquals(dtoResponse, result);

        verify(authorRepository).findById(id);
        verify(authorDtoMapper).modelToDto(author);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAuthorDoesNotExist() {
        // given
        Long id = 42L;
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> authorService.readById(id));
        assertEquals(exception.getMessage(), String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id));

        verify(authorRepository).findById(id);
        verifyNoInteractions(authorDtoMapper);
    }

    @Test
    void shouldCreateAuthorSuccessfully() {
        // given
        AuthorDtoRequest request = new AuthorDtoRequest("John Doe");
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author model = new Author("John Doe", dateTime, dateTime, null);
        Author saved = new Author("John Doe", dateTime, dateTime, new ArrayList<>());
        saved.setId(1L);
        AuthorDtoResponse response = new AuthorDtoResponse(1L, "John Doe", dateTime, dateTime);

        when(authorDtoMapper.dtoToModel(request)).thenReturn(model);
        when(authorRepository.save(model)).thenReturn(saved);
        when(authorDtoMapper.modelToDto(saved)).thenReturn(response);

        // when
        AuthorDtoResponse result = authorService.create(request);

        // then
        assertNotNull(result);
        assertEquals(response, result);

        verify(authorDtoMapper).dtoToModel(request);
        verify(authorRepository).save(model);
        verify(authorDtoMapper).modelToDto(saved);
    }

    @Test
    void update_shouldReturnUpdatedAuthor_whenAuthorExists() {
        // given
        AuthorDtoRequest updateRequest = new AuthorDtoRequest("Updated name");
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author model = new Author("Updated name", dateTime, dateTime, null);
        Author saved = new Author("Updated name", dateTime, dateTime, new ArrayList<>());
        saved.setId(1L);
        AuthorDtoResponse response = new AuthorDtoResponse(1L, "Updated name", dateTime, dateTime);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(model));
        when(authorRepository.save(model)).thenReturn(saved);
        when(authorDtoMapper.modelToDto(saved)).thenReturn(response);

        // when
        AuthorDtoResponse result = authorService.update(1L, updateRequest);

        // then
        assertNotNull(result);
        assertEquals(response, result);

        verify(authorRepository).findById(1L);
        verify(authorRepository).save(model);
        verify(authorDtoMapper).modelToDto(saved);
    }

    @Test
    void update_shouldThrowNotFoundException_whenAuthorDoesNotExist() {
        // given
        AuthorDtoRequest updateRequest = new AuthorDtoRequest("Name");

        when(authorRepository.findById(42L)).thenReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> authorService.update(42L, updateRequest));

        // then
        assertEquals(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), 42L),
                exception.getMessage());

        verify(authorRepository).findById(42L);
        verifyNoMoreInteractions(authorDtoMapper, authorRepository);
    }

    @Test
    void patch_shouldUpdateName_whenNameIsProvided() {
        // given
        Long id = 1L;
        String oldName = "Old Name";
        String newName = "New Name";
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author existingAuthor = new Author(oldName, dateTime, dateTime, null);
        existingAuthor.setId(id);

        Author updatedAuthor = new Author(newName, dateTime, dateTime, null);
        updatedAuthor.setId(id);

        AuthorDtoRequest patchRequest = new AuthorDtoRequest(newName);
        AuthorDtoResponse expectedResponse = new AuthorDtoResponse(id, newName, dateTime, dateTime);

        when(authorRepository.findById(id)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(existingAuthor)).thenReturn(updatedAuthor);
        when(authorDtoMapper.modelToDto(updatedAuthor)).thenReturn(expectedResponse);

        // when
        AuthorDtoResponse actualResponse = authorService.patch(id, patchRequest);

        // then
        assertEquals(expectedResponse, actualResponse);
        assertEquals(newName, existingAuthor.getName());
        verify(authorRepository).findById(id);
        verify(authorRepository).save(existingAuthor);
        verify(authorDtoMapper).modelToDto(updatedAuthor);
    }


    @Test
    void patch_shouldNotUpdateName_whenNameIsNull() {
        // given
        Long id = 1L;
        String originalName = "Original Name";
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Author existingAuthor = new Author(originalName, dateTime, dateTime, null);
        existingAuthor.setId(id);

        AuthorDtoRequest patchRequest = new AuthorDtoRequest(null);

        AuthorDtoResponse expectedResponse = new AuthorDtoResponse(id, originalName, dateTime, dateTime);

        when(authorRepository.findById(id)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(existingAuthor)).thenReturn(existingAuthor);
        when(authorDtoMapper.modelToDto(existingAuthor)).thenReturn(expectedResponse);

        // when
        AuthorDtoResponse actualResponse = authorService.patch(id, patchRequest);

        // then
        assertEquals(expectedResponse, actualResponse);
        assertEquals(originalName, existingAuthor.getName());
        verify(authorRepository).findById(id);
        verify(authorRepository).save(existingAuthor);
        verify(authorDtoMapper).modelToDto(existingAuthor);
    }

    @Test
    void patch_shouldThrowNotFoundException_whenAuthorDoesNotExist() {
        // given
        Long id = 42L;
        AuthorDtoRequest patchRequest = new AuthorDtoRequest("Name");

        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> authorService.patch(id, patchRequest));

        // then
        assertEquals(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());

        verify(authorRepository).findById(id);
    }

    @Test
    void deleteById_shouldDelete_whenAuthorExists() {
        Long id = 1L;

        when(authorRepository.existsById(id)).thenReturn(true);

        authorService.deleteById(id);

        verify(authorRepository).existsById(id);
        verify(authorRepository).deleteById(id);
    }

    @Test
    void deleteById_shouldThrowNotFound_whenAuthorDoesNotExist() {
        Long id = 99L;

        when(authorRepository.existsById(id)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authorService.deleteById(id));

        assertEquals(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());

        verify(authorRepository).existsById(id);
        verify(authorRepository, never()).deleteById(any());
    }

    @Test
    void readByNewsId_shouldReturnAuthorDto_whenAuthorFound() {
        Long newsId = 1L;
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author author = new Author("Name", dateTime, dateTime, null);
        author.setId(10L);
        AuthorDtoResponse expectedDto = new AuthorDtoResponse(10L, "Name", dateTime, dateTime);

        when(authorRepository.readByNewsId(newsId)).thenReturn(Optional.of(author));
        when(authorDtoMapper.modelToDto(author)).thenReturn(expectedDto);

        AuthorDtoResponse actualDto = authorService.readByNewsId(newsId);

        assertEquals(expectedDto, actualDto);
        verify(authorRepository).readByNewsId(newsId);
        verify(authorDtoMapper).modelToDto(author);
    }

    @Test
    void readByNewsId_shouldThrowNotFoundException_whenAuthorNotFound() {
        Long newsId = 99L;

        when(authorRepository.readByNewsId(newsId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authorService.readByNewsId(newsId));

        assertEquals(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), newsId), exception.getMessage());
        verify(authorRepository).readByNewsId(newsId);
        verifyNoInteractions(authorDtoMapper);
    }
}