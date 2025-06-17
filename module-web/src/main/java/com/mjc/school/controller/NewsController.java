package com.mjc.school.controller;

import com.mjc.school.service.AuthorService;
import com.mjc.school.service.CommentService;
import com.mjc.school.service.NewsService;
import com.mjc.school.service.TagService;
import com.mjc.school.dto.AuthorDtoResponse;
import com.mjc.school.dto.CommentDtoResponse;
import com.mjc.school.dto.NewsDtoRequest;
import com.mjc.school.dto.NewsDtoResponse;
import com.mjc.school.dto.ParametersDtoRequest;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.dto.TagDtoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.mjc.school.controller.RestConstants.NEWS_V1_API_PATH;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = NEWS_V1_API_PATH)
public class NewsController implements BaseController<NewsDtoRequest, NewsDtoResponse, Long> {
    private final NewsService newsService;
    private final AuthorService authorService;
    private final TagService tagService;
    private final CommentService commentService;

    @Autowired
    public NewsController(NewsService newsService, AuthorService authorService, TagService tagService, CommentService commentService) {
        this.newsService = newsService;
        this.authorService = authorService;
        this.tagService = tagService;
        this.commentService = commentService;
    }

    private static void setLinks(NewsDtoResponse newsDtoResponse) {
        Link selfRel = linkTo(NewsController.class).slash(newsDtoResponse.getId()).withSelfRel();
        newsDtoResponse.add(selfRel);
        Link authorRel = linkTo(AuthorController.class).slash(newsDtoResponse.getAuthorDtoResponse().getId()).withSelfRel();
        newsDtoResponse.getAuthorDtoResponse().add(authorRel);
        for (TagDtoResponse tagDtoResponse : newsDtoResponse.getTagDtoResponseList()) {
            Link tagRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
            tagDtoResponse.add(tagRel);
        }
    }

    @Override
    @Operation(summary = "View all news")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all news"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<NewsDtoResponse>> readAll(
            @RequestParam(name = "searchBy", required = false) String searchBy,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @PageableDefault(sort = "title", direction = Sort.Direction.DESC) Pageable pageable) {
        SearchingRequest searchingRequest = null;
        if (searchBy != null && !searchBy.isBlank() && searchValue != null && !searchValue.isBlank()) {
            searchingRequest = new SearchingRequest(searchBy + ":" + searchValue);
        }
        Page<NewsDtoResponse> page = newsService.readAll(searchingRequest, pageable);
        for (NewsDtoResponse newsDtoResponse : page.stream().toList()) {
            setLinks(newsDtoResponse);
        }
        return new ResponseEntity<>(page, OK);
    }

    @Override
    @Operation(summary = "Get news by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved news by id"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<NewsDtoResponse> readById(@PathVariable Long id) {
        NewsDtoResponse newsDtoResponse = newsService.readById(id);
        setLinks(newsDtoResponse);
        return new ResponseEntity<>(newsDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Create news")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a news"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<NewsDtoResponse> create(@RequestBody NewsDtoRequest createRequest) {
        NewsDtoResponse newsDtoResponse = newsService.create(createRequest);
        setLinks(newsDtoResponse);
        return new ResponseEntity<>(newsDtoResponse, CREATED);
    }

    @Override
    @Operation(summary = "Update news information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated news information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PutMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<NewsDtoResponse> update(@PathVariable Long id, @RequestBody NewsDtoRequest updateRequest) {
        NewsDtoResponse newsDtoResponse = newsService.update(id, updateRequest);
        setLinks(newsDtoResponse);
        return new ResponseEntity<>(newsDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Patch news information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully patched news information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PatchMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<NewsDtoResponse> patch(@PathVariable Long id, @RequestBody NewsDtoRequest updateRequest) {
        NewsDtoResponse newsDtoResponse = newsService.patch(id, updateRequest);
        setLinks(newsDtoResponse);
        return new ResponseEntity<>(newsDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Deletes specific news with the supplied id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deletes the specific news"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteById(@PathVariable Long id) {
        newsService.deleteById(id);
    }

    @Operation(summary = "Get news by params")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved news by params"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/search")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<NewsDtoResponse>> readByParams(@RequestBody ParametersDtoRequest parametersDtoRequest, Pageable pageable) {
        Page<NewsDtoResponse> newsDtoResponseList = newsService.readByParams(parametersDtoRequest, pageable);
        for (NewsDtoResponse newsDtoResponse : newsDtoResponseList) {
            setLinks(newsDtoResponse);
        }
        return new ResponseEntity<>(newsDtoResponseList, OK);
    }

    @Operation(summary = "Get author by news id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved author by news id"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/{id}/authors")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthorDtoResponse> readAuthorByNewsId(@PathVariable Long id) {
        AuthorDtoResponse authorDtoResponse = authorService.readByNewsId(id);
        Link selfRel = linkTo(AuthorController.class).slash(authorDtoResponse.getId()).withSelfRel();
        authorDtoResponse.add(selfRel);
        return new ResponseEntity<>(authorDtoResponse, OK);
    }

    @Operation(summary = "Get tags by news id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tags by news id"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/{id}/tags")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<TagDtoResponse>> readTagsByNewsId(@PathVariable Long id, Pageable pageable) {
        Page<TagDtoResponse> tagDtoResponseList = tagService.readByNewsId(id, pageable);
        for (TagDtoResponse tagDtoResponse : tagDtoResponseList) {
            Link selfRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
            tagDtoResponse.add(selfRel);
        }
        return new ResponseEntity<>(tagDtoResponseList, OK);
    }

    @Operation(summary = "Get comments by news id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments by news id"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/{id}/comments")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<CommentDtoResponse>> readCommentsByNewsId(@PathVariable Long id, Pageable pageable) {
        Page<CommentDtoResponse> commentDtoResponseList = commentService.readByNewsId(id, pageable);
        for (CommentDtoResponse commentDtoResponse : commentDtoResponseList) {
            Link selfRel = linkTo(CommentController.class).slash(commentDtoResponse.getId()).withSelfRel();
            commentDtoResponse.add(selfRel);
        }
        return new ResponseEntity<>(commentDtoResponseList, OK);
    }
}
