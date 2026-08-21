package ua.solvd.gorest.model;

import tools.jackson.databind.JsonNode;

public record GraphqlResponseDto(
        JsonNode data,
        JsonNode errors,
        String message
) {
}