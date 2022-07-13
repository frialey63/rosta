package org.pjp.rosta.model;

import java.time.Instant;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User implements Comparable<User> {

    public static final String ADMIN = "admin";

    @Id
    private String uuid;

    @Size(min = 4, max = 20)
    @Pattern(regexp="[a-z0-9]+")
    @NotNull
    private String username;

    private boolean admin;

    @Size(max = 50)
    @Pattern(regexp="[A-Za-z ]+")
    @NotNull
    private String name;

    @Size(min = 8, max = 20)
    @NotNull
    private String password;

    private boolean passwordChange;

    private Instant passwordExpiry;

    private boolean enabled;

    @Size(max = 50)
    @Email
    @NotNull
    private String email;

    private boolean notifications;

    private boolean employee;

    private boolean keyholder;

    public User() {
        super();
    }

    public User(String uuid, @NotNull String username, boolean admin, @NotNull String name, @NotNull String password, boolean enabled, @NotNull String email, boolean notifications, boolean employee, boolean keyholder) {
        super();
        this.uuid = uuid;
        this.username = username;
        this.admin = admin;
        this.name = name;
        this.password = password;
        this.enabled = enabled;
        this.email = email;
        this.notifications = notifications;
        this.employee = employee;
        this.keyholder = keyholder;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String name) {
        this.uuid = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPasswordChange() {
        return passwordChange;
    }

    public void setPasswordChange(boolean passwordChange) {
        this.passwordChange = passwordChange;
    }

    public Instant getPasswordExpiry() {
        return passwordExpiry;
    }

    public void setPasswordExpiry(Instant passwordExpiry) {
        this.passwordExpiry = passwordExpiry;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public boolean isEmployee() {
        return employee;
    }

    public void setEmployee(boolean employee) {
        this.employee = employee;
    }

    public boolean isKeyholder() {
        return keyholder;
    }

    public void setKeyholder(boolean keyholder) {
        this.keyholder = keyholder;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public int compareTo(User other) {
        if (employee && !other.isEmployee()) {
            return -1;
        } else if (!employee && other.isEmployee()) {
            return 1;
        }

        return username.compareTo(other.getUsername());
    }

    public String getDisplayName() {
        return name + (keyholder ? "*" : "");
    }

}
