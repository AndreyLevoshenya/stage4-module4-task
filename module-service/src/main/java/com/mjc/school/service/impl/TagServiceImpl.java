package com.mjc.school.service.impl;

import com.mjc.school.annotation.Valid;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.dto.TagDtoRequest;
import com.mjc.school.dto.TagDtoResponse;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.filter.EntitySpecification;
import com.mjc.school.mapper.TagDtoMapper;
import com.mjc.school.model.Tag;
import com.mjc.school.repository.NewsRepository;
import com.mjc.school.repository.TagRepository;
import com.mjc.school.service.TagService;
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

import static com.mjc.school.exception.ExceptionErrorCodes.NEWS_DOES_NOT_EXIST;
import static com.mjc.school.exception.ExceptionErrorCodes.TAG_DOES_NOT_EXIST;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TagServiceImpl implements TagService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;
    private final NewsRepository newsRepository;

    private final TagDtoMapper tagDtoMapper;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, NewsRepository newsRepository, TagDtoMapper tagDtoMapper) {
        this.tagRepository = tagRepository;
        this.newsRepository = newsRepository;
        this.tagDtoMapper = tagDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDtoResponse> readAll(@Valid SearchingRequest searchingRequest, Pageable pageable) {
        LOGGER.info("Reading all the tags for {}", searchingRequest);
        if (searchingRequest == null) {
            return tagRepository.findAll(pageable).map(tagDtoMapper::modelToDto);
        }
        String[] specs = searchingRequest.getFieldNameAndValue().split(":");
        Specification<Tag> specification = EntitySpecification.searchByField(specs[0], specs[1]);
        return tagRepository.findAll(specification, pageable).map(tagDtoMapper::modelToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDtoResponse readById(@Valid Long id) {
        LOGGER.info("Reading a tag by id {}", id);
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Tag with id {} not found", id);
                    return new NotFoundException(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        return tagDtoMapper.modelToDto(tag);
    }

    @Override
    @Transactional
    public TagDtoResponse create(@Valid TagDtoRequest createRequest) {
        LOGGER.info("Creating a new tag {}", createRequest.toString());
        Tag tag = tagDtoMapper.dtoToModel(createRequest);
        return tagDtoMapper.modelToDto(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public TagDtoResponse update(@Valid Long id, @Valid TagDtoRequest updateRequest) {
        LOGGER.info("Updating a tag with id {}", id);
        Tag prevTag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Tag with id {} not found. Unable to update tag", id);
                    return new NotFoundException(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        prevTag.setName(updateRequest.getName());
        return tagDtoMapper.modelToDto(tagRepository.save(prevTag));
    }

    @Override
    @Transactional
    public TagDtoResponse patch(@Valid Long id, @Valid TagDtoRequest patchRequest) {
        LOGGER.info("Patching a tag with id {}", id);
        String name = patchRequest.getName();

        Tag prevTag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Tag with id {} not found. Unable to patch tag", id);
                    return new NotFoundException(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        if (name != null) {
            prevTag.setName(name);
        }

        Tag savedTag = tagRepository.save(prevTag);
        return tagDtoMapper.modelToDto(savedTag);
    }

    @Override
    @Transactional
    public void deleteById(@Valid Long id) {
        LOGGER.info("Deleting a tag with id {}", id);
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Tag with id {} not found. Unable to delete tag", id);
                    return new NotFoundException(String.format(TAG_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        tag.getNews().forEach(news -> news.getTags().remove(tag));

        tag.getNews().clear();

        tagRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagDtoResponse> readByNewsId(@Valid Long newsId, Pageable pageable) {
        LOGGER.info("Reading tags by news id {}", newsId);
        if (!newsRepository.existsById(newsId)) {
            LOGGER.error("News with id {} not found. Unable to read tags", newsId);
            throw new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), newsId));
        }
        return tagRepository.readByNewsId(newsId, pageable).map(tagDtoMapper::modelToDto);
    }
}
