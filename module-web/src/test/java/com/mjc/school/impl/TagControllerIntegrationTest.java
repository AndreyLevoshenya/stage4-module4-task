package com.mjc.school.impl;

import com.mjc.school.dto.TagDtoRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TagControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    private String obtainJwtToken(String username, String password) {
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", username);
        loginPayload.put("password", password);
        return given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/v1/auth/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    void givenNoParams_whenGetAllTags_thenReturn200AndTagsPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/tags")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].name", notNullValue())
                .body("totalElements", greaterThan(0));
    }

    @Test
    void givenSearchParams_whenGetTags_thenReturnFilteredResult() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("searchBy", "name")
                .queryParam("searchValue", "name2")
                .when()
                .get("/api/v1/tags")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].name", equalTo("name2"));
    }

    @Test
    void givenPaginationParams_whenGetTags_thenReturnCorrectPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/api/v1/tags")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(2))
                .body("totalPages", greaterThanOrEqualTo(1));
    }

    @Test
    void givenExistingId_whenGetTagById_thenReturnTag() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/tags/3")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(3))
                .body("name", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/tags/3"));
    }

    @Test
    void givenNonExistentId_whenGetTagById_thenReturn404() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/tags/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void givenValidRequestAndAdminAuth_whenCreateTag_thenReturn201AndTag() {
        String token = obtainJwtToken("admin", "admin");
        TagDtoRequest request = new TagDtoRequest();
        request.setName("New Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/tags")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("New Tag"))
                .body("_links.self.href", containsString("/api/v1/tags/"));
    }

    @Test
    void givenInvalidRequestAndAdminAuth_whenCreateTag_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");
        TagDtoRequest request = new TagDtoRequest();
        request.setName("New Tag too long name");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/tags")
                .then()
                .statusCode(400);
    }

    @Test
    void givenValidRequestAndUserRole_whenCreateTag_thenReturn403() {
        String token = obtainJwtToken("test", "test");
        TagDtoRequest request = new TagDtoRequest();
        request.setName("New Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/tags")
                .then()
                .statusCode(403);
    }

    @Test
    void givenValidRequestAndNoAuth_whenCreateTag_thenReturn401() {
        given()
                .contentType(ContentType.JSON)
                .headers(Map.of())
                .body(new TagDtoRequest("NoAuth Tag"))
                .when()
                .post("/api/v1/tags")
                .then()
                .statusCode(401);
    }

    @Test
    void givenValidRequestAndAdminAuth_whenUpdateExistingTag_thenReturnUpdatedTag() {
        String token = obtainJwtToken("admin", "admin");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("new name");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("new name"))
                .body("_links.self.href", containsString("/api/v1/tags/1"));
    }

    @Test
    void givenUpdateRequestAndNoAuth_whenUpdateTag_thenReturn401() {
        TagDtoRequest request = new TagDtoRequest();
        request.setName("Updated Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    void givenUpdateRequestAndUserRole_whenUpdateTag_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("Updated Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(403);
    }

    @Test
    void givenUpdateRequestAndAdminAuth_whenUpdateNonExistentTag_thenReturn404() {
        String token = obtainJwtToken("admin", "admin");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("Updated Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/tags/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidUpdateRequestAndAdminAuth_whenUpdateTag_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(400);
    }

    @Test
    void givenValidPatchRequestAndAdminAuth_whenPatchExistingTag_thenReturnUpdatedTag() {
        String token = obtainJwtToken("admin", "admin");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("new name");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("new name"))
                .body("_links.self.href", containsString("/api/v1/tags/1"));
    }

    @Test
    void givenPatchRequestAndNoAuth_whenPatchTag_thenReturn401() {
        TagDtoRequest request = new TagDtoRequest();
        request.setName("Patched Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    void givenPatchRequestAndUserRole_whenPatchTag_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("Patched Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(403);
    }

    @Test
    void givenPatchRequestAndAdminAuth_whenPatchNonExistentTag_thenReturn404() {
        String token = obtainJwtToken("admin", "admin");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("Patched Tag");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/tags/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidPatchRequestAndAdminAuth_whenPatchTag_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");

        TagDtoRequest request = new TagDtoRequest();
        request.setName("");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/tags/{id}", 1)
                .then()
                .statusCode(400);
    }

    @Test
    void givenAdminAuth_whenDeleteExistingTag_thenReturn204() {
        Long tagId = 5L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/tags/{id}", tagId)
                .then()
                .statusCode(204);
    }

    @Test
    void givenAdminAuth_whenDeleteNonExistentTag_thenReturn404() {
        Long nonexistentId = 999L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/tags/{id}", nonexistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenUserRoleAuth_whenDeleteTag_thenReturn403() {
        Long tagId = 4L;
        String token = obtainJwtToken("test", "test");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/tags/{id}", tagId)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNoAuth_whenDeleteTag_thenReturn401() {
        Long tagId = 3L;

        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/tags/{id}", tagId)
                .then()
                .statusCode(401);
    }
}