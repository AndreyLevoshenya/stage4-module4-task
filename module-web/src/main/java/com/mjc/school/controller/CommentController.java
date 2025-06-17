package com.mjc.school.controller;

import com.mjc.school.dto.CommentDtoRequest;
import com.mjc.school.dto.CommentDtoResponse;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.dto.TagDtoResponse;
import com.mjc.school.service.CommentService;
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

import static com.mjc.school.controller.RestConstants.COMMENTS_V1_API_PATH;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = COMMENTS_V1_API_PATH)
public class CommentController implements BaseController<CommentDtoRequest, CommentDtoResponse, Long> {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    private static void setLinks(CommentDtoResponse commentDtoResponse) {
        Link selfRel = linkTo(CommentController.class).slash(commentDtoResponse.getId()).withSelfRel();
        commentDtoResponse.add(selfRel);
        Link newsRel = linkTo(NewsController.class).slash(commentDtoResponse.getNewsDtoResponse().getId()).withSelfRel();
        commentDtoResponse.getNewsDtoResponse().add(newsRel);
        Link authorRel = linkTo(AuthorController.class).slash(commentDtoResponse.getNewsDtoResponse().getAuthorDtoResponse().getId()).withSelfRel();
        commentDtoResponse.getNewsDtoResponse().getAuthorDtoResponse().add(authorRel);
        for (TagDtoResponse tagDtoResponse : commentDtoResponse.getNewsDtoResponse().getTagDtoResponseList()) {
            Link tagRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
            tagDtoResponse.add(tagRel);
        }
    }

    @Override
    @Operation(summary = "View all comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all comments"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<CommentDtoResponse>> readAll(
            @RequestParam(name = "searchBy", required = false) String searchBy,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @PageableDefault(sort = "content", direction = Sort.Direction.DESC) Pageable pageable) {
        SearchingRequest searchingRequest = null;
        if (searchBy != null && !searchBy.isBlank() && searchValue != null && !searchValue.isBlank()) {
            searchingRequest = new SearchingRequest(searchBy + ":" + searchValue);
        }
        Page<CommentDtoResponse> pageDtoResponse = commentService.readAll(searchingRequest, pageable);
        for (CommentDtoResponse commentDtoResponse : pageDtoResponse.stream().toList()) {
            setLinks(commentDtoResponse);
        }

        return new ResponseEntity<>(pageDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Get comment by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comment by id"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<CommentDtoResponse> readById(@PathVariable Long id) {
        CommentDtoResponse commentDtoResponse = commentService.readById(id);
        setLinks(commentDtoResponse);
        return new ResponseEntity<>(commentDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Create comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a comment"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<CommentDtoResponse> create(@RequestBody CommentDtoRequest createRequest) {
        CommentDtoResponse commentDtoResponse = commentService.create(createRequest);
        setLinks(commentDtoResponse);
        return new ResponseEntity<>(commentDtoResponse, CREATED);
    }

    @Override
    @Operation(summary = "Update comment information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated comment information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PutMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CommentDtoResponse> update(@PathVariable Long id, @RequestBody CommentDtoRequest updateRequest) {
        CommentDtoResponse commentDtoResponse = commentService.update(id, updateRequest);
        setLinks(commentDtoResponse);
        return new ResponseEntity<>(commentDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Patch comment information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully patched comment information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PatchMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CommentDtoResponse> patch(@PathVariable Long id, @RequestBody CommentDtoRequest updateRequest) {
        CommentDtoResponse commentDtoResponse = commentService.patch(id, updateRequest);
        setLinks(commentDtoResponse);
        return new ResponseEntity<>(commentDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Deletes specific comment with the supplied id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deletes the specific comment"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteById(@PathVariable Long id) {
        commentService.deleteById(id);
    }
}
