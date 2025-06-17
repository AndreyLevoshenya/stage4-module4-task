package com.mjc.school.impl;

import com.mjc.school.dto.CommentDtoRequest;
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
class CommentControllerIntegrationTest {
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
    void givenNoParams_whenGetAllComments_thenReturns200AndPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/comments")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].content", notNullValue())
                .body("content[0].newsDtoResponse", notNullValue())
                .body("totalElements", greaterThan(0));
    }

    @Test
    void givenSearchParams_whenGetComments_thenReturnsFilteredResults() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("searchBy", "content")
                .queryParam("searchValue", "content2")
                .when()
                .get("/api/v1/comments")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].content", equalTo("content2"));
    }

    @Test
    void givenPagination_whenGetComments_thenReturnsCorrectPage() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/api/v1/comments")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(2))
                .body("totalPages", greaterThanOrEqualTo(1));
    }

    @Test
    void givenExistingId_whenGetCommentById_thenReturnsComment() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/comments/3")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(3))
                .body("content", not(emptyOrNullString()))
                .body("newsDtoResponse.id", not(emptyOrNullString()))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/comments/3"));
    }

    @Test
    void givenNonExistentId_whenGetCommentById_thenReturns404() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/comments/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void givenValidRequestAndAuth_whenCreateComment_thenReturns201AndComment() {
        String token = obtainJwtToken("test", "test");
        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("New Content");
        request.setNewsId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/comments")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("content", equalTo("New Content"))
                .body("newsDtoResponse.id", equalTo(1))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/comments/"));
    }

    @Test
    void givenInvalidRequest_whenCreateComment_thenReturns400() {
        String token = obtainJwtToken("admin", "admin");
        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/api/v1/comments")
                .then()
                .statusCode(400);
    }

    @Test
    void givenNoAuth_whenCreateComment_thenReturn401() {
        given()
                .contentType(ContentType.JSON)
                .headers(Map.of())
                .body(new CommentDtoRequest("NoAuth Comment", 1L))
                .when()
                .post("/api/v1/comments")
                .then()
                .statusCode(401);
    }

    @Test
    void givenAdminAuthAndExistingComment_whenUpdateComment_thenReturnsUpdatedComment() {
        String token = obtainJwtToken("admin", "admin");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("new content");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/comments/{id}", 3)
                .then()
                .statusCode(200)
                .body("id", equalTo(3))
                .body("content", equalTo("new content"))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/comments/3"));
    }

    @Test
    void givenNoAuth_whenUpdateComment_thenReturn401() {
        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("Updated Content");
        request.setNewsId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/v1/comments/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    void givenUserRole_whenUpdateComment_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("Updated Content");
        request.setNewsId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/comments/{id}", 1)
                .then()
                .statusCode(403);
    }

    @Test
    void givenAdminAuthAndNonExistentComment_whenUpdateComment_thenReturns404() {
        String token = obtainJwtToken("admin", "admin");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("Updated Content");
        request.setNewsId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/comments/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidRequest_whenUpdateComment_thenReturns400() {
        String token = obtainJwtToken("admin", "admin");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .put("/api/v1/comments/{id}", 1)
                .then()
                .statusCode(400);
    }

    @Test
    void givenAdminAuthAndExistingComment_whenPatchComment_thenReturnsUpdatedComment() {
        String token = obtainJwtToken("admin", "admin");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("new content");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/comments/{id}", 4)
                .then()
                .statusCode(200)
                .body("id", equalTo(4))
                .body("content", equalTo("new content"))
                .body("createDate", not(emptyOrNullString()))
                .body("lastUpdateDate", not(emptyOrNullString()))
                .body("_links.self.href", containsString("/api/v1/comments/4"));
    }

    @Test
    void givenNoAuth_whenPatchComment_thenReturn401() {
        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("Patched Content");

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v1/comments/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    void givenUserRole_whenPatchComment_thenReturn403() {
        String token = obtainJwtToken("test", "test");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("Patched Content");
        request.setNewsId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/comments/{id}", 1)
                .then()
                .statusCode(403);
    }

    @Test
    void givenAdminAuthAndNonExistentComment_whenPatchComment_thenReturns404() {
        String token = obtainJwtToken("admin", "admin");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("Patched Content");
        request.setNewsId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/comments/{id}", 9999)
                .then()
                .statusCode(404);
    }

    @Test
    void givenInvalidRequest_whenPatchComment_thenReturns400() {
        String token = obtainJwtToken("admin", "admin");

        CommentDtoRequest request = new CommentDtoRequest();
        request.setContent("");
        request.setNewsId(1L);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .patch("/api/v1/comments/{id}", 1)
                .then()
                .statusCode(400);
    }

    @Test
    void givenAdminAuthAndExistingComment_whenDeleteComment_thenReturns204() {
        Long commentId = 6L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/comments/{id}", commentId)
                .then()
                .statusCode(204);
    }

    @Test
    void givenAdminAuthAndNonExistentComment_whenDeleteComment_thenReturns404() {
        Long nonexistentId = 999L;
        String token = obtainJwtToken("admin", "admin");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/comments/{id}", nonexistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenUserRole_whenDeleteComment_thenReturn403() {
        Long commentId = 4L;
        String token = obtainJwtToken("test", "test");

        given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/comments/{id}", commentId)
                .then()
                .statusCode(403);
    }

    @Test
    void givenNoAuth_whenDeleteComment_thenReturn401() {
        Long commentId = 3L;

        given()
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/comments/{id}", commentId)
                .then()
                .statusCode(401);
    }
}