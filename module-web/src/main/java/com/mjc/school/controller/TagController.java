package com.mjc.school.controller;

import com.mjc.school.dto.SearchingRequest;
import com.mjc.school.dto.TagDtoRequest;
import com.mjc.school.dto.TagDtoResponse;
import com.mjc.school.service.TagService;
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

import static com.mjc.school.controller.RestConstants.TAGS_V1_API_PATH;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = TAGS_V1_API_PATH)
public class TagController implements BaseController<TagDtoRequest, TagDtoResponse, Long> {
    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    @Operation(summary = "View all tags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all tags"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<TagDtoResponse>> readAll(
            @RequestParam(name = "searchBy", required = false) String searchBy,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
        SearchingRequest searchingRequest = null;
        if (searchBy != null && !searchBy.isBlank() && searchValue != null && !searchValue.isBlank()) {
            searchingRequest = new SearchingRequest(searchBy + ":" + searchValue);
        }
        Page<TagDtoResponse> pageDtoResponse = tagService.readAll(searchingRequest, pageable);
        for (TagDtoResponse tagDtoResponse : pageDtoResponse.stream().toList()) {
            Link selfRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
            tagDtoResponse.add(selfRel);
        }

        return new ResponseEntity<>(pageDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Get tag by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tag by id"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @GetMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("permitAll()")
    public ResponseEntity<TagDtoResponse> readById(@PathVariable Long id) {
        TagDtoResponse tagDtoResponse = tagService.readById(id);
        Link selfRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
        tagDtoResponse.add(selfRel);
        return new ResponseEntity<>(tagDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Create tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a tag"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PostMapping
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TagDtoResponse> create(@RequestBody TagDtoRequest createRequest) {
        TagDtoResponse tagDtoResponse = tagService.create(createRequest);
        Link selfRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
        tagDtoResponse.add(selfRel);
        return new ResponseEntity<>(tagDtoResponse, CREATED);
    }

    @Override
    @Operation(summary = "Update tag information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated tag information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PutMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TagDtoResponse> update(@PathVariable Long id, @RequestBody TagDtoRequest updateRequest) {
        TagDtoResponse tagDtoResponse = tagService.update(id, updateRequest);
        Link selfRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
        tagDtoResponse.add(selfRel);
        return new ResponseEntity<>(tagDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Patch tag information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully patched tag information"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @PatchMapping(value = "/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TagDtoResponse> patch(@PathVariable Long id, @RequestBody TagDtoRequest updateRequest) {
        TagDtoResponse tagDtoResponse = tagService.patch(id, updateRequest);
        Link selfRel = linkTo(TagController.class).slash(tagDtoResponse.getId()).withSelfRel();
        tagDtoResponse.add(selfRel);
        return new ResponseEntity<>(tagDtoResponse, OK);
    }

    @Override
    @Operation(summary = "Deletes specific tag with the supplied id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deletes the specific tag"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")})
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteById(@PathVariable Long id) {
        tagService.deleteById(id);
    }
}
