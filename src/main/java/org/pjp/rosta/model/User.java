package org.pjp.rosta.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User implements Comparable<User> {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yy");

    private static boolean isNullOrBlank(String s) {
        return (s == null) || s.isBlank();
    }

    @Id
    private String uuid;

    @Size(min = 4, max = 20)
    @Pattern(regexp="[a-z0-9]+")
    @NotNull
    private String username;

    @NotNull
    private UserRole userRole;

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

    private LocalDateTime lastLoggedIn;

    @Size(max = 50)
    @Email
    @NotNull
    private String email;

    @Size(max = 20)
    @Pattern(regexp="[0-9 ]+")
    private String telephone;

    @Size(max = 50)
    @Pattern(regexp="[A-Za-z ]+")
    private String emergencyName;

    @Size(max = 20)
    @Pattern(regexp="[0-9 ]+")
    private String emergencyTelephone;

    private boolean notifications;

    private boolean employee;

    private boolean keyholder;

    public User() {
        super();
    }

    public User(String uuid, @NotNull String username, @NotNull UserRole role, @NotNull String name, @NotNull String password, boolean enabled, @NotNull String email, boolean notifications, boolean employee, boolean keyholder) {
        super();
        this.uuid = uuid;
        this.username = username;
        this.userRole = role;
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

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public boolean hasAccess(UserRole userRole) {
        return getUserRole().compareTo(userRole) >= 0;
    }

    public boolean isManager() {
        return userRole == UserRole.MANAGER;
    }

    public boolean isSupervisor() {
        return userRole == UserRole.SUPERVISOR;
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

    public LocalDateTime getLastLoggedIn() {
        return lastLoggedIn;
    }

    public String getLastLoggedInStr() {
        if (lastLoggedIn != null) {
            return lastLoggedIn.format(FORMATTER);
        }

        return "never";
    }

    public void setLastLoggedIn(LocalDateTime lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmergencyName() {
        return emergencyName;
    }

    public void setEmergencyName(String nokName) {
        this.emergencyName = nokName;
    }

    public String getEmergencyTelephone() {
        return emergencyTelephone;
    }

    public void setEmergencyTelephone(String nokTelephone) {
        this.emergencyTelephone = nokTelephone;
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
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        return Objects.equals(uuid, other.uuid);
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

    public boolean hasIncompleteProfile() {
        return isNullOrBlank(telephone) ||isNullOrBlank(emergencyName) ||isNullOrBlank(emergencyTelephone);
    }

}
