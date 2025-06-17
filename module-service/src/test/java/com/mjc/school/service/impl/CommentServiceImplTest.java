package com.mjc.school.service.impl;

import com.mjc.school.dto.CommentDtoRequest;
import com.mjc.school.dto.CommentDtoResponse;
import com.mjc.school.dto.NewsDtoResponse;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.filter.EntitySpecification;
import com.mjc.school.mapper.CommentDtoMapper;
import com.mjc.school.mapper.NewsDtoMapper;
import com.mjc.school.model.Comment;
import com.mjc.school.model.News;
import com.mjc.school.repository.CommentRepository;
import com.mjc.school.repository.NewsRepository;
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
import java.util.List;
import java.util.Optional;

import static com.mjc.school.exception.ExceptionErrorCodes.COMMENT_DOES_NOT_EXIST;
import static com.mjc.school.exception.ExceptionErrorCodes.NEWS_DOES_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private NewsRepository newsRepository;

    @Mock
    private CommentDtoMapper commentDtoMapper;
    @Mock
    private NewsDtoMapper newsDtoMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void readAll_shouldReturnAllComments_whenSearchingRequestIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Comment> comments = List.of(new Comment(), new Comment());
        comments.get(0).setId(1L);
        comments.get(1).setId(2L);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        CommentDtoResponse dtoResponse = new CommentDtoResponse();

        when(commentRepository.findAll(pageable)).thenReturn(commentPage);
        when(commentDtoMapper.modelToDto(comments.get(0), newsDtoMapper)).thenReturn(dtoResponse);

        Page<CommentDtoResponse> result = commentService.readAll(null, pageable);

        assertEquals(comments.size(), result.getContent().size());
        verify(commentRepository).findAll(pageable);
        verify(commentDtoMapper, times(comments.size())).modelToDto(any(Comment.class), any());
    }

    @Test
    void readAll_shouldReturnFilteredComments_whenSearchingRequestIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchingRequest searchingRequest = new SearchingRequest("content:John");
        String[] specs = searchingRequest.getFieldNameAndValue().split(":");
        Specification<Comment> specification = EntitySpecification.searchByField(specs[0], specs[1]);

        List<Comment> comments = List.of(new Comment(), new Comment());
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(commentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(commentPage);
        when(commentDtoMapper.modelToDto(any(Comment.class), any())).thenAnswer(invocation -> new CommentDtoResponse());

        Page<CommentDtoResponse> result = commentService.readAll(searchingRequest, pageable);

        assertEquals(comments.size(), result.getContent().size());
        verify(commentRepository).findAll(any(Specification.class), eq(pageable));
        verify(commentDtoMapper, times(comments.size())).modelToDto(any(Comment.class), any());
    }

    @Test
    void readById_shouldReturnCommentDto_whenCommentExists() {
        Long id = 1L;
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Comment comment = new Comment("Content", null, date, date);
        CommentDtoResponse expectedDto = new CommentDtoResponse(id, "Content", null, date, date);

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(commentDtoMapper.modelToDto(comment, newsDtoMapper)).thenReturn(expectedDto);

        CommentDtoResponse actualDto = commentService.readById(id);

        assertEquals(expectedDto, actualDto);
        verify(commentRepository).findById(id);
        verify(commentDtoMapper).modelToDto(comment, newsDtoMapper);
    }

    @Test
    void readById_shouldThrowNotFoundException_whenCommentDoesNotExist() {
        Long id = 99L;

        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.readById(id));

        assertEquals(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());
        verify(commentRepository).findById(id);
        verifyNoInteractions(commentDtoMapper);
    }

    @Test
    void create_shouldSaveAndReturnCommentDto() {
        Long id = 1L;
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        CommentDtoRequest createRequest = new CommentDtoRequest("Content", id);
        Comment model = new Comment("Content", new News(), date, date);
        model.getNews().setId(id);
        Comment savedComment = new Comment("Content", new News(), date, date);
        savedComment.setId(id);
        savedComment.getNews().setId(id);
        CommentDtoResponse expectedDto = new CommentDtoResponse(id, "Content", new NewsDtoResponse(), date, date);
        expectedDto.getNewsDtoResponse().setId(id);
        when(newsRepository.existsById(id)).thenReturn(true);
        when(commentDtoMapper.dtoToModel(createRequest, newsRepository)).thenReturn(model);
        when(commentRepository.save(model)).thenReturn(savedComment);
        when(commentDtoMapper.modelToDto(savedComment, newsDtoMapper)).thenReturn(expectedDto);

        CommentDtoResponse actualDto = commentService.create(createRequest);

        assertEquals(expectedDto, actualDto);
        verify(newsRepository).existsById(id);
        verify(commentDtoMapper).dtoToModel(createRequest, newsRepository);
        verify(commentRepository).save(model);
        verify(commentDtoMapper).modelToDto(savedComment, newsDtoMapper);
    }

    @Test
    void create_shouldThrowNotFoundException_whenNewsNotFound() {
        CommentDtoRequest request = new CommentDtoRequest();
        request.setNewsId(123L);

        when(newsRepository.existsById(123L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> commentService.create(request));

        assertEquals(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), 123L), exception.getMessage());

        verify(commentDtoMapper, never()).dtoToModel(any(), any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void update_shouldReturnUpdatedCommentDto_whenCommentExists() {
        Long id = 1L;
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        CommentDtoRequest updateRequest = new CommentDtoRequest();
        updateRequest.setContent("Content");

        Comment comment = new Comment("Content", null, date, date);
        Comment savedComment = new Comment("Content", null, date, date);
        savedComment.setId(id);

        CommentDtoResponse expectedDto = new CommentDtoResponse(id, "Content", null, date, date);

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(savedComment);
        when(commentDtoMapper.modelToDto(savedComment, newsDtoMapper)).thenReturn(expectedDto);

        CommentDtoResponse actualDto = commentService.update(id, updateRequest);

        assertEquals(expectedDto, actualDto);
        verify(commentRepository).findById(id);
        verify(commentRepository).save(comment);
        verify(commentDtoMapper).modelToDto(savedComment, newsDtoMapper);
    }

    @Test
    void update_shouldThrowNotFoundException_whenCommentDoesNotExist() {
        Long id = 1L;
        CommentDtoRequest updateRequest = new CommentDtoRequest();

        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.update(id, updateRequest));

        assertEquals(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());
        verify(commentRepository).findById(id);
        verifyNoMoreInteractions(commentRepository);
        verifyNoInteractions(commentDtoMapper);
    }

    @Test
    void patch_shouldUpdateContentAndReturnDto_whenCommentExists() {
        Long id = 1L;
        String newContent = "Updated content";
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        CommentDtoRequest patchRequest = new CommentDtoRequest();
        patchRequest.setContent(newContent);

        Comment prevComment = new Comment("Old content", null, date, date);
        Comment savedComment = new Comment(newContent, null, date, date);
        savedComment.setId(id);

        CommentDtoResponse expectedDto = new CommentDtoResponse(id, newContent, null, date, date);

        when(commentRepository.findById(id)).thenReturn(Optional.of(prevComment));
        when(commentRepository.save(prevComment)).thenReturn(savedComment);
        when(commentDtoMapper.modelToDto(savedComment, newsDtoMapper)).thenReturn(expectedDto);

        CommentDtoResponse actualDto = commentService.patch(id, patchRequest);

        assertEquals(expectedDto, actualDto);
        assertEquals(newContent, prevComment.getContent());

        verify(commentRepository).findById(id);
        verify(commentRepository).save(prevComment);
        verify(commentDtoMapper).modelToDto(savedComment, newsDtoMapper);
    }

    @Test
    void patch_shouldNotUpdateContent_whenContentIsNull() {
        // given
        Long id = 1L;
        String originalContent = "Original Content";
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Comment existingComment = new Comment(originalContent, null, dateTime, dateTime);
        existingComment.setId(id);

        CommentDtoRequest patchRequest = new CommentDtoRequest(null, null);

        CommentDtoResponse expectedResponse = new CommentDtoResponse(id, originalContent, null, dateTime, dateTime);

        when(commentRepository.findById(id)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(existingComment)).thenReturn(existingComment);
        when(commentDtoMapper.modelToDto(existingComment, newsDtoMapper)).thenReturn(expectedResponse);

        // when
        CommentDtoResponse actualResponse = commentService.patch(id, patchRequest);

        // then
        assertEquals(expectedResponse, actualResponse);
        assertEquals(originalContent, existingComment.getContent());
        verify(commentRepository).findById(id);
        verify(commentRepository).save(existingComment);
        verify(commentDtoMapper).modelToDto(existingComment, newsDtoMapper);
    }

    @Test
    void patch_shouldThrowNotFoundException_whenCommentDoesNotExist() {
        Long id = 99L;
        CommentDtoRequest patchRequest = new CommentDtoRequest();

        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> commentService.patch(id, patchRequest));

        assertEquals(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());

        verify(commentRepository).findById(id);
        verifyNoMoreInteractions(commentRepository);
        verifyNoInteractions(commentDtoMapper);
    }

    @Test
    void deleteById_shouldDelete_whenAuthorExists() {
        Long id = 1L;

        when(commentRepository.existsById(id)).thenReturn(true);

        commentService.deleteById(id);

        verify(commentRepository).existsById(id);
        verify(commentRepository).deleteById(id);
    }

    @Test
    void deleteById_shouldThrowNotFound_whenAuthorDoesNotExist() {
        Long id = 99L;

        when(commentRepository.existsById(id)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.deleteById(id));

        assertEquals(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id), exception.getMessage());

        verify(commentRepository).existsById(id);
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void readByNewsId_shouldReturnPageOfComments_whenNewsExists() {
        // Given
        Long newsId = 1L;
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment = new Comment("Content", new News(), dateTime, dateTime);
        comment.getNews().setId(newsId);
        CommentDtoResponse dtoResponse = new CommentDtoResponse(1L, "Content", null, comment.getCreateDate(), comment.getLastUpdateDate());
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));

        when(newsRepository.existsById(newsId)).thenReturn(true);
        when(commentRepository.readByNewsId(newsId, pageable)).thenReturn(commentPage);
        when(commentDtoMapper.modelToDto(comment, newsDtoMapper)).thenReturn(dtoResponse);

        // When
        Page<CommentDtoResponse> result = commentService.readByNewsId(newsId, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(dtoResponse, result.getContent().get(0));
        verify(newsRepository).existsById(newsId);
        verify(commentRepository).readByNewsId(newsId, pageable);
        verify(commentDtoMapper).modelToDto(comment, newsDtoMapper);
    }

    @Test
    void readByNewsId_shouldThrowNotFoundException_whenNewsNotFound() {
        // Given
        Long newsId = 42L;
        Pageable pageable = PageRequest.of(0, 10);

        when(newsRepository.existsById(newsId)).thenReturn(false);

        // When
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.readByNewsId(newsId, pageable)
        );

        // Then
        assertEquals(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), newsId), exception.getMessage());
        verify(newsRepository).existsById(newsId);
        verifyNoMoreInteractions(commentRepository);
        verifyNoInteractions(commentDtoMapper);
    }

}