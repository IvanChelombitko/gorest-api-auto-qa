package ua.solvd.gorest;

import org.testng.Assert;
import org.testng.annotations.Test;
import ua.solvd.gorest.model.ApiResponse;
import ua.solvd.gorest.model.GenericError;
import ua.solvd.gorest.model.UserPayload;
import ua.solvd.gorest.model.ValidationError;
import ua.solvd.gorest.util.PayloadTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UsersApiTest extends BaseTest {

    @Test(description = "TC-001")
    public void testGetUserList() {
        ApiResponse<UserPayload[]> response = apiService.getUsersList();
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertTrue(response.getHeader("Content-Type").contains("application/json"), "Missing JSON Content-Type.");
        Assert.assertNotNull(response.getHeader("x-pagination-page"), "Pagination header is missing.");
        Assert.assertTrue(response.getBody().length > 0, "User list should not be empty.");
        Assert.assertNotNull(response.getBody()[0].id(), "User ID should not be null.");
    }

    @Test(description = "TC-002")
    public void testCreateUser() {
        UserPayload expectedUser = PayloadTemplate.getValidUser();
        ApiResponse<UserPayload> response = apiService.createUser(expectedUser);
        Assert.assertEquals(response.getStatusCode(), 201, "Status code should be 201.");
        Assert.assertTrue(response.getHeader("Content-Type").contains("application/json"), "Missing JSON Content-Type.");
        UserPayload actualUser = response.getBody();
        Assert.assertNotNull(actualUser.id(), "Created user ID should not be null.");
        Assert.assertEquals(actualUser.name(), expectedUser.name(), "Name does not match.");
        Assert.assertEquals(actualUser.email(), expectedUser.email(), "Email does not match.");
        Assert.assertEquals(actualUser.status(), expectedUser.status(), "Status does not match.");
    }

    @Test(description = "TC-003")
    public void testGetUserById() {
        int userId = getTemporaryUserId();
        ApiResponse<UserPayload> response = apiService.getUser(userId);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertEquals(response.getBody().id(), userId, "User ID does not match.");
        Assert.assertNotNull(response.getBody().name(), "User name should not be null.");
    }

    @Test(description = "TC-004")
    public void testUpdateUserPut() {
        int userId = getTemporaryUserId();
        UserPayload updatedPayload = PayloadTemplate.getValidUser().withUpdatedData("Updated Put Name", "inactive");
        ApiResponse<UserPayload> response = apiService.updateUserPut(userId, updatedPayload);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertTrue(response.getHeader("Content-Type").contains("application/json"), "Missing JSON Content-Type.");
        Assert.assertEquals(response.getBody().name(), "Updated Put Name", "Name was not updated.");
        Assert.assertEquals(response.getBody().status(), "inactive", "Status was not updated.");
    }

    @Test(description = "TC-005")
    public void testUpdateUserPatch() {
        int userId = getTemporaryUserId();
        Map<String, String> patchData = new HashMap<>();
        patchData.put("name", "Patched Name");
        ApiResponse<UserPayload> response = apiService.updateUserPatch(userId, patchData);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertTrue(response.getHeader("Content-Type").contains("application/json"), "Missing JSON Content-Type.");
        Assert.assertEquals(response.getBody().name(), "Patched Name", "Name was not updated.");
    }

    @Test(description = "TC-006")
    public void testDeleteUser() {
        int userId = getTemporaryUserId();
        ApiResponse<Void> deleteResponse = apiService.deleteUser(userId);
        Assert.assertEquals(deleteResponse.getStatusCode(), 204, "Delete status code should be 204.");
        ApiResponse<GenericError> getResponse = apiService.getUserNotFound(userId);
        Assert.assertEquals(getResponse.getStatusCode(), 404, "Get status code should be 404.");
        Assert.assertEquals(getResponse.getBody().message(), "Resource not found", "Error message mismatch.");
    }

    @Test(description = "TC-007")
    public void testFilterUsersByName() {
        String uniqueSearchName = "FilterTest " + UUID.randomUUID().toString().substring(0, 5);
        UserPayload payload = PayloadTemplate.getValidUserWithSpecificName(uniqueSearchName);
        apiService.createUser(payload);
        ApiResponse<UserPayload[]> response = apiService.getUsersListWithQueryParam("name", uniqueSearchName);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertTrue(response.getBody().length >= 1, "Should return at least 1 user.");
        Assert.assertEquals(response.getBody()[0].name(), uniqueSearchName, "Filtered user name mismatch.");
    }

    @Test(description = "TC-008")
    public void testCreateUserWithInvalidEmail() {
        UserPayload payload = PayloadTemplate.getUserWithInvalidEmail();
        ApiResponse<ValidationError[]> response = apiService.createUserExpectingValidationError(payload);
        Assert.assertEquals(response.getStatusCode(), 422, "Status code should be 422 Unprocessable Entity.");
        Assert.assertTrue(response.getBody().length > 0, "Should contain validation errors.");
        Assert.assertEquals(response.getBody()[0].field(), "email", "Error field mismatch.");
        Assert.assertEquals(response.getBody()[0].message(), "is invalid", "Error message mismatch.");
    }

    @Test(description = "TC-009")
    public void testGetNonExistentUser() {
        ApiResponse<GenericError> response = apiService.getUserNotFound(999999999);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code should be 404.");
        Assert.assertEquals(response.getBody().message(), "Resource not found", "Error message mismatch.");
    }

    @Test(description = "TC-010")
    public void testUnauthorizedRequest() {
        UserPayload payload = PayloadTemplate.getValidUser();
        ApiResponse<GenericError> response = apiService.createUserUnauthorized(payload);
        Assert.assertEquals(response.getStatusCode(), 401, "Status code should be 401");
        Assert.assertTrue(response.getHeader("Content-Type").contains("application/json"), "Missing JSON Content-Type.");
        Assert.assertEquals(response.getBody().message(), "Authentication failed", "Error message mismatch.");
    }
}