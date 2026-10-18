package nz.ac.auckland.se325.robolab.model;

public record User(Long id, String username, String fullName, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
