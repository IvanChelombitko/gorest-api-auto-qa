package ua.solvd.gorest.model;

public record UserPayload(
        Integer id,
        String name,
        String email,
        String gender,
        String status
) {
    public UserPayload withUpdatedData(String newName, String newStatus) {
        return new UserPayload(null, newName, this.email(), this.gender(), newStatus);
    }
}