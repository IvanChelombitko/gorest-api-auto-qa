package ua.solvd.gorest.model;

import io.restassured.http.Headers;

public class ApiResponse<T> {
    private final int statusCode;
    private final T body;
    private final Headers headers;

    public ApiResponse(int statusCode, T body, Headers headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getBody() {
        return body;
    }

    public String getHeader(String headerName) {
        return headers.getValue(headerName);
    }
}