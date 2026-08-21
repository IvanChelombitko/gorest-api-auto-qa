package ua.solvd.gorest.model;

public record PostPayload(
        Integer id,
        Integer user_id,
        String title,
        String body
) {
}