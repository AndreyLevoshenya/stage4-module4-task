package com.mjc.school.service.impl;

import com.mjc.school.annotation.Valid;
import com.mjc.school.dto.NewsDtoRequest;
import com.mjc.school.dto.NewsDtoResponse;
import com.mjc.school.dto.ParametersDtoRequest;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.filter.EntitySpecification;
import com.mjc.school.mapper.NewsDtoMapper;
import com.mjc.school.model.Author;
import com.mjc.school.model.News;
import com.mjc.school.model.SearchParameters;
import com.mjc.school.model.Tag;
import com.mjc.school.repository.AuthorRepository;
import com.mjc.school.repository.NewsRepository;
import com.mjc.school.repository.TagRepository;
import com.mjc.school.service.NewsService;
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

import java.util.ArrayList;
import java.util.List;

import static com.mjc.school.exception.ExceptionErrorCodes.*;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NewsServiceImpl implements NewsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewsServiceImpl.class);

    private final NewsRepository newsRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final NewsDtoMapper newsDtoMapper;

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository, AuthorRepository authorRepository, TagRepository tagRepository, NewsDtoMapper newsDtoMapper) {
        this.newsRepository = newsRepository;
        this.authorRepository = authorRepository;
        this.tagRepository = tagRepository;
        this.newsDtoMapper = newsDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsDtoResponse> readAll(@Valid SearchingRequest searchingRequest, Pageable pageable) {
        LOGGER.info("Reading all the news for {}", searchingRequest);
        if (searchingRequest == null) {
            return newsRepository.findAll(pageable).map(newsDtoMapper::modelToDto);
        }
        String[] specs = searchingRequest.getFieldNameAndValue().split(":");
        Specification<News> specification = EntitySpecification.searchByField(specs[0], specs[1]);
        return newsRepository.findAll(specification, pageable).map(newsDtoMapper::modelToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public NewsDtoResponse readById(@Valid Long id) {
        LOGGER.info("Reading news with id {}", id);
        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("News with id {} not found", id);
                    return new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        return newsDtoMapper.modelToDto(news);
    }

    @Override
    @Transactional
    public NewsDtoResponse create(@Valid NewsDtoRequest createRequest) {
        LOGGER.info("Creating new news {}", createRequest.toString());
        if (!authorRepository.existsById(createRequest.getAuthorId())) {
            LOGGER.error("Author with id {} not found. Unable to create news", createRequest.getAuthorId());
            throw new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), createRequest.getAuthorId()));
        }
        News model = newsDtoMapper.dtoToModel(createRequest, authorRepository, tagRepository);
        return newsDtoMapper.modelToDto(newsRepository.save(model));
    }

    @Override
    @Transactional
    public NewsDtoResponse update(@Valid Long id, @Valid NewsDtoRequest updateRequest) {
        LOGGER.info("Updating news with id {}", id);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("News with id {} not found. Unable to update news", id);
                    return new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), id));
                });

        Author author = authorRepository.findById(updateRequest.getAuthorId())
                .orElseThrow(() -> {
                    LOGGER.error("Author with id {} not found. Unable to update news", updateRequest.getAuthorId());
                    return new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), updateRequest.getAuthorId()));
                });

        List<Tag> tags = new ArrayList<>();
        if (updateRequest.getTagIds() != null) {
            for (Long tagId : updateRequest.getTagIds()) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> {
                            LOGGER.error("Tag with id {} not found. Unable to update news", tagId);
                            return new NotFoundException(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), tagId));
                        });
                tags.add(tag);
            }
        }

        news.setTitle(updateRequest.getTitle());
        news.setContent(updateRequest.getContent());
        news.setAuthor(author);
        news.setTags(tags);

        News savedNews = newsRepository.save(news);
        return newsDtoMapper.modelToDto(savedNews);
    }

    @Override
    @Transactional
    public NewsDtoResponse patch(@Valid Long id, NewsDtoRequest patchRequest) {
        LOGGER.info("Patching news with id {}", id);
        String title = patchRequest.getTitle();
        String content = patchRequest.getContent();
        Long authorId = patchRequest.getAuthorId();
        List<Long> tagIds = patchRequest.getTagIds();

        News prevNews = newsRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("News with id {} not found. Unable to patch news", id);
                    return new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        if (title != null) {
            prevNews.setTitle(title);
        }
        if (content != null) {
            prevNews.setContent(content);
        }

        if (authorId != null) {
            Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> {
                        LOGGER.error("Author with id {} not found. Unable to patch news", authorId);
                        return new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), authorId));
                    });
            prevNews.setAuthor(author);
        }
        if (tagIds != null) {
            List<Tag> tags = tagIds.stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> {
                                LOGGER.error("Tag with id {} not found. Unable to patch news", tagId);
                                return new NotFoundException(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), tagId));
                            }))
                    .toList();
            prevNews.setTags(tags);
        }

        News savedNews = newsRepository.save(prevNews);
        return newsDtoMapper.modelToDto(savedNews);
    }

    @Override
    @Transactional
    public void deleteById(@Valid Long id) {
        LOGGER.info("Deleting news with id {}", id);
        if (!newsRepository.existsById(id)) {
            LOGGER.error("News with id {} not found. Unable to delete news", id);
            throw new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), id));
        }
        newsRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsDtoResponse> readByParams(ParametersDtoRequest parametersDtoRequest, Pageable pageable) {
        LOGGER.info("Reading news by params {}", parametersDtoRequest);
        SearchParameters params = new SearchParameters(
                !parametersDtoRequest.newsTitle().isEmpty() ? parametersDtoRequest.newsTitle() : null,
                !parametersDtoRequest.newsContent().isEmpty() ? parametersDtoRequest.newsContent() : null,
                !parametersDtoRequest.authorName().isEmpty() ? parametersDtoRequest.authorName() : null,
                (parametersDtoRequest.tagIds() != null && !parametersDtoRequest.tagIds().isEmpty()) ? parametersDtoRequest.tagIds() : null,
                (parametersDtoRequest.tagNames() != null && !parametersDtoRequest.tagNames().isEmpty()) ? parametersDtoRequest.tagNames() : null);
        return newsRepository.readByParams(params, pageable).map(newsDtoMapper::modelToDto);
    }
}
