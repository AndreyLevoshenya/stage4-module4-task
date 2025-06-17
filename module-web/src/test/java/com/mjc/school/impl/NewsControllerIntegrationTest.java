package com.mjc.school.impl;

import com.mjc.school.dto.NewsDtoRequest;
import com.mjc.school.dto.ParametersDtoRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NewsControllerIntegrationTest {
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
    void givenNoParams_whenGetAllNews_thenReturn200AndNewsPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/news")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].title", notNullValue())
                .body("content[0].content", notNullValue())
                .body("content[0].authorDtoResponse", notNullValue())
                .body("totalElements", greaterThan(0));
    }

    @Test
    void givenSearchParams_whenGetNews_thenReturnFilteredResult() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("searchBy", "content")
                .queryParam("searchValue", "content2")
                .when()
                .get("/api/v1/news")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].content", equalTo("content2"));
    }

    @Test
    void givenPaginationParams_whenGetNews_thenReturnCorrectPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/api/v1/news")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(2))
                .body("totalPages", greaterThanOrEqualTo(1));
    }

    @Test
    void givenExistingId_whenGetNewsById_thenReturnNews() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/news/3")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(3))
                .body("title", not(emptyOrNullString()))
                .body("content", not(emptyOrNullString()))
                .body("authorDtoResponse", notNullValue())
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/news/3"));
    }

    @Test
    void givenNonexistentId_whenGetNewsById_thenReturn404() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/news/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void givenValidRequestAndAuthorized_whenCreateNews_thenReturn201AndCreatedNews() {
        String token = obtainJwtToken("test", "test");
        NewsDtoRequest request = new NewsDtoRequest();
        request.setTitle("New title");
        request.setContent("New Content");
        request.setAuthorId(1L);
        request.setTagIds(List.of(1L, 2L));

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/news")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("New title"))
                .body("content", equalTo("New Content"))
                .body("authorDtoResponse.id", equalTo(1))
                .body("tagDtoResponseList.size()", equalTo(2))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/news/"));
    }

    @Test
    void givenInvalidRequestAndAuthorized_whenCreateNews_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");
        NewsDtoRequest request = new NewsDtoRequest();
        request.setTitle("");
        request.setContent("");
        request.setAuthorId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/news")
                .then()
                .statusCode(400);
    }

    @Test
    void givenUnauthorizedUser_whenCreateNews_thenReturn401() {
        given()
                .contentType(ContentType.JSON)
                .headers(Map.of())
                .body(new NewsDtoRequest("news title", "content of a news", 1L, List.of()))
                .when()
                .post("/api/v1/news")
                .then()
                .statusCode(401);
    }

    @Test
    void givenValidUpdateAndAdminAndNewsExists_whenUpdateNews_thenReturnUpdatedNews() {
        String token = obtainJwtToken("admin", "admin");

        NewsDtoRequest request = new NewsDtoRequest();
        request.setTitle("Updated title");
        request.setContent("Updated content");
        request.setAuthorId(2L);
        request.setTagIds(List.of(1L, 2L));

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/news/{id}", 3)
                .then()
                .statusCode(200)
                .body("id", equalTo(3))
                .body("title", equalTo("Updated title"))
                .body("content", equalTo("Updated content"))
                .body("authorDtoResponse.id", equalTo(2))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/news/3"));
    }

    @Test
    void givenUnauthorizedUser_whenUpdateNews_thenReturn401() {
        NewsDtoRequest request = new NewsDtoRequest();
        request.setContent("Updated Content");
        request.setTitle("Updated title");
        request.setContent("Updated Content");
        request.setAuthorId(2L);
        request.setTagIds(List.of(1L, 2L));

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/v1/news/{id}", 3)
                .then()
                .statusCode(401);
    }

    @Test
    void givenUserHasNoAdminRole_whenUpdateNews_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        NewsDtoRequest request = new NewsDtoRequest();
        request.setTitle("Updated title");
        request.setContent("Updated Content");
        request.setAuthorId(2L);
        request.setTagIds(List.of(1L, 2L));

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/news/{id}", 3)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNewsNotFound_whenUpdateNews_thenReturn404() {
        String token = obtainJwtToken("admin", "admin");

        NewsDtoRequest request = new NewsDtoRequest();
        request.setTitle("Updated title");
        request.setContent("Updated Content");
        request.setAuthorId(2L);
        request.setTagIds(List.of(1L, 2L));

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/news/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidRequest_whenUpdateNews_thenReturn400() {
        String token = obtainJwtToken("admin", "admin");

        NewsDtoRequest request = new NewsDtoRequest();
        request.setTitle("");
        request.setContent("Updated Content");
        request.setAuthorId(2L);
        request.setTagIds(List.of(1L, 2L));

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/news/{id}", 3)
                .then()
                .statusCode(400);
    }

    @Test
    void givenAdminAndNewsExists_whenPatchNews_thenReturnPatchedNews() {
        String token = obtainJwtToken("admin", "admin");

        NewsDtoRequest request = new NewsDtoRequest();
        request.setContent("Patched content");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/news/{id}", 4)
                .then()
                .statusCode(200)
                .body("id", equalTo(4))
                .body("title", equalTo("title4"))
                .body("content", equalTo("Patched content"))
                .body("authorDtoResponse.id", equalTo(4))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/news/4"));
    }

    @Test
    void givenUnauthorizedUser_whenPatchNews_thenReturn401() {
        NewsDtoRequest request = new NewsDtoRequest();
        request.setContent("Patched content");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v1/news/{id}", 4)
                .then()
                .statusCode(401);
    }

    @Test
    void givenUserHasNoAdminRole_whenPatchNews_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        NewsDtoRequest request = new NewsDtoRequest();
        request.setContent("Patched content");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/news/{id}", 4)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNewsNotFound_whenPatchNews_thenReturn404() {
        String token = obtainJwtToken("admin", "admin");

        NewsDtoRequest request = new NewsDtoRequest();
        request.setContent("Patched content");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/news/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenAdminAuthorizedAndNewsExists_whenDeleteNews_thenReturn204() {
        Long newsId = 5L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/news/{id}", newsId)
                .then()
                .statusCode(204);
    }

    @Test
    void givenAdminAuthorizedAndNewsNotFound_whenDeleteNews_thenReturn404() {
        Long nonexistentId = 999L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/news/{id}", nonexistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenUserRole_whenDeleteNews_thenReturn403() {
        Long newsId = 4L;
        String token = obtainJwtToken("test", "test");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/news/{id}", newsId)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNoTokenProvided_whenDeleteNews_thenReturn401() {
        Long newsId = 3L;

        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/news/{id}", newsId)
                .then()
                .statusCode(401);
    }

    @Test
    void givenTitle_whenSearchNews_thenReturnNewsByTitle() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "title",
                "",
                "",
                null,
                null
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get("/api/v1/news/search")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content.title", everyItem(containsString("title")));
    }

    @Test
    void givenAuthorName_whenSearchNews_thenReturnNewsByAuthorName() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "",
                "",
                "Author2",
                null,
                null
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get("/api/v1/news/search")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content.authorDtoResponse.name", everyItem(equalTo("Author2")));
    }

    @Test
    void givenTagIds_whenSearchNews_thenReturnNewsByTagIds() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "",
                "",
                "",
                List.of(1),
                null
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get("/api/v1/news/search")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content.tagDtoResponseList.id.flatten()", hasItems(1));
    }

    @Test
    void givenTagNames_whenSearchNews_thenReturnNewsByTagNames() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "",
                "",
                "",
                null,
                List.of("name2")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get("/api/v1/news/search")
                .then()
                .statusCode(200)
                .body("content.tagDtoResponseList.name.flatten()", hasItems("name2"));
    }

    @Test
    void givenNoMatchingParameters_whenSearchNews_thenReturnEmptyList() {
        ParametersDtoRequest request = new ParametersDtoRequest(
                "nonexistent",
                "nonexistent",
                "no one",
                List.of(9999),
                List.of("Unknown")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get("/api/v1/news/search")
                .then()
                .statusCode(200)
                .body("content", empty());
    }

    @Test
    void givenPaginationParameters_whenSearchNews_thenReturnPaginatedResults() {
        ParametersDtoRequest request = new ParametersDtoRequest("", "", "", null, null);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .queryParam("page", 0)
                .queryParam("size", 3)
                .when()
                .get("/api/v1/news/search")
                .then()
                .statusCode(200)
                .body("content.size()", lessThanOrEqualTo(3))
                .body("totalPages", greaterThanOrEqualTo(1));
    }

    @Test
    void givenExistingNewsId_whenReadAuthor_thenReturnAuthor() {
        Long newsId = 1L;

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/news/{id}/authors", newsId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo("Author1"))
                .body("_links.self.href", containsString("/api/v1/authors/"));
    }

    @Test
    void givenNonExistentNewsId_whenReadAuthor_thenReturn404() {
        Long nonExistentId = 999L;

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/news/{id}/authors", nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenExistingNewsId_whenReadTags_thenReturnTags() {
        Long newsId = 1L;

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/news/{id}/tags", newsId)
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].id", notNullValue())
                .body("content[0].name", equalTo("name1"));
    }

    @Test
    void givenNonExistentNewsId_whenReadTags_thenReturn404() {
        Long nonExistentNewsId = 999L;

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/news/{id}/tags", nonExistentNewsId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenExistingNewsId_whenReadComments_thenReturnComments() {
        Long newsId = 1L;

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/news/{id}/comments", newsId)
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].id", notNullValue())
                .body("content[0].content", equalTo("content1"));
    }

    @Test
    void givenNonExistentNewsId_whenReadComments_thenReturn404() {
        Long nonExistentNewsId = 9999L;

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/news/{id}/comments", nonExistentNewsId)
                .then()
                .statusCode(404);
    }
}