package com.mjc.school.controller;

import com.mjc.school.dto.AuthorDtoRequest;
import com.mjc.school.dto.AuthorDtoResponse;
import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.service.AuthorService;
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

import static com.mjc.school.controller.RestConstants.AUTHORS_V1_API_PATH;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = AUTHORS_V1_API_PATH)
public class AuthorController implements BaseController<AuthorDtoRequest, AuthorDtoResponse, Long> {
    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Override
    @Operation(summary = "View all authors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all authors"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<AuthorDtoResponse>> readAll(
            @RequestParam(name = "searchBy", required = false) String searchBy,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
        SearchingRequest searchingRequest = null;
        if (searchBy != null && !searchBy.isBlank() && searchValue != null && !searchValue.isBlank()) {
            searchingRequest = new SearchingRequest(searchBy + ":" + searchValue);
        }
        Page<AuthorDtoResponse> pageDtoResponse = authorService.readAll(searchingRequest, pageable);
        for (AuthorDtoResponse authorDtoResponse : pageDtoResponse.stream().toList()) {
            Link selfRel = linkTo(AuthorController.class).slash(authorDtoResponse.getId()).withSelfRel();
            authorDtoResponse.add(selfRel);
        }

        return new ResponseEntity<>(pageDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Get author by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved authors by id"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthorDtoResponse> readById(@PathVariable Long id) {
        AuthorDtoResponse authorDtoResponse = authorService.readById(id);
        Link selfRel = linkTo(AuthorController.class).slash(id).withSelfRel();
        authorDtoResponse.add(selfRel);
        return new ResponseEntity<>(authorDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Create author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created an author"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<AuthorDtoResponse> create(@RequestBody AuthorDtoRequest createRequest) {
        AuthorDtoResponse authorDtoResponse = authorService.create(createRequest);
        Link selfRel = linkTo(AuthorController.class).slash(authorDtoResponse.getId()).withSelfRel();
        authorDtoResponse.add(selfRel);
        return new ResponseEntity<>(authorDtoResponse, CREATED);
    }

    @Override
    @Operation(summary = "Update author information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated author information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PutMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AuthorDtoResponse> update(@PathVariable Long id, @RequestBody AuthorDtoRequest updateRequest) {
        AuthorDtoResponse authorDtoResponse = authorService.update(id, updateRequest);
        Link selfRel = linkTo(AuthorController.class).slash(id).withSelfRel();
        authorDtoResponse.add(selfRel);
        return new ResponseEntity<>(authorDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Patch author information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully patched author information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PatchMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AuthorDtoResponse> patch(@PathVariable Long id, @RequestBody AuthorDtoRequest updateRequest) {
        AuthorDtoResponse authorDtoResponse = authorService.patch(id, updateRequest);
        Link selfRel = linkTo(AuthorController.class).slash(id).withSelfRel();
        authorDtoResponse.add(selfRel);
        return new ResponseEntity<>(authorDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Deletes specific author with the supplied id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deletes the specific author"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public void deleteById(@PathVariable Long id) {
        authorService.deleteById(id);
    }
}
