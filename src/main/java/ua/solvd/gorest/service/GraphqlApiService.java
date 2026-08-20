package ua.solvd.gorest.service;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import ua.solvd.gorest.constant.Constant;
import ua.solvd.gorest.model.ApiResponse;
import ua.solvd.gorest.model.GraphqlRequest;
import ua.solvd.gorest.model.GraphqlResponseDto;

import static io.restassured.RestAssured.given;

public class GraphqlApiService {
    private final RequestSpecification authSpec;
    private final RequestSpecification unAuthSpec;
    private final ObjectMapper mapper;

    public GraphqlApiService(RequestSpecification authSpec, RequestSpecification unAuthSpec) {
        this.authSpec = authSpec;
        this.unAuthSpec = unAuthSpec;
        this.mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public ApiResponse<GraphqlResponseDto> sendQuery(GraphqlRequest request) {
        return sendPostRequest(authSpec, Constant.GRAPHQL_ENDPOINT, request, GraphqlResponseDto.class);
    }

    public ApiResponse<GraphqlResponseDto> sendUnauthorizedQuery(GraphqlRequest request) {
        return sendPostRequest(unAuthSpec, Constant.GRAPHQL_ENDPOINT, request, GraphqlResponseDto.class);
    }

    private <T> ApiResponse<T> sendPostRequest(RequestSpecification spec, String path, Object payload, Class<T> responseClass) {
        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize GraphQL payload", e);
        }
        Response response = given().spec(spec).body(jsonBody).when().post(path);
        return parseResponse(response, responseClass);
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
            throw new RuntimeException("Failed to parse response body. Server returned status "
                    + response.statusCode() + " and body:\n" + response.asString(), e);
        }
    }
}