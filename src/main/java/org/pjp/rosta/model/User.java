package org.pjp.rosta.model;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User implements Comparable<User> {

    @Id
    private String uuid;

    @NotNull
    private String name;

    @NotNull
    private String email;

    private boolean employee;

    private boolean admin;

    public User() {
        super();
    }

    public User(String uuid, @NotNull String name, @NotNull String email, boolean employee, boolean admin) {
        super();
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.employee = employee;
        this.admin = admin;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String name) {
        this.uuid = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmployee() {
        return employee;
    }

    public void setEmployee(boolean employee) {
        this.employee = employee;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("User [uuid=");
        builder.append(uuid);
        builder.append(", name=");
        builder.append(name);
        builder.append(", email=");
        builder.append(email);
        builder.append(", employee=");
        builder.append(employee);
        builder.append(", admin=");
        builder.append(admin);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(User other) {
        if (employee && !other.isEmployee()) {
            return -1;
        } else if (!employee && other.isEmployee()) {
            return 1;
        }

        return name.compareTo(other.getName());
    }

}
