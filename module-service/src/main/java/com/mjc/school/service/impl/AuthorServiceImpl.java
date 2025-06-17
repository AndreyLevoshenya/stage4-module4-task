package com.mjc.school.service.impl;

import com.mjc.school.annotation.Valid;
import com.mjc.school.dto.AuthorDtoRequest;
import com.mjc.school.dto.AuthorDtoResponse;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.exception.NotFoundException;
import com.mjc.school.filter.EntitySpecification;
import com.mjc.school.mapper.AuthorDtoMapper;
import com.mjc.school.model.Author;
import com.mjc.school.repository.AuthorRepository;
import com.mjc.school.service.AuthorService;
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

import static com.mjc.school.exception.ExceptionErrorCodes.AUTHOR_DOES_NOT_EXIST;
import static com.mjc.school.exception.ExceptionErrorCodes.NEWS_DOES_NOT_EXIST;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthorServiceImpl implements AuthorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorServiceImpl.class);

    private final AuthorRepository authorRepository;

    private final AuthorDtoMapper authorDtoMapper;

    @Autowired
    public AuthorServiceImpl(AuthorRepository authorRepository, AuthorDtoMapper authorDtoMapper) {
        this.authorRepository = authorRepository;
        this.authorDtoMapper = authorDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorDtoResponse> readAll(@Valid SearchingRequest searchingRequest, Pageable pageable) {
        LOGGER.info("Reading all authors for {}", searchingRequest);
        if (searchingRequest == null) {
            return authorRepository.findAll(pageable).map(authorDtoMapper::modelToDto);
        }
        String[] specs = searchingRequest.getFieldNameAndValue().split(":");
        Specification<Author> specification = EntitySpecification.searchByField(specs[0], specs[1]);
        return authorRepository.findAll(specification, pageable).map(authorDtoMapper::modelToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorDtoResponse readById(@Valid Long id) {
        LOGGER.info("Reading author by id {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("Author with id {} not found", id);
                    return new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id));
                });
        return authorDtoMapper.modelToDto(author);
    }

    @Override
    @Transactional
    public AuthorDtoResponse create(@Valid AuthorDtoRequest createRequest) {
        LOGGER.info("Creating author {}", createRequest.toString());

        Author model = authorDtoMapper.dtoToModel(createRequest);
        Author author = authorRepository.save(model);
        return authorDtoMapper.modelToDto(author);
    }

    @Override
    @Transactional
    public AuthorDtoResponse update(@Valid Long id, @Valid AuthorDtoRequest updateRequest) {
        LOGGER.info("Updating author with id {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("Author with id {} not found. Unable to update author", id);
                    return new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id));
                });

        author.setName(updateRequest.getName());
        return authorDtoMapper.modelToDto(authorRepository.save(author));
    }

    @Override
    @Transactional
    public AuthorDtoResponse patch(@Valid Long id, @Valid AuthorDtoRequest patchRequest) {
        LOGGER.info("Patching author with id {}", id);
        String name = patchRequest.getName();

        Author prevAuthor = authorRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("Author with id {} not found. Unable to patch author", id);
                    return new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id));
                });

        if (name != null) {
            prevAuthor.setName(name);
        }

        Author savedAuthor = authorRepository.save(prevAuthor);
        return authorDtoMapper.modelToDto(savedAuthor);
    }

    @Override
    @Transactional
    public void deleteById(@Valid Long id) {
        LOGGER.info("Deleting author by id {}", id);
        if (!authorRepository.existsById(id)) {
            LOGGER.warn("Author with id {} not found. Unable to delete author", id);
            throw new NotFoundException(String.format(AUTHOR_DOES_NOT_EXIST.getErrorMessage(), id));
        }
        authorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorDtoResponse readByNewsId(@Valid Long newsId) {
        LOGGER.info("Reading author by news id {}", newsId);
        Author author = authorRepository.readByNewsId(newsId).orElseThrow(() -> {
            LOGGER.warn("News with id {} not found", newsId);
            return new NotFoundException(String.format(NEWS_DOES_NOT_EXIST.getErrorMessage(), newsId));
        });
        return authorDtoMapper.modelToDto(author);
    }
}
