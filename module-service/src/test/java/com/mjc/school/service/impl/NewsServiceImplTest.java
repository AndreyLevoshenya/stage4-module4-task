package com.mjc.school.service.impl;

import com.mjc.school.dto.AuthorDtoResponse;
import com.mjc.school.dto.NewsDtoRequest;
import com.mjc.school.dto.NewsDtoResponse;
import com.mjc.school.dto.ParametersDtoRequest;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.mapper.NewsDtoMapper;
import com.mjc.school.model.Author;
import com.mjc.school.model.News;
import com.mjc.school.model.SearchParameters;
import com.mjc.school.model.Tag;
import com.mjc.school.repository.AuthorRepository;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.mjc.school.exception.ExceptionErrorCodes.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {
    @Mock
    private NewsRepository newsRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private NewsDtoMapper newsDtoMapper;

    @InjectMocks
    private NewsServiceImpl newsService;

    @Test
    void readAll_shouldReturnAllNews() {
        Pageable pageable = PageRequest.of(0, 10);
        List<News> newsList = List.of(new News());
        Page<News> page = new PageImpl<>(newsList);
        when(newsRepository.findAll(pageable)).thenReturn(page);
        when(newsDtoMapper.modelToDto(any())).thenReturn(new NewsDtoResponse());

        Page<NewsDtoResponse> result = newsService.readAll(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(newsRepository).findAll(pageable);
    }

    @Test
    void readById_shouldReturnNews_whenExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        News news = new News("title", "content", dateTime, dateTime, null, null, null);
        news.setId(1L);
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsDtoMapper.modelToDto(news)).thenReturn(new NewsDtoResponse(1L, "title", "content", dateTime, dateTime, null, null));

        NewsDtoResponse result = newsService.readById(1L);

        assertThat(result).isNotNull();
        verify(newsRepository).findById(1L);
    }

    @Test
    void readById_shouldThrow_whenNotExists() {
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsService.readById(1L));
    }

    @Test
    void create_shouldSaveNews_whenAuthorExists() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        NewsDtoRequest request = new NewsDtoRequest("title", "content", 1L, null);
        News news = new News("title", "content", dateTime, dateTime, null, null, null);
        news.setAuthor(new Author());
        news.getAuthor().setId(1L);
        when(authorRepository.existsById(1L)).thenReturn(true);
        when(newsDtoMapper.dtoToModel(any(), any(), any())).thenReturn(news);
        when(newsRepository.save(news)).thenReturn(news);
        when(newsDtoMapper.modelToDto(news)).thenReturn(new NewsDtoResponse());

        NewsDtoResponse result = newsService.create(request);

        assertThat(result).isNotNull();
        verify(newsRepository).save(news);
    }

    @Test
    void create_shouldThrow_whenAuthorNotExists() {
        NewsDtoRequest request = new NewsDtoRequest();
        Long id = 99L;
        request.setAuthorId(id);
        when(authorRepository.existsById(id)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> newsService.create(request));
        assertEquals(exception.getMessage(), String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id));

    }

    @Test
    void update_shouldUpdateNews_whenValid() {
        NewsDtoRequest request = new NewsDtoRequest();
        request.setAuthorId(2L);
        request.setTagIds(List.of(3L));
        News news = new News();
        news.setId(1L);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(new Author()));
        when(tagRepository.findById(3L)).thenReturn(Optional.of(new Tag()));
        when(newsRepository.save(news)).thenReturn(news);
        when(newsDtoMapper.modelToDto(news)).thenReturn(new NewsDtoResponse());

        NewsDtoResponse result = newsService.update(1L, request);

        assertThat(result).isNotNull();
    }

    @Test
    void update_shouldThrow_whenNewsNotExists() {
        NewsDtoRequest request = new NewsDtoRequest();
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> newsService.update(1L, request));
        assertEquals(exception.getMessage(), String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), 1L));
    }

    @Test
    void update_shouldThrow_whenAuthorNotExists() {
        NewsDtoRequest request = new NewsDtoRequest();
        request.setAuthorId(2L);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(new News()));
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> newsService.update(1L, request));
        assertEquals(exception.getMessage(), String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), 2L));
    }

    @Test
    void update_shouldThrow_whenTagNotExists() {
        NewsDtoRequest request = new NewsDtoRequest();
        request.setAuthorId(2L);
        request.setTagIds(List.of(3L));

        when(newsRepository.findById(1L)).thenReturn(Optional.of(new News()));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(new Author()));
        when(tagRepository.findById(3L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> newsService.update(1L, request));
        assertEquals(exception.getMessage(), String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), 3L));
    }


    @Test
    void patch_whenNewsNotFound_throwsNotFoundException() {
        NewsDtoRequest patchRequest = new NewsDtoRequest();

        when(newsRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> newsService.patch(10L, patchRequest));

        assertTrue(exception.getMessage().contains("does not exist"));
        verify(newsRepository).findById(10L);
        verifyNoMoreInteractions(newsRepository, authorRepository, tagRepository, newsDtoMapper);
    }

    @Test
    void patch_whenAllFieldsNull_shouldNotChangeExistingNewsFields() {
        NewsDtoRequest patchRequest = new NewsDtoRequest();
        patchRequest.setTitle(null);
        patchRequest.setContent(null);
        patchRequest.setAuthorId(null);
        patchRequest.setTagIds(null);

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author author = new Author();
        author.setId(1L);
        News existingNews = new News("Old title", "Old content", dateTime, dateTime, author, null, null);
        existingNews.setId(10L);
        NewsDtoResponse expectedResponse = new NewsDtoResponse(10L, "Old title", "Old content", dateTime, dateTime, new AuthorDtoResponse(), null);
        expectedResponse.getAuthorDtoResponse().setId(1L);

        when(newsRepository.findById(10L)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(existingNews)).thenReturn(existingNews);
        when(newsDtoMapper.modelToDto(existingNews)).thenReturn(expectedResponse);

        NewsDtoResponse actual = newsService.patch(10L, patchRequest);

        assertSame(expectedResponse, actual);

        assertEquals("Old title", existingNews.getTitle());
        assertEquals("Old content", existingNews.getContent());
        assertEquals(author, existingNews.getAuthor());
        assertNull(existingNews.getTags());

        verify(newsRepository).findById(10L);
        verify(newsRepository).save(existingNews);
        verify(newsDtoMapper).modelToDto(existingNews);

        verifyNoInteractions(authorRepository, tagRepository);
    }

    @Test
    void patch_whenTitleAndContentProvided_shouldUpdateThem() {
        NewsDtoRequest patchRequest = new NewsDtoRequest();
        patchRequest.setTitle("New title");
        patchRequest.setContent("New content");

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author author = new Author();
        author.setId(1L);
        News existingNews = new News("Old title", "Old content", dateTime, dateTime, author, null, null);
        existingNews.setId(10L);
        NewsDtoResponse expectedResponse = new NewsDtoResponse(1L, "New title", "New content", dateTime, dateTime, new AuthorDtoResponse(), null);
        expectedResponse.getAuthorDtoResponse().setId(1L);

        when(newsRepository.findById(10L)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(existingNews)).thenReturn(existingNews);
        when(newsDtoMapper.modelToDto(existingNews)).thenReturn(expectedResponse);

        NewsDtoResponse actual = newsService.patch(10L, patchRequest);

        assertSame(expectedResponse, actual);
        assertEquals("New title", existingNews.getTitle());
        assertEquals("New content", existingNews.getContent());

        verify(newsRepository).findById(10L);
        verify(newsRepository).save(existingNews);
        verify(newsDtoMapper).modelToDto(existingNews);
        verifyNoInteractions(authorRepository, tagRepository);
    }

    @Test
    void patch_whenAuthorIdProvided_andAuthorExists_shouldUpdateAuthor() {
        NewsDtoRequest patchRequest = new NewsDtoRequest();
        patchRequest.setAuthorId(2L);

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Author newAuthor = new Author();
        newAuthor.setId(2L);
        News existingNews = new News("Old title", "Old content", dateTime, dateTime, null, null, null);
        existingNews.setId(10L);
        NewsDtoResponse expectedResponse = new NewsDtoResponse(1L, "New title", "New content", dateTime, dateTime, new AuthorDtoResponse(), null);
        expectedResponse.getAuthorDtoResponse().setId(2L);

        when(newsRepository.findById(10L)).thenReturn(Optional.of(existingNews));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(newAuthor));
        when(newsRepository.save(existingNews)).thenReturn(existingNews);
        when(newsDtoMapper.modelToDto(existingNews)).thenReturn(expectedResponse);

        NewsDtoResponse actual = newsService.patch(10L, patchRequest);

        assertSame(expectedResponse, actual);
        assertEquals(newAuthor, existingNews.getAuthor());

        verify(newsRepository).findById(10L);
        verify(authorRepository).findById(2L);
        verify(newsRepository).save(existingNews);
        verify(newsDtoMapper).modelToDto(existingNews);
        verifyNoInteractions(tagRepository);
    }

    @Test
    void patch_whenAuthorIdProvided_andAuthorNotFound_shouldThrow() {
        NewsDtoRequest patchRequest = new NewsDtoRequest();
        patchRequest.setAuthorId(999L);

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        News existingNews = new News("Old title", "Old content", dateTime, dateTime, null, null, null);
        existingNews.setId(10L);

        when(newsRepository.findById(10L)).thenReturn(Optional.of(existingNews));
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> newsService.patch(10L, patchRequest));

        assertEquals(exception.getMessage(), String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), 999L));

        verify(newsRepository).findById(10L);
        verify(authorRepository).findById(999L);
        verifyNoMoreInteractions(newsRepository, authorRepository, tagRepository, newsDtoMapper);
    }

    @Test
    void patch_whenTagIdsProvided_andAllTagsExist_shouldUpdateTags() {
        NewsDtoRequest patchRequest = new NewsDtoRequest();
        patchRequest.setTagIds(List.of(1L, 2L));

        List<Tag> tagsFromDb = List.of(new Tag("tag1"), new Tag("tag2"));
        tagsFromDb.get(0).setId(1L);
        tagsFromDb.get(1).setId(2L);

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        News existingNews = new News("title", "content", dateTime, dateTime, null, null, null);
        NewsDtoResponse expectedResponse = new NewsDtoResponse(1L, "title", "content", dateTime, dateTime, null, null);
        existingNews.setId(10L);

        when(newsRepository.findById(10L)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(existingNews)).thenReturn(existingNews);
        when(newsDtoMapper.modelToDto(existingNews)).thenReturn(expectedResponse);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tagsFromDb.get(0)));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tagsFromDb.get(1)));

        NewsDtoResponse actual = newsService.patch(10L, patchRequest);

        assertSame(expectedResponse, actual);
        assertEquals(tagsFromDb, existingNews.getTags());

        verify(newsRepository).findById(10L);
        verify(tagRepository).findById(1L);
        verify(tagRepository).findById(2L);
        verify(newsRepository).save(existingNews);
        verify(newsDtoMapper).modelToDto(existingNews);
        verifyNoInteractions(authorRepository);
    }

    @Test
    void patch_whenTagIdsProvided_andSomeTagsMissing_shouldThrow() {
        NewsDtoRequest patchRequest = new NewsDtoRequest();
        patchRequest.setTagIds(List.of(1L, 2L));

        List<Tag> incompleteTagsList = List.of(new Tag("tag1"));
        incompleteTagsList.get(0).setId(1L);

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        News existingNews = new News("title", "content", dateTime, dateTime, null, null, null);
        existingNews.setId(10L);

        when(newsRepository.findById(10L)).thenReturn(Optional.of(existingNews));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(incompleteTagsList.get(0)));
        when(tagRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> newsService.patch(10L, patchRequest));

        assertEquals(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), 2L), exception.getMessage());

        verify(newsRepository).findById(10L);
        verify(tagRepository).findById(1L);
        verify(tagRepository).findById(2L);
        verifyNoMoreInteractions(newsRepository, authorRepository, tagRepository, newsDtoMapper);
    }

    @Test
    void deleteById_whenNewsExists_shouldDeleteSuccessfully() {
        Long id = 1L;

        when(newsRepository.existsById(id)).thenReturn(true);

        newsService.deleteById(id);

        verify(newsRepository).existsById(id);
        verify(newsRepository).deleteById(id);
    }

    @Test
    void deleteById_whenNewsDoesNotExist_shouldThrowNotFoundException() {
        Long id = 2L;

        when(newsRepository.existsById(id)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> newsService.deleteById(id));

        assertEquals(exception.getMessage(), String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), id));

        verify(newsRepository).existsById(id);
        verify(newsRepository, never()).deleteById(any());
    }

    @Test
    void readByParams_allFieldsPresent_shouldReturnMappedPage() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "title",
                "content",
                "author",
                List.of(1, 2),
                List.of("tag1", "tag2")
        );

        Pageable pageable = PageRequest.of(0, 10);

        News news = new News();
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        NewsDtoResponse dto = new NewsDtoResponse(1L, "title", "content", dateTime, dateTime, null, List.of());

        Page<News> newsPage = new PageImpl<>(List.of(news));

        when(newsRepository.readByParams(any(SearchParameters.class), eq(pageable))).thenReturn(newsPage);
        when(newsDtoMapper.modelToDto(news)).thenReturn(dto);

        Page<NewsDtoResponse> result = newsService.readByParams(request, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(newsRepository).readByParams(any(SearchParameters.class), eq(pageable));
        verify(newsDtoMapper).modelToDto(news);
    }

    @Test
    void readByParams_withEmptyOptionalFields_shouldStillWork() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "",
                "",
                "",
                List.of(),
                null
        );

        Pageable pageable = PageRequest.of(0, 10);

        News news = new News();
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        NewsDtoResponse dtoResponse = new NewsDtoResponse(1L, "title", "content", dateTime, dateTime, null, null);

        Page<News> newsPage = new PageImpl<>(List.of(news));

        when(newsRepository.readByParams(any(SearchParameters.class), eq(pageable))).thenReturn(newsPage);
        when(newsDtoMapper.modelToDto(news)).thenReturn(dtoResponse);

        Page<NewsDtoResponse> result = newsService.readByParams(request, pageable);

        assertEquals(1, result.getTotalElements());
        verify(newsRepository).readByParams(any(SearchParameters.class), eq(pageable));
    }

    @Test
    void readByParams_emptyResult_shouldReturnEmptyPage() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "nonexistent",
                "",
                "",
                null,
                null
        );

        Pageable pageable = PageRequest.of(0, 5);

        when(newsRepository.readByParams(any(SearchParameters.class), eq(pageable)))
                .thenReturn(Page.empty());

        Page<NewsDtoResponse> result = newsService.readByParams(request, pageable);

        assertTrue(result.isEmpty());
        verify(newsRepository).readByParams(any(SearchParameters.class), eq(pageable));
    }
}