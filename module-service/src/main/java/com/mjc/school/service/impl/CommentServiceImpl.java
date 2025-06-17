package com.mjc.school.service.impl;

import com.mjc.school.annotation.Valid;
import com.mjc.school.dto.CommentDtoRequest;
import com.mjc.school.dto.CommentDtoResponse;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.filter.EntitySpecification;
import com.mjc.school.mapper.CommentDtoMapper;
import com.mjc.school.mapper.NewsDtoMapper;
import com.mjc.school.model.Comment;
import com.mjc.school.repository.CommentRepository;
import com.mjc.school.repository.NewsRepository;
import com.mjc.school.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mjc.school.exception.ExceptionErrorCodes.COMMENT_DOES_NOT_EXIST;
import static com.mjc.school.exception.ExceptionErrorCodes.NEWS_DOES_NOT_EXIST;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CommentServiceImpl implements CommentService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;

    private final CommentDtoMapper commentDtoMapper;
    private final NewsDtoMapper newsDtoMapper;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, NewsRepository newsRepository, CommentDtoMapper commentDtoMapper, NewsDtoMapper newsDtoMapper) {
        this.commentRepository = commentRepository;
        this.newsRepository = newsRepository;
        this.commentDtoMapper = commentDtoMapper;
        this.newsDtoMapper = newsDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDtoResponse> readAll(@Valid SearchingRequest searchingRequest, Pageable pageable) {
        LOGGER.info("Reading all the comments for {}", searchingRequest);
        if (searchingRequest == null) {
            return commentRepository.findAll(pageable).map(comment -> commentDtoMapper.modelToDto(comment, newsDtoMapper));
        }
        String[] specs = searchingRequest.getFieldNameAndValue().split(":");
        Specification<Comment> specification = EntitySpecification.searchByField(specs[0], specs[1]);
        return commentRepository.findAll(specification, pageable).map(comment -> commentDtoMapper.modelToDto(comment, newsDtoMapper));
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDtoResponse readById(@Valid Long id) {
        LOGGER.info("Reading comment with id {}", id);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Comment with id {} not found", id);
                    return new NotFoundException(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        return commentDtoMapper.modelToDto(comment, newsDtoMapper);
    }

    @Override
    @Transactional
    public CommentDtoResponse create(@Valid CommentDtoRequest createRequest) {
        LOGGER.info("Creating comment {}", createRequest.toString());
        if (!newsRepository.existsById(createRequest.getNewsId())) {
            LOGGER.error("News with id {} not found. Unable to create comment", createRequest.getNewsId());
            throw new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), createRequest.getNewsId()));
        }
        Comment model = commentDtoMapper.dtoToModel(createRequest, newsRepository);
        return commentDtoMapper.modelToDto(commentRepository.save(model), newsDtoMapper);
    }

    @Override
    @Transactional
    public CommentDtoResponse update(@Valid Long id, @Valid CommentDtoRequest updateRequest) {
        LOGGER.info("Updating comment with id {}", id);
        Comment prevComment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Comment with id {} not found. Unable to update comment", id);
                    return new NotFoundException(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        prevComment.setContent(updateRequest.getContent());

        return commentDtoMapper.modelToDto(commentRepository.save(prevComment), newsDtoMapper);
    }

    @Override
    @Transactional
    public CommentDtoResponse patch(@Valid Long id, @Valid CommentDtoRequest patchRequest) {
        LOGGER.info("Patching comment with id {}", id);
        String content = patchRequest.getContent();

        Comment prevComment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Comment with id {} not found. Unable to patch comment", id);
                    return new NotFoundException(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        if (content != null) {
            prevComment.setContent(content);
        }

        Comment savedComment = commentRepository.save(prevComment);
        return commentDtoMapper.modelToDto(savedComment, newsDtoMapper);
    }

    @Override
    @Transactional
    public void deleteById(@Valid Long id) {
        LOGGER.info("Deleting comment with id {}", id);
        if (!commentRepository.existsById(id)) {
            LOGGER.error("Comment with id {} not found. Unable to delete comment", id);
            throw new NotFoundException(String.format(COMMENT_DOES_NOT_EXIST.getErrorMessage(), id));
        }
        commentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDtoResponse> readByNewsId(@Valid Long newsId, Pageable pageable) {
        LOGGER.info("Reading comments by news id {}", newsId);
        if (!newsRepository.existsById(newsId)) {
            LOGGER.error("News with id {} not found. Unable to read comments", newsId);
            throw new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), newsId));
        }
        return commentRepository.readByNewsId(newsId, pageable).map(comment -> commentDtoMapper.modelToDto(comment, newsDtoMapper));
    }
}
