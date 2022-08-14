package org.pjp.rosta.model;

public enum UserRole {

    WORKER("ROLE_WORKER"), SUPERVISOR("ROLE_SUPERVISOR"), MANAGER("ROLE_MANAGER");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

}
