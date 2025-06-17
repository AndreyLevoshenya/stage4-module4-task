package com.mjc.school.impl;

import com.mjc.school.dto.AuthorDtoRequest;
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
class AuthorControllerIntegrationTest {
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
    void givenNoParams_whenGetAllAuthors_thenReturn200AndAuthorsPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/authors")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].name", notNullValue())
                .body("totalElements", greaterThan(0));
    }

    @Test
    void givenSearchParams_whenGetAuthors_thenReturnFilteredResult() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("searchBy", "name")
                .queryParam("searchValue", "Author2")
                .when()
                .get("/api/v1/authors")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].name", equalTo("Author2"));
    }

    @Test
    void givenPaginationParams_whenGetAuthors_thenReturnCorrectPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/api/v1/authors")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(2))
                .body("totalPages", greaterThanOrEqualTo(1));
    }

    @Test
    void givenExistingAuthorId_whenGetAuthorById_thenReturnAuthor() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/authors/3")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(3))
                .body("name", not(emptyOrNullString()))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/authors/3"));
    }

    @Test
    void givenNonExistingAuthorId_whenGetAuthorById_thenReturn404() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/authors/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void givenValidRequestAndAdminAuth_whenCreateAuthor_thenReturn201AndAuthor() {
        String token = obtainJwtToken("admin", "admin");
        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("New Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/authors")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("New Author"))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/authors/"));
    }

    @Test
    void givenInvalidRequestAndAdminAuth_whenCreateAuthor_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");
        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("New Author too long name");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/authors")
                .then()
                .statusCode(400);
    }

    @Test
    void givenUserRoleAuth_whenCreateAuthor_thenReturn403() {
        String token = obtainJwtToken("test", "test");
        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("New Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/authors")
                .then()
                .statusCode(403);
    }

    @Test
    void givenNoAuth_whenCreateAuthor_thenReturn401() {
        given()
                .contentType(ContentType.JSON)
                .headers(Map.of())
                .body(new AuthorDtoRequest("NoAuth Author"))
                .when()
                .post("/api/v1/authors")
                .then()
                .statusCode(401);
    }

    @Test
    void givenValidUpdateRequestAndAdminAuth_whenUpdateExistingAuthor_thenReturnUpdatedAuthor() {
        String token = obtainJwtToken("admin", "admin");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("new name");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/authors/{id}", 3)
                .then()
                .statusCode(200)
                .body("id", equalTo(3))
                .body("name", equalTo("new name"))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/authors/3"));
    }

    @Test
    void givenNoAuth_whenUpdateAuthor_thenReturn401() {
        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("Updated Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/v1/authors/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    void givenUserRoleAuth_whenUpdateAuthor_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("Updated Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/authors/{id}", 1)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNonExistingAuthorIdAndAdminAuth_whenUpdateAuthor_thenReturn404() {
        String token = obtainJwtToken("admin", "admin");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("Updated Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/authors/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidUpdateRequestAndAdminAuth_whenUpdateAuthor_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/authors/{id}", 1)
                .then()
                .statusCode(400);
    }

    @Test
    void givenValidPatchRequestAndAdminAuth_whenPatchExistingAuthor_thenReturnPatchedAuthor() {
        String token = obtainJwtToken("admin", "admin");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("Patched name");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/authors/{id}", 4)
                .then()
                .statusCode(200)
                .body("id", equalTo(4))
                .body("name", equalTo("Patched name"))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/authors/4"));
    }

    @Test
    void givenNoAuth_whenPatchAuthor_thenReturn401() {
        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("Patched Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v1/authors/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    void givenUserRoleAuth_whenPatchAuthor_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("Patched Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/authors/{id}", 1)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNonExistingAuthorIdAndAdminAuth_whenPatchAuthor_thenReturn404() {
        String token = obtainJwtToken("admin", "admin");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("Patched Author");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/authors/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidPatchRequestAndAdminAuth_whenPatchAuthor_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");

        AuthorDtoRequest request = new AuthorDtoRequest();
        request.setName("");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/authors/{id}", 1)
                .then()
                .statusCode(400);
    }

    @Test
    void givenAdminAuthAndExistingAuthorId_whenDeleteAuthor_thenReturn204() {
        Long authorId = 6L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/authors/{id}", authorId)
                .then()
                .statusCode(204);
    }

    @Test
    void givenAdminAuthAndNonExistingAuthorId_whenDeleteAuthor_thenReturn404() {
        Long nonexistentId = 999L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/authors/{id}", nonexistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenUserRoleAuth_whenDeleteAuthor_thenReturn403() {
        Long authorId = 4L;
        String token = obtainJwtToken("test", "test");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/authors/{id}", authorId)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNoAuth_whenDeleteAuthor_thenReturn401() {
        Long authorId = 3L;

        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/authors/{id}", authorId)
                .then()
                .statusCode(401);
    }
}