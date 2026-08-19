package ua.solvd.gorest.model;

public record ValidationError(
        String field,
        String message
) {
}