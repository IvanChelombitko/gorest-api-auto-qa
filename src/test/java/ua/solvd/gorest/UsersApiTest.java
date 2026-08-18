package ua.solvd.gorest;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import ua.solvd.gorest.util.PayloadTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UsersApiTest extends BaseTest {

    @Test(description = "TC-001")
    public void testGetUserList() {
        given()
                .spec(authRequestSpec)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .header("x-pagination-page", notNullValue())
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue());
    }

    @Test(description = "TC-002")
    public void testCreateUser() {
        var payload = PayloadTemplate.getValidUser();
        var jsonBody = mapper.writeValueAsString(payload);
        given()
                .spec(authRequestSpec)
                .body(jsonBody)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .header("Content-Type", containsString("application/json"))
                .body("id", notNullValue())
                .body("name", equalTo(payload.name()))
                .body("email", equalTo(payload.email()))
                .body("status", equalTo(payload.status()));
    }

    @Test(description = "TC-003")
    public void testGetUserById() {
        int userId = createTemporaryUserAndGetId();
        given()
                .spec(authRequestSpec)
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("name", notNullValue());
    }

    @Test(description = "TC-004")
    public void testUpdateUserPut() {
        int userId = createTemporaryUserAndGetId();
        var updatedPayload = PayloadTemplate.getValidUser().withUpdatedData("Updated Put Name", "inactive");
        var jsonBody = mapper.writeValueAsString(updatedPayload);
        given()
                .spec(authRequestSpec)
                .body(jsonBody)
                .when()
                .put("/users/" + userId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Put Name"))
                .body("status", equalTo("inactive"));
    }

    @Test(description = "TC-005")
    public void testUpdateUserPatch() {
        int userId = createTemporaryUserAndGetId();
        Map<String, String> patchData = new HashMap<>();
        patchData.put("name", "Patched Name");
        var jsonBody = mapper.writeValueAsString(patchData);
        given()
                .spec(authRequestSpec)
                .body(jsonBody)
                .when()
                .patch("/users/" + userId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Patched Name"));
    }

    @Test(description = "TC-006")
    public void testDeleteUser() {
        int userId = createTemporaryUserAndGetId();
        given()
                .spec(authRequestSpec)
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(204)
                .header("Content-Length", anyOf(nullValue(), equalTo("0"), equalTo(""))); // sometimes absent or 0
        given()
                .spec(authRequestSpec)
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(404)
                .body("message", equalTo("Resource not found"));
    }

    @Test(description = "TC-007")
    public void testFilterUsersByName() {
        String uniqueSearchName = "FilterTest " + UUID.randomUUID().toString().substring(0, 5);
        var payload = PayloadTemplate.getValidUserWithSpecificName(uniqueSearchName);
        var jsonBody = mapper.writeValueAsString(payload);
        given()
                .spec(authRequestSpec)
                .body(jsonBody)
                .post("/users")
                .then().statusCode(201);
        given()
                .spec(authRequestSpec)
                .queryParam("name", uniqueSearchName)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].name", equalTo(uniqueSearchName));
    }

    @Test(description = "TC-008")
    public void testCreateUserWithInvalidEmail() {
        var payload = PayloadTemplate.getUserWithInvalidEmail();
        var jsonBody = mapper.writeValueAsString(payload);
        given()
                .spec(authRequestSpec)
                .body(jsonBody)
                .when()
                .post("/users")
                .then()
                .statusCode(422) // Unprocessable Entity
                .body("[0].field", equalTo("email"))
                .body("[0].message", equalTo("is invalid"));
    }

    @Test(description = "TC-009")
    public void testGetNonExistentUser() {
        given()
                .spec(authRequestSpec)
                .when()
                .get("/users/999999999")
                .then()
                .statusCode(404)
                .body("message", equalTo("Resource not found"));
    }

    @Test(description = "TC-010")
    public void testUnauthorizedRequest() {
        var payload = PayloadTemplate.getValidUser();
        var jsonBody = mapper.writeValueAsString(payload);
        given()
                .spec(unAuthRequestSpec)
                .body(jsonBody)
                .when()
                .post("/users")
                .then()
                .statusCode(401)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Authentication failed"));
    }

    private int createTemporaryUserAndGetId() {
        var payload = PayloadTemplate.getValidUser();
        var jsonBody = mapper.writeValueAsString(payload);
        Response response = given()
                .spec(authRequestSpec)
                .body(jsonBody)
                .post("/users");
        response.then().statusCode(201);
        return response.jsonPath().getInt("id");
    }
}