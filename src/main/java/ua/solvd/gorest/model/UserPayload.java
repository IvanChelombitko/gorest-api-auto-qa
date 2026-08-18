package ua.solvd.gorest.model;

public record UserPayload(
        String name,
        String email,
        String gender,
        String status
) {
    public UserPayload withUpdatedData(String newName, String newStatus) {
        return new UserPayload(newName, this.email(), this.gender(), newStatus);
    }
}