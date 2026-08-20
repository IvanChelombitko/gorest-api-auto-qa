package ua.solvd.gorest.service;


import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import tools.jackson.databind.ObjectMapper;
import ua.solvd.gorest.constant.Constant;
import ua.solvd.gorest.model.ApiResponse;
import ua.solvd.gorest.model.GenericError;
import ua.solvd.gorest.model.PostPayload;
import ua.solvd.gorest.model.UserPayload;
import ua.solvd.gorest.model.ValidationError;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UsersApiService {
    private final RequestSpecification authSpec;
    private final RequestSpecification unAuthSpec;
    private final ObjectMapper mapper;

    public UsersApiService(RequestSpecification authSpec, RequestSpecification unAuthSpec) {
        this.authSpec = authSpec;
        this.unAuthSpec = unAuthSpec;
        this.mapper = new ObjectMapper();
    }

    public ApiResponse<UserPayload> createUser(UserPayload user) {
        return sendPostRequest(authSpec, Constant.ENDPOINT, user, UserPayload.class);
    }

    public ApiResponse<ValidationError[]> createUserExpectingValidationError(UserPayload user) {
        return sendPostRequest(authSpec, Constant.ENDPOINT, user, ValidationError[].class);
    }

    public ApiResponse<GenericError> createUserUnauthorized(UserPayload user) {
        return sendPostRequest(unAuthSpec, Constant.ENDPOINT, user, GenericError.class);
    }

    public ApiResponse<UserPayload[]> getUsersList() {
        Response response = given().spec(authSpec).when().get(Constant.ENDPOINT);
        return parseResponse(response, UserPayload[].class);
    }

    public ApiResponse<UserPayload[]> getUsersListWithQueryParam(String key, String value) {
        Response response = given().spec(authSpec).queryParam(key, value).when().get(Constant.ENDPOINT);
        return parseResponse(response, UserPayload[].class);
    }

    public ApiResponse<UserPayload> getUser(int userId) {
        Response response = given().spec(authSpec).when().get(Constant.ENDPOINT + "/" + userId);
        return parseResponse(response, UserPayload.class);
    }

    public ApiResponse<GenericError> getUserNotFound(int userId) {
        Response response = given().spec(authSpec).when().get(Constant.ENDPOINT + "/" + userId);
        return parseResponse(response, GenericError.class);
    }

    public ApiResponse<UserPayload> updateUserPut(int userId, UserPayload user) {
        String path = Constant.ENDPOINT + "/" + userId;
        return sendPutRequest(authSpec, path, user, UserPayload.class);
    }

    public ApiResponse<UserPayload> updateUserPatch(int userId, Map<String, String> patchData) {
        String path = Constant.ENDPOINT + "/" + userId;
        return sendPatchRequest(authSpec, path, patchData, UserPayload.class);
    }

    public ApiResponse<Void> deleteUser(int userId) {
        Response response = given().spec(authSpec).when().delete(Constant.ENDPOINT + "/" + userId);
        return new ApiResponse<>(response.statusCode(), null, response.headers());
    }

    public ApiResponse<PostPayload> createPost(int userId, PostPayload post) {
        String path = Constant.ENDPOINT + "/" + userId + Constant.ENDPOINT_POSTS;
        return sendPostRequest(authSpec, path, post, PostPayload.class);
    }

    private <T> ApiResponse<T> sendPostRequest(RequestSpecification spec, String path, Object payload, Class<T> responseClass) {
        try {
            String jsonBody = mapper.writeValueAsString(payload);
            Response response = given().spec(spec).body(jsonBody).when().post(path);
            return parseResponse(response, responseClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload or send POST request to " + path, e);
        }
    }

    private <T> ApiResponse<T> sendPutRequest(RequestSpecification spec, String path, Object payload, Class<T> responseClass) {
        try {
            String jsonBody = mapper.writeValueAsString(payload);
            Response response = given().spec(spec).body(jsonBody).when().put(path);
            return parseResponse(response, responseClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload or send PUT request to " + path, e);
        }
    }

    private <T> ApiResponse<T> sendPatchRequest(RequestSpecification spec, String path, Object payload, Class<T> responseClass) {
        try {
            String jsonBody = mapper.writeValueAsString(payload);
            Response response = given().spec(spec).body(jsonBody).when().patch(path);
            return parseResponse(response, responseClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload or send PATCH request to " + path, e);
        }
    }

    private <T> ApiResponse<T> parseResponse(Response response, Class<T> responseClass) {
        try {
            T body = null;
            String responseString = response.asString();
            if (responseString != null && !responseString.trim().isEmpty() && !responseClass.equals(Void.class)) {
                body = mapper.readValue(responseString, responseClass);
            }
            return new ApiResponse<>(response.statusCode(), body, response.headers());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response body", e);
        }
    }
}